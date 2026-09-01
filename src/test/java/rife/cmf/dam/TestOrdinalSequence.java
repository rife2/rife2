/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.TestDatasources;
import rife.cmf.dam.exceptions.ListRequiredException;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.*;

public class TestOrdinalSequence {
    private static final String TABLE = "sequencetest";

    private DbQueryManager setup(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        manager.executeUpdate(new CreateTable(datasource)
            .table(TABLE)
            .column("id", int.class, CreateTable.NOTNULL)
            .column("book", int.class, CreateTable.NOTNULL)
            .column("ordinal", int.class, CreateTable.NOTNULL)
            .primaryKey("id"));
        return manager;
    }

    private void tearDown(Datasource datasource, DbQueryManager manager) {
        try {
            manager.executeUpdate(new DropTable(datasource).table(TABLE));
        } catch (Exception e) {
            // already gone
        }
    }

    private void add(Datasource datasource, DbQueryManager manager, int id, int book, int ordinal) {
        manager.executeUpdate(new Insert(datasource)
                .into(TABLE)
                .fieldParameter("id").fieldParameter("book").fieldParameter("ordinal"),
            statement -> statement.setInt("id", id).setInt("book", book).setInt("ordinal", ordinal));
    }

    private int ordinalOf(Datasource datasource, DbQueryManager manager, int id) {
        return manager.executeGetFirstInt(new Select(datasource)
            .field("ordinal").from(TABLE).whereParameter("id", "="), statement -> statement.setInt("id", id));
    }

    private List<Integer> order(Datasource datasource, DbQueryManager manager, int book) {
        var result = new ArrayList<Integer>();
        manager.executeFetchAll(new Select(datasource)
                .field("id").from(TABLE).whereParameter("book", "=").orderBy("ordinal"),
            resultSet -> result.add(resultSet.getInt(1)),
            statement -> statement.setInt("book", book));
        return result;
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMovesByIdentifierWithinItsList(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 1, 0);
            add(datasource, manager, 2, 1, 1);
            add(datasource, manager, 3, 1, 2);
            add(datasource, manager, 4, 2, 0);

            assertTrue(sequence.moveUp(3, 1));
            assertEquals(List.of(1, 3, 2), order(datasource, manager, 1));

            assertTrue(sequence.moveDown(1, 1));
            assertEquals(List.of(3, 1, 2), order(datasource, manager, 1));

            // the boundaries of a list are its own
            assertFalse(sequence.moveUp(3, 1));
            assertFalse(sequence.moveDown(2, 1));
            assertFalse(sequence.moveUp(4, 2));

            // and the other list was left alone
            assertEquals(List.of(4), order(datasource, manager, 2));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMovesThroughOrdinalsThatDontFollowEachOther(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 1, 5);
            add(datasource, manager, 2, 1, 10);
            add(datasource, manager, 3, 1, 40);

            // a row is exchanged with the one next to it, whatever the
            // distance between their ordinals happens to be
            assertTrue(sequence.moveUp(3, 1));
            assertEquals(List.of(1, 3, 2), order(datasource, manager, 1));

            assertTrue(sequence.moveUp(3, 1));
            assertEquals(List.of(3, 1, 2), order(datasource, manager, 1));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemovingARowLeavesTheRestInOrder(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 1, 0);
            add(datasource, manager, 2, 1, 1);
            add(datasource, manager, 3, 1, 2);

            manager.executeUpdate("DELETE FROM " + TABLE + " WHERE id = 2");

            // nothing has to close the gap for the moves to keep working
            assertTrue(sequence.moveUp(3, 1));
            assertEquals(List.of(3, 1), order(datasource, manager, 1));

            assertTrue(sequence.tighten(1));
            assertEquals(List.of(3, 1), order(datasource, manager, 1));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAMoveStaysInsideTheListItWasGiven(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 1, 0);
            add(datasource, manager, 2, 2, 0);
            add(datasource, manager, 3, 2, 1);

            // the row belongs to another list than the one that is locked,
            // so it isn't found rather than being rewritten from the wrong
            // list
            assertFalse(sequence.moveUp(3, 1));
            assertEquals(List.of(2, 3), order(datasource, manager, 2));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTighteningRecoversWhenTheTopIsReached(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 1, Integer.MAX_VALUE - 1);
            add(datasource, manager, 2, 1, Integer.MAX_VALUE);

            // renumbering doesn't need room above the ordinals to move
            // through, since nothing forbids two of them being equal while
            // it happens
            assertTrue(sequence.tighten(1));
            assertEquals(List.of(1, 2), order(datasource, manager, 1));
            assertEquals(0, ordinalOf(datasource, manager, 1));
            assertEquals(1, ordinalOf(datasource, manager, 2));

            // and appending is possible again afterwards
            sequence.inList(1, () -> add(datasource, manager, 3, 1, sequence.nextOrdinal(1)));
            assertEquals(List.of(1, 2, 3), order(datasource, manager, 1));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTighteningNegativeOrdinalsDoesntCollideWithItself(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 1, -3);
            add(datasource, manager, 2, 1, -2);

            // the range that the rows are moved through has to stay clear of
            // the ordinals that they're about to be given, or the first one
            // lands on a row that is still parked there
            assertTrue(sequence.tighten(1));
            assertEquals(List.of(1, 2), order(datasource, manager, 1));
            assertEquals(0, ordinalOf(datasource, manager, 1));
            assertEquals(1, ordinalOf(datasource, manager, 2));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testReadsThePlacementStraightFromTheTable(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 2, 7);

            assertEquals(7, sequence.storedOrdinal(1));
            assertEquals(2, sequence.storedRestriction(1));
            assertTrue(sequence.isPlacedAt(1, 2, 7));
            assertFalse(sequence.isPlacedAt(1, 3, 7));
            assertFalse(sequence.isPlacedAt(1, 2, 8));

            // a row that isn't there is nowhere
            assertNull(sequence.storedOrdinal(999));
            assertFalse(sequence.isPlacedAt(999, 2, 7));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @Test
    void testASecondDatasourceGetsItsOwnTransaction() {
        // one database is enough to tell whether the second datasource ran
        // its work inside a transaction of its own
        var datasource = TestDatasources.H2;
        var manager = setup(datasource);
        // a second instance that describes the same database hands out its
        // own connections, so being inside a list of the first one says
        // nothing about the transactions of the second
        var other = new Datasource(datasource.getDriver(), datasource.getUrl(),
            datasource.getUser(), datasource.getPassword(), 5);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            var same_database = new OrdinalSequence(other, TABLE, "id", "ordinal", "book");
            var other_manager = new DbQueryManager(other);

            // the list holds nothing yet, so entering it from both of them
            // doesn't wait on rows that the other one is holding
            try {
                sequence.inList(7, () -> same_database.inList(7, () -> {
                    add(other, other_manager, 1, 7, 0);
                    throw new IllegalStateException("stop");
                }));
                fail("expected the operation to fail");
            } catch (IllegalStateException e) {
                assertEquals("stop", e.getMessage());
            }

            // the row was written inside a transaction of its own datasource,
            // which the failure took back
            assertEquals(0, manager.executeGetFirstInt(new Select(datasource)
                .field("count(*)").from(TABLE)));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testReadsTheEndsOfSeveralListsAtOnce(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            // ordinals don't have to start anywhere or run without gaps
            add(datasource, manager, 1, 1, 5);
            add(datasource, manager, 2, 1, 40);
            add(datasource, manager, 3, 2, -3);
            add(datasource, manager, 4, 2, -1);
            add(datasource, manager, 5, 3, 7);

            var bounds = sequence.bounds(List.of(1L, 2L, 99L));

            // only the lists that were asked about and hold rows come back
            assertEquals(2, bounds.size());
            assertArrayEquals(new int[]{5, 40}, bounds.get(1L));
            assertArrayEquals(new int[]{-3, -1}, bounds.get(2L));
            // a list that holds nothing has no ends, and one that wasn't
            // asked about doesn't come back either
            assertNull(bounds.get(99L));
            assertNull(bounds.get(3L));

            assertTrue(sequence.bounds(List.of()).isEmpty());
            assertTrue(sequence.bounds(null).isEmpty());
        } finally {
            tearDown(datasource, manager);
        }
    }

    @Test
    void testReadsTheEndsOfAnUnpartitionedTable() {
        var datasource = TestDatasources.H2;
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal");
            add(datasource, manager, 1, 1, 5);
            add(datasource, manager, 2, 2, 40);

            // the whole table is one list, whatever it is asked about
            var bounds = sequence.bounds(List.of(-1L));
            assertEquals(1, bounds.size());
            assertArrayEquals(new int[]{5, 40}, bounds.get(-1L));

            // and a table that holds nothing has no ends to report, the same
            // way a list that holds nothing has none
            manager.executeUpdate("DELETE FROM " + TABLE);
            assertTrue(sequence.bounds(List.of(-1L)).isEmpty());
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testOperationsWithoutAListRefuseAPartitionedSequence(Datasource datasource) {
        var partitioned = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");

        // an operation that doesn't name a list would quietly work on the
        // list of a value that nothing uses
        assertThrows(ListRequiredException.class, partitioned::nextOrdinal);
        assertThrows(ListRequiredException.class, partitioned::tighten);
        assertThrows(ListRequiredException.class, () -> partitioned.moveUp(1));
        assertThrows(ListRequiredException.class, () -> partitioned.moveDown(1));
        assertThrows(ListRequiredException.class, () -> partitioned.inList(() -> fail("the operation ran")));
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRefusesNamesThatArentUsable(Datasource datasource) {
        // the names end up inside the queries, so one that isn't usable is
        // refused at construction instead of failing at first use
        assertThrows(IllegalArgumentException.class,
            () -> new OrdinalSequence(datasource, "bad name", "id", "ordinal"));
        assertThrows(IllegalArgumentException.class,
            () -> new OrdinalSequence(datasource, TABLE, "id; drop", "ordinal"));
        assertThrows(IllegalArgumentException.class,
            () -> new OrdinalSequence(datasource, TABLE, "id", "1ordinal"));
        assertThrows(IllegalArgumentException.class,
            () -> new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book-scope"));
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSimultaneousFirstAdditionsAreKeptApart(Datasource datasource)
    throws Exception {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");

            // the list holds nothing yet, so the database has no rows to
            // lock and the application itself has to keep these apart
            var errors = Collections.synchronizedList(new ArrayList<Throwable>());
            var barrier = new CyclicBarrier(4);
            var running = new ArrayList<Thread>();
            for (var i = 0; i < 4; i++) {
                var id = 100 + i;
                var thread = new Thread(() -> {
                    try {
                        barrier.await();
                        sequence.inList(1, () -> add(datasource, manager, id, 1, sequence.nextOrdinal(1)));
                    } catch (Throwable e) {
                        errors.add(e);
                    }
                });
                running.add(thread);
                thread.start();
            }
            for (var thread : running) {
                thread.join(30_000);
            }
            for (var thread : running) {
                assertFalse(thread.isAlive(), "an addition was still waiting after 30 seconds");
            }

            assertTrue(errors.isEmpty(), String.valueOf(errors));
            var ordinals = new ArrayList<Integer>();
            for (var id = 100; id < 104; id++) {
                ordinals.add(ordinalOf(datasource, manager, id));
            }
            Collections.sort(ordinals);
            assertEquals(List.of(0, 1, 2, 3), ordinals);
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMovesThroughTheTopOfTheRange(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 1, 5);
            add(datasource, manager, 2, 1, Integer.MAX_VALUE);

            // there's no room above the highest ordinal to exchange through,
            // so the room below the lowest one gets used instead
            assertTrue(sequence.moveUp(2, 1));
            assertEquals(List.of(2, 1), order(datasource, manager, 1));
            assertEquals(5, ordinalOf(datasource, manager, 2));
        } finally {
            tearDown(datasource, manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSimultaneousAppendsGetTheirOwnOrdinal(Datasource datasource)
    throws Exception {
        var manager = setup(datasource);
        try {
            var sequence = new OrdinalSequence(datasource, TABLE, "id", "ordinal", "book");
            add(datasource, manager, 1, 1, 0);

            // holding the list makes obtaining an ordinal and storing it one
            // thing, so no two appends can be given the same one
            var errors = Collections.synchronizedList(new ArrayList<Throwable>());
            var barrier = new CyclicBarrier(4);
            var running = new ArrayList<Thread>();
            for (var i = 0; i < 4; i++) {
                var id = 100 + i;
                var thread = new Thread(() -> {
                    try {
                        barrier.await();
                        sequence.inList(1, () -> add(datasource, manager, id, 1, sequence.nextOrdinal(1)));
                    } catch (Throwable e) {
                        errors.add(e);
                    }
                });
                running.add(thread);
                thread.start();
            }
            for (var thread : running) {
                thread.join(30_000);
            }
            for (var thread : running) {
                assertFalse(thread.isAlive(), "an append was still waiting after 30 seconds");
            }

            assertTrue(errors.isEmpty(), String.valueOf(errors));
            assertEquals(5, order(datasource, manager, 1).size());
        } finally {
            tearDown(datasource, manager);
        }
    }
}
