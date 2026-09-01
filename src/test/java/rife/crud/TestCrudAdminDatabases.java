/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.exceptions.DatabaseException;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rifetestmodels.CrudArticle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the parts of the administration that depend on the behavior of
 * the database itself, against each of the embedded databases.
 */
public class TestCrudAdminDatabases {
    /**
     * Waits for the threads with a bound, so that a deadlock fails the test
     * instead of stalling the suite, while a loaded machine still gets the
     * room it needs to finish work that takes milliseconds.
     */
    private void joinAll(List<Thread> running)
    throws InterruptedException {
        for (var thread : running) {
            thread.join(30_000);
        }
        for (var thread : running) {
            assertFalse(thread.isAlive(), "an operation was still waiting after 30 seconds, which means it was deadlocked");
        }
    }

    private CrudArticle article(String title) {
        var article = new CrudArticle();
        article.setTitle(title);
        article.setAuthor("Geert");
        article.setStatus("draft");
        return article;
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstallGuardsTheOrdinalsWithAConstraint(Datasource datasource) {
        var admin = new CrudAdmin(datasource).entity(CrudArticle.class);
        admin.install();
        try {
            // installing again is skipped through the table metadata
            admin.install();

            var manager = GenericQueryManagerFactory.instance(datasource, CrudArticle.class);
            var first = article("First");
            first.setOrdinal(0);
            manager.save(first);

            // two rows with the same ordinal can't be stored, whichever way
            // they got it
            var duplicate = article("Second");
            duplicate.setOrdinal(0);
            var e = assertThrows(DatabaseException.class, () -> manager.save(duplicate));
            assertTrue(CrudElement.isConstraintViolation(e));
        } finally {
            admin.remove();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAViolationThatIsntAnOrdinalCollisionIsntRetried(Datasource datasource) {
        var admin = new CrudAdmin(datasource).entity(CrudArticle.class);
        admin.install();
        try {
            @SuppressWarnings("unchecked")
            var entity = (CrudEntity<CrudArticle>) admin.getEntities().get(0);
            var add = new CrudAdd<>(admin, entity);

            // the title is mandatory in the database, and its violation is
            // about the instance itself, not about an ordinal that another
            // addition took first
            var invalid = new CrudArticle();
            invalid.setStatus("draft");
            // a value that the database refuses comes back as something to
            // correct instead of as a failure
            assertEquals(CrudAdd.Outcome.INVALID, add.saveOrdered(invalid));

            var manager = GenericQueryManagerFactory.instance(datasource, CrudArticle.class);
            assertEquals(0, manager.count());
        } finally {
            admin.remove();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSimultaneousMovesKeepTheOrdinalsConsecutive(Datasource datasource)
    throws Exception {
        var admin = new CrudAdmin(datasource).entity(CrudArticle.class);
        admin.install();
        try {
            @SuppressWarnings("unchecked")
            var entity = (CrudEntity<CrudArticle>) admin.getEntities().get(0);
            var add = new CrudAdd<>(admin, entity);
            var move = new CrudMove<>(admin, entity);

            for (var i = 0; i < 5; i++) {
                add.saveOrdered(article("Row " + i));
            }
            var manager = GenericQueryManagerFactory.instance(datasource, CrudArticle.class);
            var ids = manager.restore(manager.getRestoreQuery().orderBy("ordinal"))
                .stream().map(CrudArticle::getId).toList();

            // the movers contend on the same rows, including two that fight
            // over the same one, and a move that the database refuses to
            // break a deadlock counts as a conflict instead of an error
            var errors = Collections.synchronizedList(new ArrayList<Throwable>());
            for (var round = 0; round < 3; round++) {
                var moves = new int[][]{
                    {ids.get(0), 0}, {ids.get(1), 1}, {ids.get(2), 0}, {ids.get(2), 1}
                };
                var barrier = new CyclicBarrier(moves.length);
                var running = new ArrayList<Thread>();
                for (var m : moves) {
                    var thread = new Thread(() -> {
                        try {
                            barrier.await();
                            move.moveCurrent(null, m[0], m[1] == 1);
                        } catch (DatabaseException e) {
                            if (!CrudElement.isTransactionConflict(e)) {
                                errors.add(e);
                            }
                        } catch (Throwable e) {
                            errors.add(e);
                        }
                    });
                    running.add(thread);
                    thread.start();
                }
                joinAll(running);
            }

            assertTrue(errors.isEmpty(), String.valueOf(errors));
            var stored = manager.restore(manager.getRestoreQuery().orderBy("ordinal"));
            assertEquals(5, stored.size());
            for (var i = 0; i < stored.size(); i++) {
                assertEquals(i, stored.get(i).getOrdinal());
            }
        } finally {
            admin.remove();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSimultaneousAdditionsDeletionsAndMovesKeepTheOrdinalsConsecutive(Datasource datasource)
    throws Exception {
        var admin = new CrudAdmin(datasource).entity(CrudArticle.class);
        admin.install();
        try {
            @SuppressWarnings("unchecked")
            var entity = (CrudEntity<CrudArticle>) admin.getEntities().get(0);
            var add = new CrudAdd<>(admin, entity);
            var move = new CrudMove<>(admin, entity);
            var manager = GenericQueryManagerFactory.instance(datasource, CrudArticle.class);

            for (var i = 0; i < 6; i++) {
                add.saveOrdered(article("Row " + i));
            }

            // the operations that renumber the same list run at the same
            // time, so a deletion that is tightening can't be interleaved
            // with an addition that is appending
            var errors = Collections.synchronizedList(new ArrayList<Throwable>());
            var ids = manager.restore(manager.getRestoreQuery().orderBy("ordinal"))
                .stream().map(CrudArticle::getId).toList();
            var barrier = new CyclicBarrier(4);
            var running = new ArrayList<Thread>();
            Runnable[] work = {
                () -> add.saveOrdered(article("Added")),
                () -> new CrudDelete<>(admin, entity).delete(null, manager.restore(ids.get(1)), ids.get(1)),
                () -> move.moveCurrent(null, ids.get(3), true),
                () -> move.moveCurrent(null, ids.get(4), false)
            };
            for (var task : work) {
                var thread = new Thread(() -> {
                    try {
                        barrier.await();
                        task.run();
                    } catch (DatabaseException e) {
                        if (!CrudElement.isTransactionConflict(e)) {
                            errors.add(e);
                        }
                    } catch (Throwable e) {
                        errors.add(e);
                    }
                });
                running.add(thread);
                thread.start();
            }
            joinAll(running);

            assertTrue(errors.isEmpty(), String.valueOf(errors));
            var stored = manager.restore(manager.getRestoreQuery().orderBy("ordinal"));
            assertEquals(6, stored.size());
            // the ordinals may hold gaps, since a deletion doesn't close
            // them, but no two instances ever share one
            for (var i = 1; i < stored.size(); i++) {
                assertTrue(stored.get(i - 1).getOrdinal() < stored.get(i).getOrdinal(),
                    "two instances ended up with the same ordinal");
            }
        } finally {
            admin.remove();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGappedOrdinalsGetTheirOwnBoundaries(Datasource datasource) {
        var admin = new CrudAdmin(datasource).entity(CrudArticle.class);
        admin.install();
        try {
            var manager = GenericQueryManagerFactory.instance(datasource, CrudArticle.class);
            for (var ordinal : new int[]{5, 10}) {
                var article = article("Ordinal " + ordinal);
                article.setOrdinal(ordinal);
                manager.save(article);
            }

            @SuppressWarnings("unchecked")
            var entity = (CrudEntity<CrudArticle>) admin.getEntities().get(0);
            var move = new CrudMove<>(admin, entity);
            var ids = manager.restore(manager.getRestoreQuery().orderBy("ordinal"))
                .stream().map(CrudArticle::getId).toList();

            // ordinals that don't start at zero and skip values still lock
            // and move the instances that they actually belong to
            assertEquals(CrudMove.Outcome.MOVED, move.moveCurrent(null, ids.get(1), true));
            var stored = manager.restore(manager.getRestoreQuery().orderBy("ordinal"));
            assertEquals("Ordinal 10", stored.get(0).getTitle());
            assertEquals("Ordinal 5", stored.get(1).getTitle());
        } finally {
            admin.remove();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveOutcomesAreReported(Datasource datasource)
    throws Exception {
        var admin = new CrudAdmin(datasource).entity(CrudArticle.class);
        admin.install();
        try {
            @SuppressWarnings("unchecked")
            var entity = (CrudEntity<CrudArticle>) admin.getEntities().get(0);
            var add = new CrudAdd<>(admin, entity);
            var move = new CrudMove<>(admin, entity);

            add.saveOrdered(article("Only"));
            var manager = GenericQueryManagerFactory.instance(datasource, CrudArticle.class);
            var id = manager.restore().get(0).getId();

            // an instance that is gone is reported as missing
            assertEquals(CrudMove.Outcome.MISSING, move.moveCurrent(null, 9999, true));
            // the only instance of a list has nothing to be moved past
            assertEquals(CrudMove.Outcome.UNCHANGED, move.moveCurrent(null, id, true));
            assertEquals(0, manager.restore().get(0).getOrdinal());
        } finally {
            admin.remove();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSimultaneousAdditionsGetDistinctOrdinals(Datasource datasource)
    throws Exception {
        var admin = new CrudAdmin(datasource).entity(CrudArticle.class);
        admin.install();
        try {
            @SuppressWarnings("unchecked")
            var entity = (CrudEntity<CrudArticle>) admin.getEntities().get(0);
            var add = new CrudAdd<>(admin, entity);

            // the additions all allocate at the same moment, so most of them
            // obtain an ordinal that another one stores first, and they stay
            // within the connection pool since a transaction that has to
            // wait for a connection does so while holding the datasource
            // monitor that nested transactions need
            var threads = 4;
            var rounds = 3;
            var errors = Collections.synchronizedList(new ArrayList<Throwable>());
            for (var round = 0; round < rounds; round++) {
                var barrier = new CyclicBarrier(threads);
                var running = new ArrayList<Thread>();
                for (var i = 0; i < threads; i++) {
                    var thread = new Thread(() -> {
                        try {
                            barrier.await();
                            add.saveOrdered(article("Concurrent"));
                        } catch (Throwable e) {
                            errors.add(e);
                        }
                    });
                    running.add(thread);
                    thread.start();
                }
                joinAll(running);
            }

            assertTrue(errors.isEmpty(), String.valueOf(errors));
            var manager = GenericQueryManagerFactory.instance(datasource, CrudArticle.class);
            var stored = manager.restore(manager.getRestoreQuery().orderBy("ordinal"));
            assertEquals(threads * rounds, stored.size());
            for (var i = 0; i < stored.size(); i++) {
                assertEquals(i, stored.get(i).getOrdinal());
            }
        } finally {
            admin.remove();
        }
    }
}
