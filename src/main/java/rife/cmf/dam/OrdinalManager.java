/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.database.*;

import rife.database.queries.Select;
import rife.database.queries.Update;
import rife.datastructures.EnumClass;
import rife.tools.ExceptionUtils;
import rife.tools.InnerClassException;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * This class makes it possible to easily manage an integer ordinal column
 * that is typically used to determine the order of the rows in a specific
 * table.
 * <p>The basic version manages the ordinals for the entire table, but it's
 * also possible to create an {@code OrdinalManager} that uses several
 * independent ranges of ordinals according to a restricting integer column.
 * <p>For example, consider the following '{@code article}' table:
 * <pre>id          INT
 * categoryId  INT
 * ordinal     INT
 * name        VARCHAR(30)</pre>
 * <p>with the following rows:
 * <pre> id | categoryId | ordinal | name
 * ----+------------+---------+-----------------------
 *  2 |          1 |       0 | some article
 *  0 |          1 |       1 | another one
 *  3 |          1 |       2 | boom boom
 *  1 |          2 |       0 | this is yet an article
 *  5 |          2 |       1 | an article for you
 *  4 |          3 |       0 | our latest article
 *  6 |          3 |       1 | important one</pre>
 * <p>You can clearly see three independent {@code ordinal} ranges
 * according to the {@code categoryId} column.
 * <p>The {@code OrdinalManager} allows you to easily change the order of
 * the articles by moving them up and down with the provided methods: {@link
 * #move(Direction, int) move}, {@link #up(int) up} and {@link #down(int) down}.
 * It's also possible to do more complex manipulations by using the lower
 * level methods: {@link #free(int) free}, {@link #update(int, int) update},
 * {@link #tighten() tighten} and {@link #obtainInsertOrdinal()
 * obtainInsertOrdinal}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class OrdinalManager implements Cloneable {
    /**
     * @see Direction#UP
     */
    public static final Direction UP = Direction.UP;
    /**
     * @see Direction#DOWN
     */
    public static final Direction DOWN = Direction.DOWN;

    private final Datasource datasource_;
    private final DbQueryManager dbQueryManager_;
    private final String table_;
    private final String ordinalColumn_;
    private String restrictColumn_;

    private final Update freeMoveOrdinal_;
    private final Select getFinalOrdinal_;
    private final Select getOrdinals_;
    private Select getFinalOrdinalRestricted_ = null;
    private Update freeMoveOrdinalRestricted_ = null;
    private Select getOrdinalsRestricted_ = null;

    /**
     * Creates a new {@code OrdinalManager} that manages ordinals
     * globally for the specified table.
     *
     * @param datasource    the datasource where the table is accessible
     * @param table         the name of the table that will be managed
     * @param ordinalColumn the name of the column that contains the integer
     *                      ordinals
     * @since 1.0
     */
    public OrdinalManager(Datasource datasource, String table, String ordinalColumn) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null");
        if (null == table) throw new IllegalArgumentException("table can't be null");
        if (null == ordinalColumn) throw new IllegalArgumentException("ordinalColumn can't be null");

        datasource_ = datasource;

        dbQueryManager_ = new DbQueryManager(datasource);

        table_ = table;
        ordinalColumn_ = ordinalColumn;
        restrictColumn_ = null;

        getFinalOrdinal_ = new Select(datasource);
        getFinalOrdinal_
            .field(ordinalColumn_)
            .from(table_)
            .orderBy(ordinalColumn_, Select.DESC);

        freeMoveOrdinal_ = new Update(datasource);
        freeMoveOrdinal_
            .table(table_)
            .whereParameter(ordinalColumn_, "current", "=")
            .fieldParameter(ordinalColumn_, "new");

        getOrdinals_ = new Select(datasource);
        getOrdinals_
            .field(ordinalColumn_)
            .from(table_)
            .orderBy(ordinalColumn_, Select.ASC);
    }

    /**
     * Creates a new {@code OrdinalManager} that manages ordinals for the
     * specified table in independent ranges according to a restricting
     * integer column.
     *
     * @param datasource     the datasource where the table is accessible
     * @param table          the name of the table that will be managed
     * @param ordinalColumn  the name of the column that contains the integer
     *                       ordinals
     * @param restrictColumn the name of the column whose values will
     *                       partition the ordinals in independent ranges
     * @since 1.0
     */
    public OrdinalManager(Datasource datasource, String table, String ordinalColumn, String restrictColumn) {
        this(datasource, table, ordinalColumn);

        if (null == restrictColumn) throw new IllegalArgumentException("restrictColumn can't be null");

        restrictColumn_ = restrictColumn;

        getFinalOrdinalRestricted_ = new Select(datasource_);
        getFinalOrdinalRestricted_
            .field(ordinalColumn_)
            .from(table_)
            .whereParameter(restrictColumn_, "=")
            .orderBy(ordinalColumn_, Select.DESC);

        freeMoveOrdinalRestricted_ = new Update(datasource_);
        freeMoveOrdinalRestricted_
            .table(table_)
            .whereParameter(restrictColumn_, "=")
            .whereParameterAnd(ordinalColumn_, "current", "=")
            .fieldParameter(ordinalColumn_, "new");

        getOrdinalsRestricted_ = new Select(datasource_);
        getOrdinalsRestricted_
            .field(ordinalColumn_)
            .from(table_)
            .whereParameter(restrictColumn_, "=")
            .orderBy(ordinalColumn_, Select.ASC);

    }

    /**
     * Retrieves the name of the table of this {@code OrdinalManager}.
     *
     * @return the name of the table
     * @since 1.0
     */
    public String getTable() {
        return table_;
    }

    /**
     * Retrieves the name of the ordinal column.
     *
     * @return the name of the ordinal column
     * @since 1.0
     */
    public String getOrdinalColumn() {
        return ordinalColumn_;
    }

    /**
     * Retrieves the name of the restricting column.
     *
     * @return the name of the restricting column; or
     * <p>{@code null} if this {@code OrdinalManager} manages the
     * table globally
     * @since 1.0
     */
    public String getRestrictColumn() {
        return restrictColumn_;
    }

    /**
     * Moves the position of a row with a specific ordinal within the entire
     * table.
     *
     * @param direction the direction in which to move: {@link
     *                  OrdinalManager#UP OrdinalManager.UP} or {@link OrdinalManager#DOWN
     *                  OrdinalManager.DOWN}
     * @param ordinal   the ordinal of the row that has to be moved
     * @return {@code true} if the move was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #move(Direction, long, int)
     * @since 1.0
     */
    public boolean move(Direction direction, final int ordinal) {
        if (direction == UP) {
            return up(ordinal);
        } else if (direction == DOWN) {
            return down(ordinal);
        }

        return false;
    }

    /**
     * Moves the position of a row with a specific ordinal within the range
     * restricted by the provided ID.
     *
     * @param direction  the direction in which to move: {@link
     *                   OrdinalManager#UP OrdinalManager.UP} or {@link OrdinalManager#DOWN
     *                   OrdinalManager.DOWN}
     * @param restrictId the restriction ID value
     * @param ordinal    the ordinal of the row that has to be moved
     * @return {@code true} if the move was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #move(Direction, int)
     * @since 1.0
     */
    public boolean move(Direction direction, final long restrictId, final int ordinal) {
        if (direction == UP) {
            return up(restrictId, ordinal);
        } else if (direction == DOWN) {
            return down(restrictId, ordinal);
        }

        return false;
    }

    /**
     * Moves a row with a specific ordinal upwards within the entire table.
     *
     * @param ordinal the ordinal of the row that has to be moved
     * @return {@code true} if the move was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #up(long, int)
     * @since 1.0
     */
    public boolean up(final int ordinal) {
        Boolean result = dbQueryManager_.inTransaction(new DbTransactionUser() {
            public Boolean useTransaction()
            throws InnerClassException {
                if (!free(ordinal - 1)) {
                    rollback();
                }
                if (!update(ordinal + 1, ordinal - 1)) {
                    rollback();
                }
                if (!tighten()) {
                    rollback();
                }

                return true;
            }
        });

        return null != result && result;
    }

    /**
     * Moves a row with a specific ordinal upwards within the range restricted
     * by the provided ID.
     *
     * @param restrictId the restriction ID value
     * @param ordinal    the ordinal of the row that has to be moved
     * @return {@code true} if the move was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #up(int)
     * @since 1.0
     */
    public boolean up(final long restrictId, final int ordinal) {
        Boolean result = dbQueryManager_.inTransaction(new DbTransactionUser() {
            public Boolean useTransaction()
            throws InnerClassException {
                if (!free(restrictId, ordinal - 1)) {
                    rollback();
                }
                if (!update(restrictId, ordinal + 1, ordinal - 1)) {
                    rollback();
                }
                if (!tighten(restrictId)) {
                    rollback();
                }

                return true;
            }
        });

        return null != result && result;
    }

    /**
     * Moves a row with a specific ordinal downwards within the entire table.
     *
     * @param ordinal the ordinal of the row that has to be moved
     * @return {@code true} if the move was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #down(long, int)
     * @since 1.0
     */
    public boolean down(final int ordinal) {
        Boolean result = dbQueryManager_.inTransaction(new DbTransactionUser() {
            public Boolean useTransaction()
            throws InnerClassException {
                if (!free(ordinal + 2)) {
                    rollback();
                }
                if (!update(ordinal, ordinal + 2)) {
                    rollback();
                }
                if (!tighten()) {
                    rollback();
                }

                return true;
            }
        });

        return null != result && result;
    }

    /**
     * Moves a row with a specific ordinal downwards within the range
     * restricted by the provided ID.
     *
     * @param restrictId the restriction ID value
     * @param ordinal    the ordinal of the row that has to be moved
     * @return {@code true} if the move was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #down(int)
     * @since 1.0
     */
    public boolean down(final long restrictId, final int ordinal) {
        Boolean result = dbQueryManager_.inTransaction(new DbTransactionUser() {
            public Boolean useTransaction()
            throws InnerClassException {
                if (!free(restrictId, ordinal + 2)) {
                    rollback();
                }
                if (!update(restrictId, ordinal, ordinal + 2)) {
                    rollback();
                }
                if (!tighten(restrictId)) {
                    rollback();
                }

                return true;
            }
        });

        return null != result && result;
    }

    /**
     * Moves a row with a specific ordinal to the location of another ordinal
     * within the entire table.
     *
     * @param fromOrdinal the ordinal of the row that has to be moved
     * @param toOrdinal   the ordinal of the row where the from row has will be
     *                    put above
     * @return {@code true} if the move was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #down(int)
     * @since 1.0
     */
    public boolean move(final int fromOrdinal, final int toOrdinal) {
        Boolean result = dbQueryManager_.inTransaction(new DbTransactionUser() {
            public Boolean useTransaction()
            throws InnerClassException {
                if (!free(toOrdinal)) {
                    rollback();
                }
                int real_from = fromOrdinal;
                if (toOrdinal < fromOrdinal) {
                    real_from++;
                }
                if (!update(real_from, toOrdinal)) {
                    rollback();
                }
                if (!tighten()) {
                    rollback();
                }

                return true;
            }
        });

        return null != result && result;
    }

    /**
     * Moves a row with a specific ordinal to the location of another ordinal
     * within the range restricted by the provided ID.
     *
     * @param restrictId  the restriction ID value
     * @param fromOrdinal the ordinal of the row that has to be moved
     * @param toOrdinal   the ordinal of the row where the from row has will be
     *                    put above
     * @return {@code true} if the move was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #down(int)
     * @since 1.0
     */
    public boolean move(final long restrictId, final int fromOrdinal, final int toOrdinal) {
        if (fromOrdinal == toOrdinal) {
            return true;
        }

        Boolean result = dbQueryManager_.inTransaction(new DbTransactionUser() {
            public Boolean useTransaction()
            throws InnerClassException {
                if (!free(restrictId, toOrdinal)) {
                    rollback();
                }
                int real_from = fromOrdinal;
                if (toOrdinal < fromOrdinal) {
                    real_from++;
                }
                if (!update(restrictId, real_from, toOrdinal)) {
                    rollback();
                }
                if (!tighten(restrictId)) {
                    rollback();
                }

                return true;
            }
        });

        return null != result && result;
    }

    /**
     * Frees up a slot for the specified ordinal within the entire table.,
     * this is done by incrementing everything after it by 1 to make space.
     * <p>So for example issuing the method {@code free(1)} on the
     * following table:
     * <pre> id | ordinal | name
     * ----+---------+-----------------------
     *  2 |       0 | some article
     *  0 |       1 | another one
     *  1 |       2 | this is yet an article</pre>
     * <p>will result in:
     * <pre> id | ordinal | name
     * ----+---------+-----------------------
     *  2 |       0 | some article
     *  0 |       2 | another one
     *  1 |       3 | this is yet an article</pre>
     *
     * @param ordinal an integer representing the ordinal to free
     * @return {@code true} if the slot was freed up correctly; or
     * <p>{@code false} if the operation wasn't possible
     * @see #free(long, int)
     * @since 1.0
     */
    public boolean free(final int ordinal) {
        Boolean result = dbQueryManager_.inTransaction(() -> {
            if (ordinal < 0) {
                return false;
            }

            int last_ordinal = dbQueryManager_.executeGetFirstInt(getFinalOrdinal_);

            for (int i = last_ordinal; i >= ordinal; i--) {
                final int current_ordinal = i;

                dbQueryManager_.executeUpdate(freeMoveOrdinal_, s -> s
                    .setInt("current", current_ordinal)
                    .setInt("new", current_ordinal + 1));
            }

            return true;
        });

        return null != result && result;
    }

    /**
     * Frees up a slot for the specified ordinal within the range restricted
     * by the provided ID, this is done by incrementing everything after it by
     * 1 to make space.
     * <p>So for example issuing the method {@code free(2, 0)} on the
     * following table:
     * <pre> id | categoryId | ordinal | name
     * ----+------------+---------+-----------------------
     *  2 |          1 |       0 | some article
     *  0 |          1 |       1 | another one
     *  3 |          1 |       2 | boom boom
     *  1 |          2 |       0 | this is yet an article
     *  5 |          2 |       1 | an article for you
     *  4 |          3 |       0 | our latest article
     *  6 |          3 |       1 | important one</pre>
     * <p>will result into:
     * <pre> id | categoryId | ordinal | name
     * ----+------------+---------+-----------------------
     *  2 |          1 |       0 | some article
     *  0 |          1 |       1 | another one
     *  3 |          1 |       2 | boom boom
     *  1 |          2 |       1 | this is yet an article
     *  5 |          2 |       2 | an article for you
     *  4 |          3 |       0 | our latest article
     *  6 |          3 |       1 | important one</pre>
     *
     * @param restrictId the id by which to restrict with
     * @param ordinal    an int representation the ordinal to free
     * @return {@code true} if the slot was freed up correctly; or
     * <p>{@code false} if the operation wasn't possible
     * @see #free(int)
     * @since 1.0
     */
    public boolean free(final long restrictId, final int ordinal) {
        Boolean result = dbQueryManager_.inTransaction(() -> {
            if (ordinal < 0) {
                return false;
            }

            int last_ordinal = dbQueryManager_.executeGetFirstInt(getFinalOrdinalRestricted_, s ->
                s.setLong(restrictColumn_, restrictId));

            for (int i = last_ordinal; i >= ordinal; i--) {
                final int current_ordinal = i;

                dbQueryManager_.executeUpdate(freeMoveOrdinalRestricted_, s ->
                    s.setLong(restrictColumn_, restrictId)
                        .setInt("current", current_ordinal)
                        .setInt("new", current_ordinal + 1));
            }

            return true;
        });

        return null != result && result;
    }

    /**
     * Changes the ordinal of a certain row to a new value.
     * <p>This simply updates the value of the ordinal column and doesn't
     * execute any other logic.
     *
     * @param currentOrdinal the ordinal of the row that has to be updated
     * @param newOrdinal     the new ordinal value
     * @return {@code true} if the update was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #update(long, int, int)
     * @since 1.0
     */
    public boolean update(final int currentOrdinal, final int newOrdinal) {
        Boolean result = dbQueryManager_.inTransaction(() -> {
            return 0 != dbQueryManager_.executeUpdate(freeMoveOrdinal_, s ->
                s.setInt("current", currentOrdinal)
                    .setInt("new", newOrdinal));
        });

        return null != result && result;
    }

    /**
     * Changes the ordinal of a certain row with a specific restriction ID to
     * a new value.
     * <p>This simply updates the value of the ordinal column and doesn't
     * execute any other logic.
     *
     * @param restrictId     the id by which to restrict with
     * @param currentOrdinal the ordinal of the row that has to be updated
     * @param newOrdinal     the new ordinal value
     * @return {@code true} if the update was executed successfully; or
     * <p>{@code false} if this wasn't the case
     * @see #update(int, int)
     * @since 1.0
     */
    public boolean update(final long restrictId, final int currentOrdinal, final int newOrdinal) {
        Boolean result = dbQueryManager_.inTransaction(() -> {
            return 0 != dbQueryManager_.executeUpdate(freeMoveOrdinalRestricted_, s ->
                s.setLong(restrictColumn_, restrictId)
                    .setInt("current", currentOrdinal)
                    .setInt("new", newOrdinal));
        });

        return null != result && result;
    }

    /**
     * Tightens the series of ordinal within the entire table so that no
     * spaces are present in between the ordinals.
     * <p>So for example issuing the method {@code tighten()} on the
     * following table:
     * <pre> id | ordinal | name
     * ----+---------+-----------------------
     *  2 |       0 | some article
     *  0 |       2 | another one
     *  1 |       5 | this is yet an article</pre>
     * <p>will result in:
     * <pre> id | ordinal | name
     * ----+---------+-----------------------
     *  2 |       0 | some article
     *  0 |       1 | another one
     *  1 |       2 | this is yet an article</pre>
     *
     * @return {@code true} if the tightening was executed correctly; or
     * <p>{@code false} if the operation wasn't possible
     * @see #tighten(long)
     * @since 1.0
     */
    public boolean tighten() {
        dbQueryManager_.inTransaction(() -> dbQueryManager_.executeQuery(getOrdinals_, new TightenResultSetHandler()));

        return true;
    }

    /**
     * Tightens the series of ordinal within the range restricted by the
     * provided ID so that no spaces are present in between the ordinals.
     * <p>So for example issuing the method {@code tighten(2)} on the
     * following table:
     * <pre> id | categoryId | ordinal | name
     * ----+------------+---------+-----------------------
     *  2 |          1 |       1 | some article
     *  0 |          1 |       2 | another one
     *  3 |          1 |       7 | boom boom
     *  1 |          2 |       4 | this is yet an article
     *  5 |          2 |       8 | an article for you
     *  4 |          3 |       4 | our latest article
     *  6 |          3 |       5 | important one</pre>
     * <p>will result in:
     * <pre> id | categoryId | ordinal | name
     * ----+------------+---------+-----------------------
     *  2 |          1 |       1 | some article
     *  0 |          1 |       2 | another one
     *  3 |          1 |       7 | boom boom
     *  1 |          2 |       0 | this is yet an article
     *  5 |          2 |       1 | an article for you
     *  4 |          3 |       4 | our latest article
     *  6 |          3 |       5 | important one</pre>
     *
     * @param restrictId the id by which to restrict with
     * @return {@code true} if the tightening was executed correctly; or
     * <p>{@code false} if the operation wasn't possible
     * @see #tighten()
     * @since 1.0
     */
    public boolean tighten(final long restrictId) {
        final TightenResultSetHandlerRestricted handler = new TightenResultSetHandlerRestricted(restrictId) {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setLong(restrictColumn_, restrictId);
            }
        };

        dbQueryManager_.inTransaction(() -> dbQueryManager_.executeQuery(getOrdinalsRestricted_, handler));

        return handler.isTightened();
    }

    /**
     * Returns the next freely available ordinal that can be used to insert a
     * new row behind all the other rows in the entire table.
     * <p>So for example issuing the method {@code obtainInsertOrdinal()}
     * on the following table:
     * <pre> id | ordinal | name
     * ----+---------+-----------------------
     *  2 |       0 | some article
     *  0 |       1 | another one
     *  1 |       2 | this is yet an article</pre>
     * <p>Will return the value {@code 3}.
     *
     * @return the requested ordinal; or
     * <p>{@code 0} if no ordinals are present within the table yet
     * @see #obtainInsertOrdinal(long)
     * @since 1.0
     */
    public int obtainInsertOrdinal() {
        // return next ordinal to use
        // +1 because mGetFinalOrdinal returns the last ordinal
        // in the db series
        return dbQueryManager_.executeGetFirstInt(getFinalOrdinal_) + 1;
    }

    /**
     * Returns the next freely available ordinal that can be used to insert a
     * new row behind all the other rows in the range restricted by the
     * provided ID.
     * <p>So for example issuing the method
     * {@code obtainInsertOrdinal(3)} on the following table:
     * <pre> id | categoryId | ordinal | name
     * ----+------------+---------+-----------------------
     *  2 |          1 |       0 | some article
     *  0 |          1 |       1 | another one
     *  3 |          1 |       2 | boom boom
     *  1 |          2 |       0 | this is yet an article
     *  5 |          2 |       1 | an article for you
     *  4 |          3 |       0 | our latest article
     *  6 |          3 |       1 | important one</pre>
     * <p>Will return the value {@code 2}.
     *
     * @param restrictId the id by which to restrict with
     * @return the requested ordinal; or
     * <p>{@code 0} if no ordinals are present within the range yet
     * @see #obtainInsertOrdinal()
     * @since 1.0
     */
    public int obtainInsertOrdinal(final long restrictId) {
        // return next ordinal to use
        int ordinal = dbQueryManager_.executeGetFirstInt(getFinalOrdinalRestricted_, s ->
            s.setLong(restrictColumn_, restrictId));

        return ordinal + 1;
    }

    /**
     * Simply clones the instance with the default clone method. This creates
     * a shallow copy of all fields and the clone will in fact just be another
     * reference to the same underlying data. The independence of each cloned
     * instance is consciously not respected since they rely on resources that
     * can't be cloned.
     *
     * @since 1.0
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.cmf").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }

    private class TightenResultSetHandler extends DbPreparedStatementHandler {
        private int mCount = 0;

        public TightenResultSetHandler() {
        }

        public Object concludeResults(DbResultSet resultSet)
        throws SQLException {
            while (resultSet.next()) {
                int ordinal = resultSet.getInt(ordinalColumn_);
                if (ordinal != mCount) {
                    update(ordinal, mCount);
                }
                mCount++;
            }

            return null;
        }
    }

    private class TightenResultSetHandlerRestricted extends DbPreparedStatementHandler {
        private int mCount = 0;
        private long mRestrictId = -1;

        private boolean mTightened = false;

        public TightenResultSetHandlerRestricted(long restrictId) {
            mRestrictId = restrictId;
        }

        public Object concludeResults(DbResultSet resultset)
        throws SQLException {
            while (resultset.next()) {
                mTightened = true;

                int ordinal = resultset.getInt(ordinalColumn_);
                if (ordinal != mCount) {
                    update(mRestrictId, ordinal, mCount);
                }
                mCount++;
            }

            return null;
        }

        public boolean isTightened() {
            return mTightened;
        }
    }

    public static class Direction extends EnumClass<String> {
        /**
         * Has to be used to indicate an upwards direction for the {@link
         * #move(Direction, int) move} method.
         */
        public static final Direction UP = new Direction("up");
        /**
         * Has to be used to indicate a downwards direction for the {@link
         * #move(Direction, int) move} method.
         */
        public static final Direction DOWN = new Direction("down");

        private Direction(String identifier) {
            super(identifier);
        }

        public static Direction getDirection(String name) {
            return getMember(Direction.class, name);
        }
    }
}

