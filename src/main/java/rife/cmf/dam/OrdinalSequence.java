/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.cmf.dam.exceptions.OrdinalsOutOfRoomException;
import rife.cmf.dam.exceptions.ListRequiredException;
import rife.cmf.dam.exceptions.RowNotInListException;
import rife.cmf.dam.exceptions.TransactionsRequiredException;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.Select;
import rife.database.queries.Update;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Orders the rows of a table relative to each other through the identifiers
 * of those rows.
 * <p>An {@code OrdinalSequence} does what an {@link OrdinalManager} does,
 * with the difference that every operation names the row that it applies to
 * by its identifier instead of by the ordinal that the row happens to have.
 * With an {@code OrdinalManager}, {@code up(2)} moves whatever row holds
 * ordinal {@code 2} at that moment, so when another user reorders the list
 * between the rendering of a page and the click on one of its buttons, the
 * click moves a different row than the one that was shown. The identifier
 * of a row never changes, so {@code moveUp(87)} moves row {@code 87}
 * whatever happened in between:
 * <pre>var sequence = new OrdinalSequence(datasource, "article", "id", "ordinal", "categoryId");
 *sequence.moveUp(87, categoryId);</pre>
 * <p>When a restricting column is provided, the rows that share a value of
 * it form their own list: the articles of category {@code 12} are one
 * list, the ones of category {@code 13} another, and each list is ordered
 * on its own. Without a restricting column the whole table is one list.
 * <p>The ordinals don't have to follow each other and don't have to start
 * anywhere in particular. A move exchanges the ordinals of two rows that
 * are next to each other in ordinal order, whatever the distance between
 * their values. Deleting the row with ordinal {@code 1} from a list with
 * ordinals {@code 0}, {@code 1} and {@code 2} leaves {@code 0} and
 * {@code 2}, and every operation keeps working on that. {@link #tighten()}
 * renumbers them to {@code 0} and {@code 1} for tidiness, not for
 * correctness.
 * <p>Every operation runs inside a transaction that first locks all the
 * rows of its list with a single {@code UPDATE}, so the operations of a
 * list run one after the other. A manually ordered list holds the number
 * of rows that somebody is willing to arrange by hand, which is what makes
 * locking all of them affordable, and nothing has to be installed alongside
 * the table that is being ordered.
 * <p>A list that holds nothing yet has no rows to lock. Within one
 * application that doesn't matter, since the operations of a list also
 * wait for each other in the application itself, but two applications that
 * share a database can each hand their very first addition to a list the
 * same ordinal. Declare the ordinal unique in the database, together with
 * its restriction when it has one, so that the second of those two
 * additions is refused and can be submitted again.
 * <p>The instances of this class hold no state of their own and can be
 * shared between threads.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see OrdinalManager
 * @since 1.10
 */
public class OrdinalSequence {
    // the lists that this application is inside of, so that the operations
    // of a list also wait for each other when the list has no rows yet for
    // the database to lock
    // an entry is only here while there are threads inside that list or
    // waiting for it, and the last one out takes it away again, so the
    // lists that keep arriving leave nothing behind
    private static final ConcurrentMap<String, HeldList> LIST_LOCKS = new ConcurrentHashMap<>();

    private record HeldList(ReentrantLock lock, int holders) {
    }

    // the lists that the current thread is already inside of, so that
    // entering one of them again doesn't hold and renumber what it is
    // already holding
    private static final ThreadLocal<Set<ListEntry>> ENTERED_LISTS = ThreadLocal.withInitial(HashSet::new);

    // names a list that a thread is inside of; the datasource is compared
    // by identity rather than by what it describes, since two instances that
    // point at the same database each provide their own connections, and
    // being inside a transaction of one of them says nothing about the
    // other, while every column that the sequence was made with is part of
    // this as well, so that two sequences which order the same table
    // differently are never taken for one another
    private record ListEntry(Datasource datasource, String table, String identifierColumn,
                             String ordinalColumn, String restrictColumn, long restriction) {
        public boolean equals(Object other) {
            return other instanceof ListEntry entry &&
                   datasource == entry.datasource &&
                   restriction == entry.restriction &&
                   table.equals(entry.table) &&
                   identifierColumn.equals(entry.identifierColumn) &&
                   ordinalColumn.equals(entry.ordinalColumn) &&
                   Objects.equals(restrictColumn, entry.restrictColumn);
        }

        public int hashCode() {
            return Objects.hash(System.identityHashCode(datasource), table, identifierColumn,
                ordinalColumn, restrictColumn, restriction);
        }
    }

    private static final int LIST_TIMEOUT_SECONDS = 30;

    private final Datasource datasource_;
    private final DbQueryManager dbQueryManager_;
    private final String table_;
    private final String identifierColumn_;
    private final String ordinalColumn_;
    private final String restrictColumn_;
    private volatile boolean transactionsVerified_ = false;

    /**
     * Creates a new sequence that orders every row of a table relative to
     * every other one.
     *
     * @param datasource       the datasource that the table is accessible
     *                         through
     * @param table            the name of the table that is ordered
     * @param identifierColumn the name of the column that identifies a row
     * @param ordinalColumn    the name of the column that holds the ordinals
     * @since 1.10
     */
    public OrdinalSequence(Datasource datasource, String table, String identifierColumn, String ordinalColumn) {
        this(datasource, table, identifierColumn, ordinalColumn, null);
    }

    /**
     * Creates a new sequence that orders the rows of a table within the
     * separate lists that a column partitions them into.
     *
     * @param datasource       the datasource that the table is accessible
     *                         through
     * @param table            the name of the table that is ordered
     * @param identifierColumn the name of the column that identifies a row
     * @param ordinalColumn    the name of the column that holds the ordinals
     * @param restrictColumn   the name of the column that partitions the
     *                         rows into separate lists, or {@code null}
     * @since 1.10
     */
    public OrdinalSequence(Datasource datasource, String table, String identifierColumn, String ordinalColumn, String restrictColumn) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null");
        // the names end up inside the queries that this builds, so one that
        // isn't usable fails here instead of as a syntax error at first use
        verifyName(table, "table");
        verifyName(identifierColumn, "identifierColumn");
        verifyName(ordinalColumn, "ordinalColumn");
        if (restrictColumn != null) {
            verifyName(restrictColumn, "restrictColumn");
        }

        datasource_ = datasource;
        dbQueryManager_ = new DbQueryManager(datasource);
        table_ = table;
        identifierColumn_ = identifierColumn;
        ordinalColumn_ = ordinalColumn;
        restrictColumn_ = restrictColumn;
    }

    /**
     * Retrieves the name of the table that this sequence orders.
     *
     * @return the name of the ordered table
     * @since 1.10
     */
    public String getTable() {
        return table_;
    }

    /**
     * Retrieves the name of the column that identifies a row.
     *
     * @return the name of the identifier column
     * @since 1.10
     */
    public String getIdentifierColumn() {
        return identifierColumn_;
    }

    /**
     * Retrieves the name of the column that holds the ordinals.
     *
     * @return the name of the ordinal column
     * @since 1.10
     */
    public String getOrdinalColumn() {
        return ordinalColumn_;
    }

    /**
     * Retrieves the name of the column that partitions the rows into
     * separate lists.
     *
     * @return the name of the restricting column; or
     * <p>{@code null} when the rows aren't partitioned
     * @since 1.10
     */
    public String getRestrictColumn() {
        return restrictColumn_;
    }

    /**
     * Performs an operation while the rows of the entire table are only
     * being ordered by it.
     *
     * @param operation the operation to perform
     * @see #inList(long, Runnable)
     * @since 1.10
     */
    public void inList(Runnable operation) {
        verifyUnrestricted();
        inList(-1, operation);
    }

    /**
     * Performs an operation while the rows of a list are only being ordered
     * by it.
     * <p>Perform everything that reads ordinals to then write them as one
     * operation. Appending a row is the typical case:
     * <pre>sequence.inList(categoryId, () -> {
     *    article.setOrdinal(sequence.nextOrdinal(categoryId));
     *    manager.insert(article);
     *});</pre>
     * <p>Without this, another request can be handed the same ordinal
     * between the call to {@code nextOrdinal} and the insert, and one of
     * the two rows ends up misplaced.
     * <p>The operation runs inside a transaction that also holds every row
     * of the list, so an operation that is already running a transaction is
     * simply performed within it. A list without rows has nothing for the
     * database to hold, which is covered within this application by the
     * list also being held here, while another application sharing the
     * database is only kept out by the uniqueness of the ordinals.
     * <p>That cover ends where this operation does. When this joins a
     * transaction that was already running, the list is let go of as soon as
     * the operation returns while the transaction that wrote in it is still
     * open, so another thread can enter a list that holds nothing yet before
     * the first rows of it are committed. The uniqueness of the ordinals is
     * what keeps those apart, there as well as between applications.
     * <p>Perform the work of one list in one operation. Entering a list
     * while already inside another one makes two operations wait for each
     * other, and one that waits too long is broken off and reported.
     * <p>Entering the same list again is free. The rows are already held by
     * the operation that is inside it and the transaction that holds them is
     * still open, so the work is simply performed without holding or
     * renumbering anything a second time.
     *
     * @param restriction the value of the restricting column that names the
     *                    list, which is ignored when the rows aren't
     *                    partitioned
     * @param operation   the operation to perform
     * @since 1.10
     */
    public void inList(long restriction, Runnable operation) {
        if (null == operation) throw new IllegalArgumentException("operation can't be null");

        verifyTransactions();

        // a list that this thread is already inside of is held by that
        // operation and renumbered within its transaction, so entering it
        // again only performs the work
        var entry = listEntry(restriction);
        var entered = ENTERED_LISTS.get();
        if (!entered.add(entry)) {
            operation.run();
            return;
        }

        try {
            // the list is also held in the application itself, so operations
            // on a list that has no rows yet for the database to lock still
            // wait for each other, at least within this application
            // this one is named by what the datasource describes rather than
            // by which instance it is, since the operations that another
            // instance performs on the same database have to wait too
            var key = listKey(restriction);
            var lock = enterListLock(key);
            try {
                try {
                    if (!lock.tryLock(LIST_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        throw new DatabaseException("A list of the '" + table_ + "' table couldn't be entered, " +
                                                    "another operation has been holding it for over " + LIST_TIMEOUT_SECONDS + " seconds.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new DatabaseException("A list of the '" + table_ + "' table couldn't be entered, " +
                                                "the thread was interrupted while waiting for it.", e);
                }
                try {
                    dbQueryManager_.inTransaction(() -> {
                        lockList(restriction);
                        operation.run();
                    });
                } finally {
                    lock.unlock();
                }
            } finally {
                leaveListLock(key);
            }
        } finally {
            entered.remove(entry);
            if (entered.isEmpty()) {
                ENTERED_LISTS.remove();
            }
        }
    }

    /**
     * Retrieves the ordinal that places a row after every other one of a
     * list.
     * <p>This is the highest ordinal of the list plus one, so for a list
     * with ordinals {@code 5} and {@code 10} it returns {@code 11}, and for
     * a list without rows it returns {@code 0}. Obtain it and store it
     * within one {@link #inList(long, Runnable)} operation, so that nothing
     * else can be handed the same one in between.
     * <p>The ordinals of a list run out at {@link Integer#MAX_VALUE}, which
     * {@link #tighten(long)} makes room in again.
     *
     * @param restriction the value of the restricting column that names the
     *                    list, which is ignored when the rows aren't
     *                    partitioned
     * @return the ordinal that comes after every other one of the list
     * @since 1.10
     */
    public int nextOrdinal(long restriction) {
        var query = new Select(datasource_)
            .field("MAX(" + ordinalColumn_ + ")")
            .from(table_);
        restrict(query);
        var highest = dbQueryManager_.executeGetFirstString(query, statement -> bindRestriction(statement, restriction));
        if (null == highest) {
            return 0;
        }
        var last = Integer.parseInt(highest);
        if (Integer.MAX_VALUE == last) {
            throw new OrdinalsOutOfRoomException(table_);
        }
        return last + 1;
    }

    /**
     * Retrieves the ordinal that places a row after every other one.
     *
     * @return the ordinal that comes after every other one
     * @see #nextOrdinal(long)
     * @since 1.10
     */
    public int nextOrdinal() {
        verifyUnrestricted();
        return nextOrdinal(-1);
    }

    /**
     * Moves a row of an unpartitioned table before the one that currently
     * comes right before it.
     *
     * @param id the identifier of the row that has to be moved
     * @return {@code true} when the row was moved; or
     * <p>{@code false} when it was already the first one, or when it isn't
     * there
     * @see #moveUp(int, long)
     * @since 1.10
     */
    public boolean moveUp(int id) {
        verifyUnrestricted();
        return moveUp(id, -1);
    }

    /**
     * Moves a row before the one that currently comes right before it in
     * its list.
     * <p>The two rows exchange their ordinals, whatever the distance
     * between them. So issuing {@code moveUp(3, 1)} on the following table:
     * <pre> id | categoryId | ordinal | name
     * ----+------------+---------+-----------------------
     *  2 |          1 |       0 | some article
     *  0 |          1 |       5 | another one
     *  3 |          1 |      10 | boom boom</pre>
     * <p>results into:
     * <pre> id | categoryId | ordinal | name
     * ----+------------+---------+-----------------------
     *  2 |          1 |       0 | some article
     *  3 |          1 |       5 | boom boom
     *  0 |          1 |      10 | another one</pre>
     *
     * @param id          the identifier of the row that has to be moved
     * @param restriction the value of the restricting column that names the
     *                    list, which is ignored when the rows aren't
     *                    partitioned
     * @return {@code true} when the row was moved; or
     * <p>{@code false} when it was already the first one of its list, or
     * when it isn't there
     * @since 1.10
     */
    public boolean moveUp(int id, long restriction) {
        return exchangeWithNeighbour(id, restriction, true);
    }

    /**
     * Moves a row of an unpartitioned table after the one that currently
     * comes right after it.
     *
     * @param id the identifier of the row that has to be moved
     * @return {@code true} when the row was moved; or
     * <p>{@code false} when it was already the last one, or when it isn't
     * there
     * @see #moveDown(int, long)
     * @since 1.10
     */
    public boolean moveDown(int id) {
        verifyUnrestricted();
        return moveDown(id, -1);
    }

    /**
     * Moves a row after the one that currently comes right after it in its
     * list.
     * <p>This is the mirror of {@link #moveUp(int, long)}: the row and the
     * one that follows it exchange their ordinals.
     *
     * @param id          the identifier of the row that has to be moved
     * @param restriction the value of the restricting column that names the
     *                    list, which is ignored when the rows aren't
     *                    partitioned
     * @return {@code true} when the row was moved; or
     * <p>{@code false} when it was already the last one of its list, or
     * when it isn't there
     * @since 1.10
     */
    public boolean moveDown(int id, long restriction) {
        return exchangeWithNeighbour(id, restriction, false);
    }

    /**
     * Renumbers the single list of an unpartitioned table so that its
     * ordinals start at zero and follow each other.
     *
     * @return {@code true} when the rows were renumbered; or
     * <p>{@code false} when there was nothing to renumber
     * @see #tighten(long)
     * @since 1.10
     */
    public boolean tighten() {
        verifyUnrestricted();
        return tighten(-1);
    }

    /**
     * Renumbers the rows of a list so that their ordinals start at zero and
     * follow each other, without changing the order that they were already
     * in.
     * <p>A list with ordinals {@code 0}, {@code 5} and {@code 10} ends up
     * with {@code 0}, {@code 1} and {@code 2}. Nothing depends on the
     * ordinals following each other, so this only tidies up the gaps that
     * removing rows leaves behind, and makes room again when the ordinals
     * have run all the way up to {@link Integer#MAX_VALUE}.
     *
     * @param restriction the value of the restricting column that names the
     *                    list, which is ignored when the rows aren't
     *                    partitioned
     * @return {@code true} when the rows were renumbered; or
     * <p>{@code false} when there was nothing to renumber
     * @since 1.10
     */
    public boolean tighten(long restriction) {
        var result = new boolean[]{false};
        inList(restriction, () -> {
            var ids = identifiersInOrder(restriction);
            if (ids.isEmpty()) {
                return;
            }

            // the rows are first moved out of the way, so that assigning
            // the final ordinals never has two rows carrying the same one,
            // and they're moved by their identifier so that two that ended
            // up equal are given one of their own
            var parking = parkingOrdinal(restriction, ids.size());
            for (var i = 0; i < ids.size(); i++) {
                assign(ids.get(i), parking + i, restriction);
            }
            for (var i = 0; i < ids.size(); i++) {
                assign(ids.get(i), i, restriction);
            }
            result[0] = true;
        });
        return result[0];
    }

    // exchanges the ordinal of a row with the one of the row next to it
    private boolean exchangeWithNeighbour(int id, long restriction, boolean upwards) {
        var result = new boolean[]{false};
        inList(restriction, () -> {
            var ordinal = ordinalOf(id, restriction);
            if (null == ordinal) {
                return;
            }

            var neighbour = neighbourOf(ordinal, restriction, upwards);
            if (null == neighbour) {
                return;
            }
            var neighbourOrdinal = ordinalOf(neighbour, restriction);
            if (null == neighbourOrdinal) {
                return;
            }

            // the two ordinals are exchanged through one that nothing
            // holds, since a uniqueness constraint on them would refuse the
            // moment that both rows carry the same one
            var parking = parkingOrdinal(restriction, 1);
            assign(id, parking, restriction);
            assign(neighbour, ordinal, restriction);
            assign(id, neighbourOrdinal, restriction);
            result[0] = true;
        });
        return result[0];
    }

    // refuses to order anything on a database that doesn't hold on to what
    // a transaction changed: a lock is only a lock for as long as a
    // transaction keeps it, so without transactions the operations of a list
    // would run at the same time, and the several changes that one of them
    // makes could be left half applied
    private void verifyTransactions() {
        if (transactionsVerified_) {
            return;
        }
        var connection = datasource_.getConnection();
        try {
            if (!connection.supportsTransactions()) {
                throw new TransactionsRequiredException(datasource_.getDriver());
            }
            transactionsVerified_ = true;
        } finally {
            connection.close();
        }
    }

    // refuses an operation that doesn't name a list while the rows are
    // partitioned into them, since it would quietly work on the list of a
    // value that nothing uses
    private void verifyUnrestricted() {
        if (restrictColumn_ != null) {
            throw new ListRequiredException(table_, restrictColumn_);
        }
    }

    private static void verifyName(String name, String role) {
        if (null == name) throw new IllegalArgumentException(role + " can't be null");
        if (!name.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("The " + role + " '" + name +
                                               "' isn't usable, use letters, digits and underscores without a leading digit.");
        }
    }

    private ListEntry listEntry(long restriction) {
        return new ListEntry(datasource_, table_.toLowerCase(Locale.ROOT), identifierColumn_.toLowerCase(Locale.ROOT),
            ordinalColumn_.toLowerCase(Locale.ROOT), restrictColumn_ == null ? null : restrictColumn_.toLowerCase(Locale.ROOT),
            restrictColumn_ == null ? -1 : restriction);
    }

    private String listKey(long restriction) {
        return datasource_.getUrl() + "\n" + table_.toLowerCase(Locale.ROOT) + "\n" + (restrictColumn_ == null ? "" : restriction);
    }

    // retrieves the lock of a list and remembers that this thread is going
    // to be inside it or waiting for it
    private static ReentrantLock enterListLock(String key) {
        return LIST_LOCKS.compute(key, (k, held) -> held == null
            ? new HeldList(new ReentrantLock(), 1)
            : new HeldList(held.lock(), held.holders() + 1)).lock();
    }

    // forgets the lock of a list again, taking it away once no thread is
    // inside it or waiting for it anymore
    private static void leaveListLock(String key) {
        LIST_LOCKS.computeIfPresent(key, (k, held) -> held.holders() == 1
            ? null
            : new HeldList(held.lock(), held.holders() - 1));
    }

    // locks every row of a list in a single statement, so there's nothing
    // to acquire in steps and nothing that can slip away in between, and the
    // rows that an operation is about to renumber are exactly the ones that
    // it holds; note that the rows are assigned the ordinal that they
    // already have, which is a change to the database as far as it's
    // concerned, so an update trigger on the table sees it
    private void lockList(long restriction) {
        var column = ordinalColumn_;
        var take = new Update(datasource_)
            .table(table_)
            .fieldCustom(column, column);
        if (restrictColumn_ != null) {
            take.whereParameter(restrictColumn_, "=");
        }
        dbQueryManager_.executeUpdate(take, statement -> bindRestriction(statement, restriction));
    }

    private Integer ordinalOf(int id, long restriction) {
        // the list is part of the lookup, so a row that belongs to another
        // one than the list that is locked is simply not found
        var query = new Select(datasource_)
            .field(ordinalColumn_)
            .from(table_)
            .whereParameter(identifierColumn_, "=");
        if (restrictColumn_ != null) {
            query.whereParameterAnd(restrictColumn_, "=");
        }
        var found = dbQueryManager_.executeGetFirstString(query, statement -> {
            statement.setInt(identifierColumn_, id);
            bindRestriction(statement, restriction);
        });
        return found == null ? null : Integer.valueOf(found);
    }

    private Integer neighbourOf(int ordinal, long restriction, boolean upwards) {
        var query = new Select(datasource_)
            .field(identifierColumn_)
            .from(table_)
            .whereParameter(ordinalColumn_, "boundary", upwards ? "<" : ">")
            .orderBy(ordinalColumn_, upwards ? Select.DESC : Select.ASC)
            .limit(1);
        if (restrictColumn_ != null) {
            query.whereParameterAnd(restrictColumn_, "=");
        }
        var found = dbQueryManager_.executeGetFirstString(query, statement -> {
            statement.setInt("boundary", ordinal);
            bindRestriction(statement, restriction);
        });
        return found == null ? null : Integer.valueOf(found);
    }

    private java.util.List<Integer> identifiersInOrder(long restriction) {
        var query = new Select(datasource_)
            .field(identifierColumn_)
            .from(table_)
            .orderBy(ordinalColumn_, Select.ASC);
        restrict(query);
        var result = new java.util.ArrayList<Integer>();
        dbQueryManager_.executeFetchAll(query, resultSet -> result.add(resultSet.getInt(1)),
            statement -> bindRestriction(statement, restriction));
        return result;
    }

    /**
     * Retrieves the ordinal that a row holds in the database.
     * <p>The column is read with a plain query, without creating a bean and
     * without running its {@code afterRestore} callback, so what the
     * database holds is what answers this.
     *
     * @param id the identifier of the row to look at
     * @return the ordinal of that row; or
     * <p>{@code null} when the row isn't there
     * @since 1.10
     */
    public Integer storedOrdinal(int id) {
        var found = columnOf(id, ordinalColumn_);
        return found == null ? null : Integer.valueOf(found);
    }

    /**
     * Retrieves the value of the restricting column that names the list a
     * row belongs to.
     *
     * @param id the identifier of the row to look at
     * @return the list that the row belongs to; or
     * <p>{@code null} when the row isn't there, or when the rows aren't
     * partitioned
     * @see #storedOrdinal(int)
     * @since 1.10
     */
    public Long storedRestriction(int id) {
        if (null == restrictColumn_) {
            return null;
        }
        // the lists are named by longs everywhere else, so a value that
        // doesn't fit in an int names a list like any other
        var found = columnOf(id, restrictColumn_);
        return found == null ? null : Long.valueOf(found);
    }

    /**
     * Retrieves the list and the ordinal that a row holds, read together so
     * that they describe the same moment.
     * <p>Reading them one after the other can return the list that a row was
     * in together with the ordinal it was given after being moved out of it.
     *
     * @param id the identifier of the row to look at
     * @return the list and the ordinal of that row; or
     * <p>{@code null} when the row isn't there
     * @since 1.10
     */
    public Placement storedPlacement(int id) {
        var query = new Select(datasource_)
            .field(ordinalColumn_)
            .from(table_)
            .whereParameter(identifierColumn_, "=");
        if (restrictColumn_ != null) {
            query.field(restrictColumn_);
        }
        var found = new Placement[]{null};
        dbQueryManager_.executeFetchFirst(query, resultSet -> {
            var ordinal = resultSet.getObject(ordinalColumn_);
            // a row without an ordinal isn't placed anywhere, which isn't
            // the same as being placed at zero
            if (ordinal != null) {
                found[0] = new Placement(restrictColumn_ == null ? -1L : resultSet.getLong(restrictColumn_),
                    ((Number) ordinal).intValue());
            }
        }, statement -> statement.setInt(identifierColumn_, id));
        return found[0];
    }

    /**
     * Where a row sits, which is the list that it belongs to and the
     * ordinal that it holds within it.
     *
     * @since 1.10
     */
    public record Placement(long restriction, int ordinal) {
    }

    private String columnOf(int id, String column) {
        var query = new Select(datasource_)
            .field(column)
            .from(table_)
            .whereParameter(identifierColumn_, "=");
        return dbQueryManager_.executeGetFirstString(query, statement -> statement.setInt(identifierColumn_, id));
    }

    /**
     * Indicates whether a row sits where it's expected to sit.
     * <p>The columns are read with a plain query, without creating a bean
     * and without running its {@code afterRestore} callback, so what the
     * database holds is what answers this.
     *
     * @param id          the identifier of the row to look at
     * @param restriction the list that it's expected to be in, which is
     *                    ignored when the rows aren't partitioned
     * @param ordinal     the ordinal that it's expected to have
     * @return {@code true} when the row sits there; or
     * <p>{@code false} when it doesn't, or when it isn't there at all
     * @since 1.10
     */
    public boolean isPlacedAt(int id, long restriction, int ordinal) {
        var found = ordinalOf(id, restriction);
        return found != null && found == ordinal;
    }

    // retrieves the first of a range of ordinals that nothing holds, which
    // the rows are moved through while they exchange the ones they had; the
    // range sits above the ordinals when there's room for it there, and
    // below them when the top has been reached, so that tightening can still
    // make room in a list whose ordinals run all the way up, while a list
    // that holds both ends of the range at once has no room on either side,
    // which only writers outside of this class can produce, and is reported
    private int parkingOrdinal(long restriction, int count) {
        var highest = boundary(restriction, "MAX");
        if (null == highest) {
            return 0;
        }

        // the range has to stay clear of the ordinals that are there now and
        // of the ones that they're about to be given, since renumbering ends
        // at zero and a row that is still parked there would be in the way
        var above = Math.max(highest, count - 1);
        if (above <= Integer.MAX_VALUE - count) {
            return above + 1;
        }

        var lowest = boundary(restriction, "MIN");
        var below = Math.min(lowest == null ? 0 : lowest, 0);
        if (below >= Integer.MIN_VALUE + count) {
            return below - count;
        }

        throw new OrdinalsOutOfRoomException(table_);
    }

    /**
     * Retrieves the lowest and the highest ordinal of every list that is
     * asked about.
     * <p>Which rows sit at the ends of a list is what says whether they can
     * still be moved, and the lists that a page shows are asked about
     * together instead of one at a time.
     *
     * @param restrictions the lists to read the ends of, which an
     *                     unpartitioned sequence answers for as a single
     *                     list whatever they are
     * @return the lowest and the highest ordinal of every list that holds
     * rows, by the value that names it
     * @since 1.10
     */
    public Map<Long, int[]> bounds(Collection<Long> restrictions) {
        var result = new HashMap<Long, int[]>();
        if (null == restrictions || restrictions.isEmpty()) {
            return result;
        }

        var query = new Select(datasource_)
            .field(lowest())
            .field(highest())
            .from(table_);
        if (restrictColumn_ != null) {
            query.field(restrictColumn_).groupBy(restrictColumn_);
            var first = true;
            for (var ignored : restrictions) {
                if (first) {
                    query.whereParameter(restrictColumn_, "=");
                    first = false;
                } else {
                    query.whereParameterOr(restrictColumn_, "=");
                }
            }
        }

        dbQueryManager_.executeFetchAll(query, resultSet -> {
                // an aggregate over a list that holds nothing is nothing,
                // which a list of its own would otherwise be reported as
                // sitting between zero and zero
                var lowest = resultSet.getObject(1);
                var highest = resultSet.getObject(2);
                if (null == lowest || null == highest) {
                    return;
                }
                result.put(restrictColumn_ == null ? -1L : resultSet.getLong(3),
                    new int[]{resultSet.getInt(1), resultSet.getInt(2)});
            },
            statement -> {
                if (restrictColumn_ != null) {
                    // the parameters of a statement are counted from one
                    var i = 1;
                    for (var restriction : restrictions) {
                        statement.setLong(i++, restriction);
                    }
                }
            });
        return result;
    }

    // the builder writes fields, so an aggregate of one is the expression
    // that names it, the way the query managers of the framework write theirs
    private String lowest() {
        return "MIN(" + ordinalColumn_ + ")";
    }

    private String highest() {
        return "MAX(" + ordinalColumn_ + ")";
    }

    private Integer boundary(long restriction, String aggregate) {
        var query = new Select(datasource_)
            .field(aggregate + "(" + ordinalColumn_ + ")")
            .from(table_);
        restrict(query);
        var found = dbQueryManager_.executeGetFirstString(query, statement -> bindRestriction(statement, restriction));
        return found == null ? null : Integer.valueOf(found);
    }

    private void assign(int id, int ordinal, long restriction) {
        var update = new Update(datasource_)
            .table(table_)
            .fieldParameter(ordinalColumn_)
            .whereParameter(identifierColumn_, "=");
        if (restrictColumn_ != null) {
            update.whereParameterAnd(restrictColumn_, "=");
        }
        var changed = dbQueryManager_.executeUpdate(update, statement -> {
            statement.setInt(ordinalColumn_, ordinal);
            statement.setInt(identifierColumn_, id);
            bindRestriction(statement, restriction);
        });
        // a database that reports the rows it changed reports nothing when
        // the ordinal already was the one that it was given, so the row is
        // looked up rather than the count being trusted
        if (0 == changed && null == ordinalOf(id, restriction)) {
            throw new RowNotInListException(table_, id, restriction);
        }
    }

    private void restrict(Select query) {
        if (restrictColumn_ != null) {
            query.whereParameter(restrictColumn_, "=");
        }
    }

    private void bindRestriction(rife.database.DbPreparedStatement statement, long restriction) {
        if (restrictColumn_ != null) {
            statement.setLong(restrictColumn_, restriction);
        }
    }

}
