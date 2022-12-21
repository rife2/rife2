/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.RollbackException;
import rife.database.exceptions.RowProcessorErrorException;
import rife.database.queries.Query;
import rife.database.queries.ReadQuery;
import rife.tools.ExceptionUtils;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;
import rife.tools.ReaderUser;
import rife.tools.exceptions.ControlFlowRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is a convenience class to make it easy to control the queries that
 * handle the retrieval, storage, update and removal of data in a database.
 * All queries will be executed in a connection of the <code>Datasource</code>
 * that's provided to the constructor of the <code>DbQueryManager</code>.
 * <p>A collection of convenience methods have been provided to quickly
 * execute queries in a variety of manners without having to worry about the
 * logic behind it or having to remember to close the queries at the
 * appropriate moment. These methods optionally interact with the
 * <code>DbPreparedStatementHandler</code> and <code>DbResultSetHandler</code>
 * classes to make it possible to fully customize the executed queries. The
 * following categories of worry-free methods exist:
 * <ul>
 * <li>{@linkplain #executeUpdate(Query) execute an update query directly}
 * <li>{@linkplain #executeUpdate(Query, PreparedStatementHandler) execute a
 * customizable update query}
 * <li>{@linkplain #executeQuery(ReadQuery, PreparedStatementHandler) execute a
 * customizable select query}
 * <li>{@linkplain #executeHasResultRows(ReadQuery, PreparedStatementHandler)
 * check the result rows of a customizable select query}
 * <li>{@linkplain #executeGetFirstString(ReadQuery, PreparedStatementHandler)
 * obtain the first value of a customizable select query}
 * <li>{@linkplain
 * #executeFetchFirst(ReadQuery, RowProcessor, PreparedStatementHandler) fetch
 * the first row of a customizable select query}
 * <li>{@linkplain
 * #executeFetchFirstBean(ReadQuery, Class, PreparedStatementHandler) fetch the
 * first bean of a customizable select query}
 * <li>{@linkplain
 * #executeFetchAll(ReadQuery, RowProcessor, PreparedStatementHandler) fetch
 * all rows of a customizable select query}
 * <li>{@linkplain
 * #executeFetchAllBeans(ReadQuery, Class, PreparedStatementHandler) fetch all
 * beans of a customizable select query}
 * </ul>
 * <p>Lower-level methods are also available for the sake of repetitive
 * code-reduction. To obtain execute regular statements directly,
 * use the {@link #executeQuery(ReadQuery) executeQuery} method.
 * <p>Finally, <code>since DbStatement</code> and
 * <code>DbPreparedStatement</code> instances preserve a reference to their
 * result set, it's easy to iterate over the rows of a result set with the
 * {@link #fetch(ResultSet, RowProcessor) fetch} or {@link
 * #fetchAll(ResultSet, RowProcessor) fetchAll} methods.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.database.DbPreparedStatement
 * @see rife.database.DbStatement
 * @see rife.database.DbRowProcessor
 * @see rife.database.DbPreparedStatementHandler
 * @see rife.database.DbResultSetHandler
 * @see rife.database.DbConnectionUser
 * @since 1.0
 */
public class DbQueryManager implements Cloneable {
    private final Datasource datasource_;

    /**
     * Instantiates a new <code>DbQueryManager</code> object and ties it to
     * the provided datasource.
     *
     * @param datasource the datasource that will be used to obtain database
     *                   connections from
     * @since 1.0
     */
    public DbQueryManager(Datasource datasource) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        datasource_ = datasource;
    }

    /**
     * Safely and quickly executes an update statement. It relies on the
     * wrapped {@link DbStatement#executeUpdate(String)} method, but also
     * automatically closes the statement after its execution.
     * <p>This method is typically used in situations where one static update
     * query needs to be executed without any parametrization or other
     * processing.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * int count = manager.executeUpdate("INSERT INTO person (name) VALUES ('me')");</pre>
     *
     * @param sql the sql query that has to be executed
     * @return the row count for the executed query
     * @throws DatabaseException see {@link
     *                           DbStatement#executeUpdate(String)}
     * @see DbStatement#executeUpdate(String)
     * @see #executeUpdate(Query)
     * @since 1.0
     */
    public int executeUpdate(String sql)
    throws DatabaseException {
        if (null == sql) throw new IllegalArgumentException("sql can't be null.");

        var connection = getConnection();
        try {
            var statement = connection.createStatement();
            try {
                return statement.executeUpdate(sql);
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly executes an update statement. It relies on the
     * wrapped {@link DbStatement#executeUpdate(Query)} method, but also
     * automatically closes the statement after its execution.
     * <p>This method is typically used in situations where one static update
     * query needs to be executed without any parametrization or other
     * processing.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Insert insert = new Insert(datasource);
     * insert.into("person").field("name", "me");
     * int count = manager.executeUpdate(insert);</pre>
     *
     * @param query the query builder instance that needs to be executed
     * @return the row count for the executed query
     * @throws DatabaseException see {@link
     *                           DbStatement#executeUpdate(Query)}
     * @see DbStatement#executeUpdate(Query)
     * @see #executeUpdate(String)
     * @since 1.0
     */
    public int executeUpdate(Query query)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var connection = getConnection();
        try {
            var statement = connection.createStatement();
            try {
                return statement.executeUpdate(query);
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    private DbPreparedStatement getPreparedStatement(Query query, DbResultSetHandler handler, DbConnection connection) {
        return datasource_.getCapabilitiesCompensator().getCapablePreparedStatement(query, handler, connection);
    }

    private void executeQuery(DbPreparedStatement statement, DbPreparedStatementHandler handler) {
        if (null == handler) {
            statement.executeQuery();
        } else {
            handler.performQuery(statement);
        }
    }

    private DbResultSet getResultSet(DbPreparedStatement statement) {
        return datasource_.getCapabilitiesCompensator().getCapableResultSet(statement);
    }

    private static DbPreparedStatementHandler ensureFullPreparedStatementHandler(PreparedStatementHandler handler) {
        DbPreparedStatementHandler full_handler = null;
        if (handler != null) {
            if (handler instanceof DbPreparedStatementHandler h) {
                full_handler = h;
            } else {
                full_handler = new DbPreparedStatementHandler<>() {
                    public void setParameters(DbPreparedStatement statement) {
                        handler.setParameters(statement);
                    }
                };
            }
        }
        return full_handler;
    }

    /**
     * Safely execute an update statement. It relies on the wrapped {@link
     * DbPreparedStatement#executeUpdate()} method, but also automatically
     * closes the statement after its execution and allows customization of
     * the prepared statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>This method is typically used when you need to fully customize a
     * query at runtime, but still want to benefit of a safety net that
     * ensures that the allocated statement will be closed.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * var insert = new Insert(datasource);
     * insert.into("person").fieldParameter("name");
     * final String name = "me";
     * int count = manager.executeUpdate(insert,
     *     statement -> statement.setString("name", name));
     * </pre>
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the row count for the executed query
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeUpdate()}
     * @see DbPreparedStatement#executeUpdate()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public int executeUpdate(Query query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                if (null == full_handler) {
                    return statement.executeUpdate();
                }

                return full_handler.performUpdate(statement);
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    private boolean executeHasResultRows(DbPreparedStatement statement, DbPreparedStatementHandler handler) {
        executeQuery(statement, handler);

        return getResultSet(statement).hasResultRows();

    }

    /**
     * Safely and quickly verifies if a select query returns any rows. It
     * relies on the wrapped {@link DbResultSet#hasResultRows()} method, but
     * also automatically closes the statement after its execution.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person");
     * boolean result = manager.executeHasResultRows(select);
     * </pre>
     *
     * @param query the query builder instance that needs to be executed
     * @return <code>true</code> when rows were returned by the query; or
     * <p><code>false</code> otherwise
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#hasResultRows()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#hasResultRows()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public boolean executeHasResultRows(ReadQuery query)
    throws DatabaseException {
        return executeHasResultRows(query, null);
    }

    /**
     * Safely verifies if a customizable select query returns any rows. It
     * relies on the wrapped {@link DbResultSet#hasResultRows()} method, but
     * also automatically closes the statement after its execution and allows
     * customization of the prepared statement through an optional instance of
     * {@link PreparedStatementHandler}.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * var select = new Select(datasource);
     * select.from("person").whereParameter("name", "=");
     * final String name = "you";
     * boolean result = manager.executeHasResultRows(select,
     *     statement -> statement.setString("name", name));
     * </pre>
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution
     * @return <code>true</code> when rows were returned by the query; or
     * <p><code>false</code> otherwise
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#hasResultRows()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#hasResultRows()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public boolean executeHasResultRows(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                var result = executeHasResultRows(statement, full_handler);

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>String</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstString()} method, but also automatically closes the
     * statement after its execution.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.field("name").from("person");
     * String result = manager.executeGetFirstString(select);
     * </pre>
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>String</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstString()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstString()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public String executeGetFirstString(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstString(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>String</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstString()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.field("first").from("person").whereParameter("last", "=");
     * final String last = "Smith";
     * String result = manager.executeGetFirstString(select,
     *     statement -> statement.setString("name", name));
     * </pre>
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>String</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstString()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstString()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public String executeGetFirstString(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                String result = null;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstString();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>boolean</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstBoolean()} method, but also automatically closes
     * the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>boolean</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstBoolean()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstBoolean()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public boolean executeGetFirstBoolean(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstBoolean(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>boolean</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstBoolean()} method, but also automatically closes
     * the statement after its execution and allows customization of the
     * prepared statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>boolean</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstBoolean()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstBoolean()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public boolean executeGetFirstBoolean(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                var result = false;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstBoolean();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>byte</code> from
     * the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstByte()} method, but also automatically closes the
     * statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>byte</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstByte()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstByte()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public byte executeGetFirstByte(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstByte(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>byte</code> from the results
     * of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstByte()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>byte</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstByte()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstByte()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public byte executeGetFirstByte(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                byte result = -1;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstByte();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>short</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstShort()} method, but also automatically closes the
     * statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>short</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstShort()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstShort()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public short executeGetFirstShort(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstShort(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>short</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstShort()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>short</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstShort()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstShort()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public short executeGetFirstShort(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                short result = -1;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstShort();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>int</code> from
     * the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstInt()} method, but also automatically closes the
     * statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>int</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link DbResultSet#getFirstInt()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstInt()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public int executeGetFirstInt(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstInt(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>int</code> from the results
     * of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstInt()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>int</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link DbResultSet#getFirstInt()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstInt()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public int executeGetFirstInt(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                var result = -1;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstInt();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>long</code> from
     * the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstLong()} method, but also automatically closes the
     * statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>long</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstLong()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstLong()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public long executeGetFirstLong(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstLong(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>long</code> from the results
     * of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstLong()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>long</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstLong()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstLong()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public long executeGetFirstLong(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                long result = -1;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstLong();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>float</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstFloat()} method, but also automatically closes the
     * statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>float</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstFloat()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstFloat()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public float executeGetFirstFloat(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstFloat(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>float</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstFloat()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>float</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstFloat()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstFloat()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public float executeGetFirstFloat(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                float result = -1;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstFloat();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>double</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstDouble()} method, but also automatically closes the
     * statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>double</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstDouble()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstDouble()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public double executeGetFirstDouble(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstDouble(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>double</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstDouble()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>double</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstDouble()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstDouble()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public double executeGetFirstDouble(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                double result = -1;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstDouble();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a <code>byte</code>
     * array from the results of a select query. It relies on the wrapped
     * {@link DbResultSet#getFirstBytes()} method, but also automatically
     * closes the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first <code>byte</code> array in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstBytes()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstBytes()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public byte[] executeGetFirstBytes(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstBytes(query, null);
    }

    /**
     * Safely retrieves the first cell as a <code>byte</code> array from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstBytes()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first <code>byte</code> array in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstBytes()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstBytes()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public byte[] executeGetFirstBytes(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                byte[] result = null;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstBytes();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a sql <code>Date</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstDate()} method, but also automatically closes the
     * statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first sql <code>Date</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstDate()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstDate()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Date executeGetFirstDate(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstDate(query, (DbPreparedStatementHandler) null);
    }

    /**
     * Safely retrieves the first cell as a sql <code>Date</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstDate()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first sql <code>Date</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstDate()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstDate()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Date executeGetFirstDate(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                java.sql.Date result = null;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstDate();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a sql <code>Date</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstDate(Calendar)} method, but also automatically
     * closes the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @param cal   the <code>Calendar</code> object to use in constructing the
     *              date
     * @return the first sql <code>Date</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstDate(Calendar)}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstDate(Calendar)
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Date executeGetFirstDate(ReadQuery query, Calendar cal)
    throws DatabaseException {
        return executeGetFirstDate(query, cal, null);
    }

    /**
     * Safely retrieves the first cell as a sql <code>Date</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstDate(Calendar)} method, but also automatically
     * closes the statement after its execution and allows customization of
     * the prepared statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param cal     the <code>Calendar</code> object to use in constructing the
     *                date
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first sql <code>Date</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstDate(Calendar)}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstDate(Calendar)
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Date executeGetFirstDate(ReadQuery query, Calendar cal, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                java.sql.Date result = null;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstDate(cal);
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a sql <code>Time</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstTime()} method, but also automatically closes the
     * statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first sql <code>Time</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstTime()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstTime()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Time executeGetFirstTime(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstTime(query, (DbPreparedStatementHandler) null);
    }

    /**
     * Safely retrieves the first cell as a sql <code>Time</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstTime()} method, but also automatically closes the
     * statement after its execution and allows customization of the prepared
     * statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first sql <code>Time</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstTime()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstTime()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Time executeGetFirstTime(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                java.sql.Time result = null;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstTime();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a sql <code>Time</code>
     * from the results of a select query. It relies on the wrapped {@link
     * DbResultSet#getFirstTime(Calendar)} method, but also automatically
     * closes the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @param cal   the <code>Calendar</code> object to use in constructing the
     *              time
     * @return the first sql <code>Time</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstTime(Calendar)}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstTime(Calendar)
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Time executeGetFirstTime(ReadQuery query, Calendar cal)
    throws DatabaseException {
        return executeGetFirstTime(query, cal, null);
    }

    /**
     * Safely retrieves the first cell as a sql <code>Time</code> from the
     * results of a customizable select query. It relies on the wrapped {@link
     * DbResultSet#getFirstTime(Calendar)} method, but also automatically
     * closes the statement after its execution and allows customization of
     * the prepared statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param cal     the <code>Calendar</code> object to use in constructing the
     *                time
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first sql <code>Time</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstTime(Calendar)}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstTime(Calendar)
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Time executeGetFirstTime(ReadQuery query, Calendar cal, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                java.sql.Time result = null;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstTime(cal);
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a sql
     * <code>Timestamp</code> from the results of a select query. It relies on
     * the wrapped {@link DbResultSet#getFirstTimestamp()} method, but also
     * automatically closes the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @return the first sql <code>Timestamp</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstTimestamp()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstTimestamp()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Timestamp executeGetFirstTimestamp(ReadQuery query)
    throws DatabaseException {
        return executeGetFirstTimestamp(query, (DbPreparedStatementHandler) null);
    }

    /**
     * Safely retrieves the first cell as a sql <code>Timestamp</code> from
     * the results of a customizable select query. It relies on the wrapped
     * {@link DbResultSet#getFirstTimestamp()} method, but also automatically
     * closes the statement after its execution and allows customization of
     * the prepared statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first sql <code>Timestamp</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstTimestamp()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstTimestamp()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Timestamp executeGetFirstTimestamp(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                java.sql.Timestamp result = null;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstTimestamp();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as a sql
     * <code>Timestamp</code> from the results of a select query. It relies on
     * the wrapped {@link DbResultSet#getFirstTimestamp(Calendar)} method, but
     * also automatically closes the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @param cal   the <code>Calendar</code> object to use in constructing the
     *              timestamp
     * @return the first sql <code>Timestamp</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstTimestamp(Calendar)}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstTimestamp(Calendar)
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Timestamp executeGetFirstTimestamp(ReadQuery query, Calendar cal)
    throws DatabaseException {
        return executeGetFirstTimestamp(query, cal, null);
    }

    /**
     * Safely retrieves the first cell as a sql <code>Timestamp</code> from
     * the results of a customizable select query. It relies on the wrapped
     * {@link DbResultSet#getFirstTimestamp(Calendar)} method, but also
     * automatically closes the statement after its execution and allows
     * customization of the prepared statement through an optional instance of
     * {@link PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param cal     the <code>Calendar</code> object to use in constructing the
     *                timestamp
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the first sql <code>Timestamp</code> in the query's result set
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           DbResultSet#getFirstTimestamp(Calendar)}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstTimestamp(Calendar)
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public java.sql.Timestamp executeGetFirstTimestamp(ReadQuery query, Calendar cal, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                java.sql.Timestamp result = null;

                if (executeHasResultRows(statement, full_handler)) {
                    result = getResultSet(statement).getFirstTimestamp(cal);
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as an ASCII
     * <code>InputStream</code> from the results of a select query. It relies
     * on the wrapped {@link DbResultSet#getFirstAsciiStream()} method, but
     * also automatically closes the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @param user  an instance of <code>InputStreamUser</code>
     *              that contains the logic that will be executed with this stream
     * @return the return value from the <code>useInputStream</code> method of
     * the provided <code>InputStreamUser</code> instance
     * @throws DatabaseException   see {@link
     *                             DbPreparedStatement#executeQuery()} and {@link
     *                             DbResultSet#getFirstAsciiStream()}
     * @throws InnerClassException when errors occurs inside the
     *                             <code>InputStreamUser</code>
     * @see InputStreamUser
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstAsciiStream()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public <ResultType> ResultType executeUseFirstAsciiStream(ReadQuery query, InputStreamUser user)
    throws DatabaseException, InnerClassException {
        return (ResultType) executeUseFirstAsciiStream(query, user, null);
    }

    /**
     * Safely retrieves the first cell as an ASCII <code>InputStream</code>
     * from the results of a customizable select query. It relies on the
     * wrapped {@link DbResultSet#getFirstAsciiStream()} method, but also
     * automatically closes the statement after its execution and allows
     * customization of the prepared statement through an optional instance of
     * {@link PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param user    an instance of <code>InputStreamUser</code>
     *                that contains the logic that will be executed with this stream
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the return value from the <code>useInputStream</code> method of
     * the provided <code>InputStreamUser</code> instance
     * @throws DatabaseException   see {@link
     *                             DbPreparedStatement#executeQuery()} and {@link
     *                             DbResultSet#getFirstAsciiStream()}
     * @throws InnerClassException when errors occurs inside the
     *                             <code>InputStreamUser</code>
     * @see InputStreamUser
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstAsciiStream()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public <ResultType> ResultType executeUseFirstAsciiStream(ReadQuery query, InputStreamUser user, PreparedStatementHandler handler)
    throws DatabaseException, InnerClassException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");
        if (null == user) throw new IllegalArgumentException("user can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            InputStream stream = null;
            try {
                statement.setFetchSize(1);

                if (executeHasResultRows(statement, full_handler)) {
                    stream = getResultSet(statement).getFirstAsciiStream();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return (ResultType) user.useInputStream(stream);
            } finally {
                defensiveClose(stream);
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as an character
     * <code>Reader</code> from the results of a select query. It relies on
     * the wrapped {@link DbResultSet#getFirstCharacterStream()} method, but
     * also automatically closes the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @param user  an instance of <code>ReaderUser</code>
     *              that contains the logic that will be executed with this reader
     * @return the return value from the <code>useReader</code> method of
     * the provided <code>ReaderUser</code> instance
     * @throws DatabaseException   see {@link
     *                             DbPreparedStatement#executeQuery()} and {@link
     *                             DbResultSet#getFirstCharacterStream()}
     * @throws InnerClassException when errors occurs inside the
     *                             <code>ReaderUser</code>
     * @see ReaderUser
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstCharacterStream()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public <ResultType> ResultType executeUseFirstCharacterStream(ReadQuery query, ReaderUser user)
    throws DatabaseException, InnerClassException {
        return (ResultType) executeUseFirstCharacterStream(query, user, null);
    }

    /**
     * Safely retrieves the first cell as an character <code>Reader</code>
     * from the results of a customizable select query. It relies on the
     * wrapped {@link DbResultSet#getFirstCharacterStream()} method, but also
     * automatically closes the statement after its execution and allows
     * customization of the prepared statement through an optional instance of
     * {@link PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param user    an instance of <code>ReaderUser</code>
     *                that contains the logic that will be executed with this reader
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the return value from the <code>useReader</code> method of
     * the provided <code>ReaderUser</code> instance
     * @throws DatabaseException   see {@link
     *                             DbPreparedStatement#executeQuery()} and {@link
     *                             DbResultSet#getFirstCharacterStream()}
     * @throws InnerClassException when errors occurs inside the
     *                             <code>ReaderUser</code>
     * @see ReaderUser
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstCharacterStream()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public <ResultType> ResultType executeUseFirstCharacterStream(ReadQuery query, ReaderUser user, PreparedStatementHandler handler)
    throws DatabaseException, InnerClassException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");
        if (null == user) throw new IllegalArgumentException("user can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            Reader reader = null;
            try {
                statement.setFetchSize(1);

                if (executeHasResultRows(statement, full_handler)) {
                    reader = getResultSet(statement).getFirstCharacterStream();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return (ResultType) user.useReader(reader);
            } finally {
                defensiveClose(reader);
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly retrieves the first cell as an binary
     * <code>InputStream</code> from the results of a select query. It relies
     * on the wrapped {@link DbResultSet#getFirstBinaryStream()} method, but
     * also automatically closes the statement after its execution.
     * <p>Refer to {@link #executeGetFirstString(ReadQuery) executeGetFirstString}
     * for an example code snippet, it's 100% analogous.
     *
     * @param query the query builder instance that needs to be executed
     * @param user  an instance of <code>InputStreamUser</code>
     *              that contains the logic that will be executed with this stream
     * @return the return value from the <code>useInputStream</code> method of
     * the provided <code>InputStreamUser</code> instance
     * @throws DatabaseException   see {@link
     *                             DbPreparedStatement#executeQuery()} and {@link
     *                             DbResultSet#getFirstBinaryStream()}
     * @throws InnerClassException when errors occurs inside the
     *                             <code>InputStreamUser</code>
     * @see InputStreamUser
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstBinaryStream()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public <ResultType> ResultType executeUseFirstBinaryStream(ReadQuery query, InputStreamUser user)
    throws DatabaseException, InnerClassException {
        return (ResultType) executeUseFirstBinaryStream(query, user, null);
    }

    /**
     * Safely retrieves the first cell as an binary <code>InputStream</code>
     * from the results of a customizable select query. It relies on the
     * wrapped {@link DbResultSet#getFirstBinaryStream()} method, but also
     * automatically closes the statement after its execution and allows
     * customization of the prepared statement through an optional instance of
     * {@link PreparedStatementHandler}.
     * <p>Refer to {@link
     * #executeGetFirstString(ReadQuery, PreparedStatementHandler)
     * executeGetFirstString} for an example code snippet, it's 100% analogous.
     *
     * @param query   the query builder instance that needs to be executed
     * @param user    an instance of <code>InputStreamUser</code>
     *                that contains the logic that will be executed with this stream
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the return value from the <code>useInputStream</code> method of
     * the provided <code>InputStreamUser</code> instance
     * @throws DatabaseException   see {@link
     *                             DbPreparedStatement#executeQuery()} and {@link
     *                             DbResultSet#getFirstBinaryStream()}
     * @throws InnerClassException when errors occurs inside the
     *                             <code>InputStreamUser</code>
     * @see InputStreamUser
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSet#getFirstBinaryStream()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public <ResultType> ResultType executeUseFirstBinaryStream(ReadQuery query, InputStreamUser user, PreparedStatementHandler handler)
    throws DatabaseException, InnerClassException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");
        if (null == user) throw new IllegalArgumentException("user can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            InputStream stream = null;
            try {
                statement.setFetchSize(1);

                if (executeHasResultRows(statement, full_handler)) {
                    stream = getResultSet(statement).getFirstBinaryStream();
                }

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return (ResultType) user.useInputStream(stream);
            } finally {
                defensiveClose(stream);
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Safely and quickly fetches the first row from the results of a select
     * query. It relies on the wrapped {@link
     * #fetch(ResultSet, DbRowProcessor)} method, but automatically closes the
     * statement after its execution.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person").where("name", "=", "me");
     * DbRowProcessor processor = new YourProcessor();
     * boolean result = manager.executeFetchFirst(select, processor);</pre>
     *
     * @param query        the query builder instance that needs to be executed
     * @param rowProcessor a <code>DbRowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched row
     * @return <code>true</code> if a row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           #fetch(ResultSet, DbRowProcessor)}
     * @see #fetch(ResultSet, DbRowProcessor)
     * @see DbRowProcessor
     * @since 1.0
     */
    public boolean executeFetchFirst(ReadQuery query, DbRowProcessor rowProcessor)
    throws DatabaseException {
        return executeFetchFirst(query, rowProcessor, null);
    }

    /**
     * Convenience alternative to {@link #executeFetchFirst(ReadQuery, DbRowProcessor)} that
     * uses a simplified <code>RowProcessor</code> that can be implemented with a lambda.
     *
     * @param query        the query builder instance that needs to be executed
     * @param rowProcessor a <code>RowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched row
     * @return <code>true</code> if a row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           #fetchAll(ResultSet, DbRowProcessor)}
     * @see #executeFetchFirst(ReadQuery, DbRowProcessor)
     * @see RowProcessor
     * @since 1.0
     */
    public boolean executeFetchFirst(ReadQuery query, RowProcessor rowProcessor)
    throws DatabaseException {
        return executeFetchFirst(query, rowProcessor, null);
    }

    /**
     * Safely fetches the first row from the results of a customizable select
     * query. It relies on the wrapped {@link
     * #fetch(ResultSet, DbRowProcessor)} method, but also automatically
     * closes the statement after its execution and allows customization of
     * the prepared statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person").whereParameter("name", "=");
     * DbRowProcessor processor = new YourProcessor();
     * final String name = "you";
     * boolean result = manager.executeFetchFirst(select, processor,
     *     statement -> statement.setString("name", name));
     * </pre>
     *
     * @param query        the query builder instance that needs to be executed
     * @param rowProcessor a <code>DbRowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched row
     * @param handler      an instance of <code>PreparedStatementHandler</code>
     *                     that will be used to customize the query execution; or
     *                     <code>null</code> if you don't want to customize it at all
     * @return <code>true</code> if a row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           #fetch(ResultSet, DbRowProcessor)}
     * @see #fetch(ResultSet, DbRowProcessor)
     * @see DbRowProcessor
     * @since 1.0
     */
    public boolean executeFetchFirst(ReadQuery query, DbRowProcessor rowProcessor, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                statement.setFetchSize(1);

                executeQuery(statement, full_handler);

                var result = fetch(getResultSet(statement), rowProcessor);

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Convenience alternative to {@link #executeFetchFirst(ReadQuery, DbRowProcessor, PreparedStatementHandler)}
     * that uses a simplified <code>RowProcessor</code> that can be implemented with a lambda.
     *
     * @param query        the query builder instance that needs to be executed
     * @param rowProcessor a <code>RowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched row
     * @param handler      an instance of <code>PreparedStatementHandler</code>
     *                     that will be used to customize the query execution; or
     *                     <code>null</code> if you don't want to customize it at all
     * @return <code>true</code> if a row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           #fetchAll(ResultSet, DbRowProcessor)}
     * @see #executeFetchFirst(ReadQuery, DbRowProcessor, PreparedStatementHandler)
     * @see RowProcessor
     * @since 1.0
     */
    public boolean executeFetchFirst(ReadQuery query, RowProcessor rowProcessor, PreparedStatementHandler handler) {
        if (rowProcessor == null) {
            return executeFetchFirst(query, (DbRowProcessor) null, handler);
        }

        return executeFetchFirst(query, new DbRowProcessor() {
            public boolean processRow(ResultSet resultSet)
            throws SQLException {
                rowProcessor.processRow(resultSet);
                return true;
            }
        }, handler);
    }

    /**
     * Safely and quickly fetches the first bean instance from the results of
     * a select query. It relies on the wrapped {@link
     * #executeFetchFirst(ReadQuery, DbRowProcessor)} method, but automatically
     * uses an appropriate {@link DbBeanFetcher} instance and returns the
     * resulting bean.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person").fields(Person.class);
     * Person person = manager.executeFetchFirstBean(select, Person.class);</pre>
     *
     * @param query     the query builder instance that needs to be executed
     * @param beanClass the class of the bean
     * @return <code>true</code> if a row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link DbBeanFetcher} and {@link
     *                           #executeFetchFirst(ReadQuery, DbRowProcessor)}
     * @see #executeFetchFirst(ReadQuery, DbRowProcessor)
     * @see DbBeanFetcher
     * @since 1.0
     */
    public <BeanType> BeanType executeFetchFirstBean(ReadQuery query, Class<BeanType> beanClass)
    throws DatabaseException {
        return executeFetchFirstBean(query, beanClass, null);
    }

    /**
     * Safely fetches the first bean instance from the results of a
     * customizable select query. It relies on the wrapped {@link
     * #executeFetchFirst(ReadQuery, DbRowProcessor)} method, but automatically
     * uses an appropriate {@link DbBeanFetcher} instance, returns the
     * resulting bean and allows customization of the prepared statement
     * through an optional instance of {@link PreparedStatementHandler}.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person").fields(Person.class).whereParameter("name", "=");
     * final String name = "you";
     * Person person = manager.executeFetchFirstBean(select, Person.class,
     *     statement -> statement.setString("name", name));
     * </pre>
     *
     * @param query     the query builder instance that needs to be executed
     * @param beanClass the class of the bean
     * @param handler   an instance of <code>PreparedStatementHandler</code>
     *                  that will be used to customize the query execution; or
     *                  <code>null</code> if you don't want to customize it at all
     * @return <code>true</code> if a row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link DbBeanFetcher} and {@link
     *                           #executeFetchFirst(ReadQuery, DbRowProcessor)}
     * @see #executeFetchFirst(ReadQuery, DbRowProcessor)
     * @see DbBeanFetcher
     * @since 1.0
     */
    public <BeanType> BeanType executeFetchFirstBean(ReadQuery query, Class<BeanType> beanClass, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var bean_fetcher = new DbBeanFetcher<>(getDatasource(), beanClass);
        if (executeFetchFirst(query, bean_fetcher, full_handler)) {
            return bean_fetcher.getBeanInstance();
        }

        return null;
    }

    /**
     * Safely and quickly fetches all the rows from the results of a select
     * query. It relies on the wrapped {@link
     * #fetchAll(ResultSet, DbRowProcessor)} method, but automatically closes
     * the statement after its execution.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person").where("gender", "=", "m");
     * DbRowProcessor processor = new YourProcessor();
     * boolean result = manager.executeFetchAll(select, processor);</pre>
     *
     * @param query        the query builder instance that needs to be executed
     * @param rowProcessor a <code>DbRowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched rows
     * @return <code>true</code> if rows were retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           #fetchAll(ResultSet, DbRowProcessor)}
     * @see #fetchAll(ResultSet, DbRowProcessor)
     * @see DbRowProcessor
     * @since 1.0
     */
    public boolean executeFetchAll(ReadQuery query, DbRowProcessor rowProcessor)
    throws DatabaseException {
        return executeFetchAll(query, rowProcessor, null);
    }

    /**
     * Convenience alternative to {@link #executeFetchAll(ReadQuery, DbRowProcessor))}
     * that uses a simplified <code>RowProcessor</code> that can be implemented with a lambda.
     *
     * @param query        the query builder instance that needs to be executed
     * @param rowProcessor a <code>RowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched rows
     * @return <code>true</code> if a row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           #fetchAll(ResultSet, DbRowProcessor)}
     * @see #executeFetchAll(ReadQuery, DbRowProcessor)
     * @see RowProcessor
     * @since 1.0
     */
    public boolean executeFetchAll(ReadQuery query, RowProcessor rowProcessor)
    throws DatabaseException {
        return executeFetchAll(query, rowProcessor, null);
    }

    /**
     * Safely fetches all the rows from the results of a customizable select
     * query. It relies on the wrapped {@link
     * #fetchAll(ResultSet, DbRowProcessor)} method, but also automatically
     * closes the statement after its execution and allows customization of
     * the prepared statement through an optional instance of {@link
     * PreparedStatementHandler}.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person").whereParameter("gender", "=");
     * DbRowProcessor processor = new YourProcessor();
     * final String name = "m";
     * boolean result = manager.executeFetchAll(select, processor,
     *     statement -> statement.setString("name", name));
     * </pre>
     *
     * @param query        the query builder instance that needs to be executed
     * @param rowProcessor a <code>DbRowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched row
     * @param handler      an instance of <code>PreparedStatementHandler</code>
     *                     that will be used to customize the query execution; or
     *                     <code>null</code> if you don't want to customize it at all
     * @return <code>true</code> if rows were retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           #fetchAll(ResultSet, DbRowProcessor)}
     * @see #fetchAll(ResultSet, DbRowProcessor)
     * @see DbRowProcessor
     * @since 1.0
     */
    public boolean executeFetchAll(ReadQuery query, DbRowProcessor rowProcessor, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                executeQuery(statement, full_handler);

                var result = fetchAll(getResultSet(statement), rowProcessor);

                if (full_handler != null) {
                    try {
                        full_handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return result;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Convenience alternative to {@link #executeFetchAll(ReadQuery, DbRowProcessor, PreparedStatementHandler))}
     * that uses a simplified <code>RowProcessor</code> that can be implemented with a lambda.
     *
     * @param query        the query builder instance that needs to be executed
     * @param rowProcessor a <code>RowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched rows
     * @param handler      an instance of <code>PreparedStatementHandler</code>
     *                     that will be used to customize the query execution; or
     *                     <code>null</code> if you don't want to customize it at all
     * @return <code>true</code> if a row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()} and {@link
     *                           #fetchAll(ResultSet, DbRowProcessor)}
     * @see #executeFetchAll(ReadQuery, DbRowProcessor, PreparedStatementHandler)
     * @see RowProcessor
     * @since 1.0
     */
    public boolean executeFetchAll(ReadQuery query, RowProcessor rowProcessor, PreparedStatementHandler handler)
    throws DatabaseException {
        if (rowProcessor == null) {
            return executeFetchAll(query, (DbRowProcessor) rowProcessor, handler);
        }

        return executeFetchAll(query, new DbRowProcessor() {
            public boolean processRow(ResultSet resultSet)
            throws SQLException {
                rowProcessor.processRow(resultSet);
                return true;
            }
        }, handler);
    }

    /**
     * Safely and quickly fetches the all the bean instances from the results
     * of a select query. It relies on the wrapped {@link
     * #executeFetchAll(ReadQuery, DbRowProcessor)} method, but automatically
     * uses an appropriate {@link DbBeanFetcher} instance and returns the
     * results.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person").fields(Person.class).where("gender", "=", "m");
     * List persons = manager.executeFetchAllBeans(select, Person.class);</pre>
     *
     * @param query     the query builder instance that needs to be executed
     * @param beanClass the class of the bean
     * @return <code>a List instance with all the beans, the list is empty if
     * no beans could be returned</code>
     * @throws DatabaseException see {@link DbBeanFetcher} and {@link
     *                           #executeFetchAll(ReadQuery, DbRowProcessor)}
     * @see #executeFetchAll(ReadQuery, DbRowProcessor)
     * @see DbBeanFetcher
     * @since 1.0
     */
    public <BeanType> List<BeanType> executeFetchAllBeans(ReadQuery query, Class<BeanType> beanClass)
    throws DatabaseException {
        return executeFetchAllBeans(query, beanClass, null);
    }

    /**
     * Safely fetches the all the bean instances from the results of a
     * customizable select query. It relies on the wrapped {@link
     * #executeFetchAll(ReadQuery, DbRowProcessor)} method, but automatically
     * uses an appropriate {@link DbBeanFetcher} instance, returns the results
     * and allows customization of the prepared statement through an optional
     * instance of {@link PreparedStatementHandler}.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select.from("person").fields(Person.class).whereParameter("gender", "=");
     * final String name = "m";
     * List persons = manager.executeFetchAllBeans(select, Person.class,
     *     statement -> statement.setString("name", name));
     * </pre>
     *
     * @param query     the query builder instance that needs to be executed
     * @param beanClass the class of the bean
     * @param handler   an instance of <code>PreparedStatementHandler</code>
     *                  that will be used to customize the query execution; or
     *                  <code>null</code> if you don't want to customize it at all
     * @return <code>a List instance with all the beans, the list is empty if
     * no beans could be returned</code>
     * @throws DatabaseException see {@link DbBeanFetcher} and {@link
     *                           #executeFetchAll(ReadQuery, DbRowProcessor)}
     * @see #executeFetchAll(ReadQuery, DbRowProcessor)
     * @see DbBeanFetcher
     * @since 1.0
     */
    public <BeanType> List<BeanType> executeFetchAllBeans(ReadQuery query, Class<BeanType> beanClass, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var bean_fetcher = new DbBeanFetcher<>(getDatasource(), beanClass, true);
        executeFetchAll(query, bean_fetcher, full_handler);

        return bean_fetcher.getCollectedInstances();
    }

    /**
     * Executes a customizable select statement. It relies on the wrapped
     * {@link DbPreparedStatement#executeQuery()} method, but also
     * automatically closes the statement after its execution and allows
     * complete customization of the prepared statement through an optional
     * instance of {@link PreparedStatementHandler}.
     * <p>This method is typically used when you need to fully customize a
     * query at runtime, but still want to benefit of a safety net that
     * ensures that the allocated statement will be closed. Often another more
     * specialized method in this class will already serve your needs, so be
     * sure to verify that you actually need to intervene on every front.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select
     *    .field("first")
     *    .field("last")
     *    .from("person")
     *    .whereParameter("name", "=");
     * final String name = "you";
     * String result = (String)manager.executeQuery(select, new DbPreparedStatementHandler() {
     *        public void setParameters(DbPreparedStatement statement)
     *        {
     *            statement
     *                .setString("name", name);
     *        }
     *
     *        public Object concludeResults(DbResultSet result set)
     *        throws SQLException
     *        {
     *            return result set.getString("first")+" "+result set.getString("last");
     *        }
     *    });</pre>
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code>PreparedStatementHandler</code>
     *                that will be used to customize the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the object that was returned by the overridden {@link
     * DbResultSetHandler#concludeResults(DbResultSet) concludeResults}
     * method; or
     * <p><code>null</code> if this method wasn't overridden
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()}
     * @see DbPreparedStatement#executeQuery()
     * @see PreparedStatementHandler
     * @see DbPreparedStatementHandler
     * @since 1.0
     */
    public <ResultType> ResultType executeQuery(ReadQuery query, PreparedStatementHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var full_handler = ensureFullPreparedStatementHandler(handler);
        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, full_handler, connection);
            try {
                executeQuery(statement, full_handler);
                if (null == full_handler) {
                    return null;
                }

                try {
                    return (ResultType) full_handler.concludeResults(getResultSet(statement));
                } catch (SQLException e) {
                    statement.handleException();
                    throw new DatabaseException(e);
                }
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Executes a select statement and handle the results in a custom fashion.
     * It relies on the wrapped {@link DbPreparedStatement#executeQuery()}
     * method, but also automatically closes the statement after its execution
     * and allows interaction with the result set through an optional instance
     * of {@link DbResultSetHandler}.
     * <p>This method is typically used when you need to interact with the
     * results of a query, but still want to benefit of a safety net that
     * ensures that the allocated statement will be closed. Often another more
     * specialized method in this class will already serve your needs, so be
     * sure to verify that there isn't another one that's better suited.
     * <h4>Example</h4>
     * <pre>DbQueryManager manager = new DbQueryManager(datasource);
     * Select select = new Select(datasource);
     * select
     *    .field("first")
     *    .field("last")
     *    .from("person");
     * String result = (String)manager.executeResultQuery(select, new DbResultSetHandler() {
     *        public Object concludeResults(DbResultSet result set)
     *        throws SQLException
     *        {
     *            return result set.getString("first")+" "+result set.getString("last");
     *        }
     *    });</pre>
     *
     * @param query   the query builder instance that needs to be executed
     * @param handler an instance of <code><code>DbResultSetHandler</code></code>
     *                that will be used to handle the results of the query execution; or
     *                <code>null</code> if you don't want to customize it at all
     * @return the object that was returned by the overridden {@link
     * DbResultSetHandler#concludeResults(DbResultSet) concludeResults}
     * method; or
     * <p><code>null</code> if this method wasn't overridden
     * @throws DatabaseException see {@link
     *                           DbPreparedStatement#executeQuery()}
     * @see DbPreparedStatement#executeQuery()
     * @see DbResultSetHandler
     * @since 1.0
     */
    public <ResultType> ResultType executeResultQuery(ReadQuery query, DbResultSetHandler handler)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var connection = getConnection();
        try {
            var statement = getPreparedStatement(query, handler, connection);
            try {
                executeQuery(statement, null);

                if (handler != null) {
                    try {
                        return (ResultType) handler.concludeResults(getResultSet(statement));
                    } catch (SQLException e) {
                        statement.handleException();
                        throw new DatabaseException(e);
                    }
                }

                return null;
            } finally {
                defensiveClose(statement);
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Reserves a database connection for a particular thread for all the
     * instructions that are executed in the provided {@link DbConnectionUser}
     * instance.
     * <p>This is typically used to ensure that a series of operations is done
     * with the same connection, even though a database pool is used in the
     * background.
     * <h4>Example</h4>
     * <pre>Person person;
     * final Insert store_data = new Insert(datasource).into("person").fields(person);
     * final Select get_last_id = new Select(datasource).from("person").field("LAST_INSERT_ID()");
     * final DbQueryManager manager = new DbQueryManager(datasource);
     * int id = ((Integer)manager.reserveConnection(new DbConnectionUser() {
     *        public Integer useConnection(DbConnection connection)
     *        {
     *            manager.executeUpdate(store_data);
     *            return new Integer(manager.executeGetFirstInt(get_last_id));
     *        }
     *    })).intValue();</pre>
     *
     * @param user an instance of <code>DbConnectionUser</code> that contains
     *             the logic that will be executed
     * @return the return value from the <code>useConnection</code> method of
     * the provided <code>DbConnectionUser</code> instance
     * @throws DatabaseException   when errors occurs during the reservation
     *                             of a connection for this thread
     * @throws InnerClassException when errors occurs inside the
     *                             <code>DbConnectionUser</code>
     * @see DbConnectionUser#useConnection(DbConnection)
     * @since 1.0
     */
    public <ResultType> ResultType reserveConnection(DbConnectionUser user)
    throws InnerClassException, DatabaseException {
        if (null == user) throw new IllegalArgumentException("user can't be null.");

        var connection = datasource_.getConnection();
        var pool = datasource_.getPool();
        synchronized (pool) {
            var does_threadconnection_exist = pool.hasThreadConnection(Thread.currentThread());
            try {
                if (!does_threadconnection_exist) pool.registerThreadConnection(Thread.currentThread(), connection);

                return (ResultType) user.useConnection(connection);
            } finally {
                if (!does_threadconnection_exist) pool.unregisterThreadConnection(Thread.currentThread());
            }
        }
    }

    /**
     * Ensures that all the instructions that are executed in the provided
     * {@link DbTransactionUser} instance are executed inside a transaction
     * and committed afterwards. This doesn't mean that a new transaction will
     * always be created. If a transaction is already active, it will simply
     * be re-used. The commit will also only be take place if a new
     * transaction has actually been started, otherwise it's the
     * responsibility of the enclosing code to execute the commit. If an
     * runtime exception occurs during the execution and a new transaction has
     * been started beforehand, it will be automatically rolled back.
     * <p>If you need to explicitly roll back an active transaction, use the
     * {@link DbTransactionUser#rollback() rollback} method of the
     * <code>DbTransactionUser</code> class. If you use a regular rollback
     * method, it's possible that you're inside a nested transaction executed
     * and that after the rollback, other logic continues to be executed
     * outside the transaction. Using the correct {@link
     * DbTransactionUser#rollback() rollback} method, stops the execution of
     * the active <code>DbTransactionUser</code> and breaks out of any number
     * of them nesting.
     * <p>It's recommended to always use transactions through this method
     * since it ensures that transactional code can be re-used and enclosed in
     * other transactional code. Correctly using the regular
     * transaction-related methods requires great care and planning and often
     * results in error-prone and not reusable code.
     * <h4>Example</h4>
     * <pre>final Insert insert = new Insert(mDatasource).into("valuelist").field("value", 232);
     * final DbQueryManager manager = new DbQueryManager(datasource);
     * manager.inTransaction(new DbTransactionUserWithoutResult() {
     *        public void useTransactionWithoutResult()
     *        throws InnerClassException {
     *            manager.executeUpdate(insert);
     *            manager.executeUpdate(insert);
     *        }
     *    });
     * </pre>
     *
     * @param user an instance of <code>TransactionUser</code> that contains
     *             the logic that will be executed
     * @return the return value from the <code>useTransaction</code> method of
     * the provided <code>DbTransactionUser</code> instance
     * @throws DatabaseException   when errors occurs during the handling of
     *                             the transaction
     * @throws InnerClassException when errors occurs inside the
     *                             <code>DbTransactionUser</code>
     * @see DbTransactionUser#useTransaction()
     * @see DbTransactionUserWithoutResult#useTransactionWithoutResult()
     * @since 1.0
     */
    public <ResultType> ResultType inTransaction(TransactionUser user)
    throws InnerClassException, DatabaseException {
        DbTransactionUser full_user = null;
        if (user != null) {
            if (user instanceof DbTransactionUser u) {
                full_user = u;
            } else {
                full_user = new DbTransactionUser<>() {
                    public Object useTransaction()
                    throws InnerClassException {
                        return user.useTransaction();
                    }
                };
            }
        }

        var started_transaction = false;
        DbConnection connection = null;
        try {
            synchronized (datasource_) {
                connection = datasource_.getConnection();
                var isolation = full_user.getTransactionIsolation();
                if (isolation != -1) {
                    connection.setTransactionIsolation(isolation);
                }
                started_transaction = connection.beginTransaction();
            }

            var result = (ResultType) full_user.useTransaction();
            if (started_transaction) {
                connection.commit();
                if (!datasource_.isPooled()) {
                    connection.close();
                }
            }
            return result;
        } catch (RollbackException e) {
            if (connection != null) {
                connection.rollback();
                if (!datasource_.isPooled()) {
                    connection.close();
                }
            }

            if (started_transaction) {
                return (ResultType) null;
            } else {
                throw e;
            }
        } catch (RuntimeException e) {
            if (started_transaction &&
                connection != null) {
                try {
                    if (e instanceof ControlFlowRuntimeException) {
                        connection.commit();
                    } else {
                        connection.rollback();
                        if (!datasource_.isPooled()) {
                            connection.close();
                        }
                    }
                } catch (DatabaseException e2) {
                    // nothing that can be done about this
                    // the connection is probably closed since
                    // a database error occurred
                }
            }
            throw e;
        } catch (Error e) {
            if (started_transaction &&
                connection != null) {
                try {
                    connection.rollback();
                    if (!datasource_.isPooled()) {
                        connection.close();
                    }
                } catch (DatabaseException e2) {
                    // nothing that can be done about this
                    // the connection is probably closed since
                    // a database error occurred
                }
            }
            throw e;
        }
    }

    /**
     * Convenience method that ensures that all the instructions that are
     * executed in the provided {@link TransactionUserWithoutResult} instance,
     * are executed inside a transaction and committed afterwards.
     * <p>
     * Everything of {@link #inTransaction(TransactionUser)} applies but instead
     * of requiring a result to be returned, it's assumed that none will,
     * making your code cleaner.
     *
     * @param user an instance of <code>TransactionUserWithoutResult</code>
     *             that contains the logic that will be executed
     * @throws DatabaseException   when errors occurs during the handling of
     *                             the transaction
     * @throws InnerClassException when errors occurs inside the
     *                             <code>DbTransactionUser</code>
     * @see TransactionUserWithoutResult#useTransaction()
     * @see #inTransaction(TransactionUser)
     * @since 1.0
     */
    public void inTransaction(TransactionUserWithoutResult user)
    throws InnerClassException, DatabaseException {
        inTransaction(new DbTransactionUser<>() {
            public Object useTransaction()
            throws InnerClassException {
                user.useTransaction();
                return null;
            }
        });
    }

    /**
     * Executes a query statement in a connection of this
     * <code>DbQueryManager</code>'s <code>Datasource</code>. Functions
     * exactly as the wrapped {@link DbStatement#executeQuery(ReadQuery)} method.
     * <p>Note that the statement will not be automatically closed since using
     * this method implies that you still have to work with the result set.
     *
     * @param query the query builder instance that should be executed
     * @return the statement that has been executed
     * @throws DatabaseException see {@link DbStatement#executeQuery(ReadQuery)}
     * @see DbStatement#executeQuery(ReadQuery)
     * @since 1.0
     */
    public DbStatement executeQuery(ReadQuery query)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var statement = getConnection().createStatement();
        statement.executeQuery(query);
        return statement;
    }

    /**
     * Fetches the next row of a result set without processing it in any way.
     *
     * @param resultSet a valid <code>ResultSet</code> instance
     * @return <code>true</code> if a new row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException when an error occurred during the fetch of
     *                           the next row in the result set
     * @see #fetch(ResultSet, DbRowProcessor)
     * @since 1.0
     */
    public boolean fetch(ResultSet resultSet)
    throws DatabaseException {
        return fetch(resultSet, (DbRowProcessor) null);
    }

    /**
     * Fetches the next row of a result set and processes it through a
     * <code>DbRowProcessor</code>.
     *
     * @param resultSet    a valid <code>ResultSet</code> instance
     * @param rowProcessor a <code>DbRowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched row
     * @return <code>true</code> if a new row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException when an error occurred during the fetch of
     *                           the next row in the result set
     * @see #fetch(ResultSet)
     * @see DbRowProcessor
     * @since 1.0
     */
    public boolean fetch(ResultSet resultSet, DbRowProcessor rowProcessor)
    throws DatabaseException {
        if (null == resultSet) throw new IllegalArgumentException("resultSet can't be null.");

        try {
            if (resultSet.next()) {
                if (rowProcessor != null) {
                    rowProcessor.processRowWrapper(resultSet);
                }
                return true;
            }
        } catch (SQLException e) {
            throw new RowProcessorErrorException(e);
        }

        return false;
    }

    /**
     * Convenience alternative to {@link #fetch(ResultSet, DbRowProcessor)))}
     * that uses a simplified <code>RowProcessor</code> that can be implemented with a lambda.
     *
     * @param resultSet    a valid <code>ResultSet</code> instance
     * @param rowProcessor a <code>RowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched row
     * @return <code>true</code> if a new row was retrieved; or
     * <p><code>false</code> if there are no more rows .
     * @throws DatabaseException when an error occurred during the fetch of
     *                           the next row in the result set
     * @see #fetch(ResultSet, DbRowProcessor)
     * @see RowProcessor
     * @since 1.0
     */
    public boolean fetch(ResultSet resultSet, RowProcessor rowProcessor)
    throws DatabaseException {
        if (rowProcessor == null) {
            return fetch(resultSet, (DbRowProcessor) null);
        }

        return fetch(resultSet, new DbRowProcessor() {
            public boolean processRow(ResultSet resultSet)
            throws SQLException {
                rowProcessor.processRow(resultSet);
                return true;
            }
        });
    }

    /**
     * Fetches all the next rows of a result set and processes it through a
     * <code>DbRowProcessor</code>.
     *
     * @param resultSet    a valid <code>ResultSet</code> instance
     * @param rowProcessor a <code>DbRowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched rows
     * @return <code>true</code> if rows were fetched; or
     * <p><code>false</code> if the result set contained no rows.
     * @throws DatabaseException when an error occurred during the fetch of
     *                           the next rows in the result set
     * @see DbRowProcessor
     * @since 1.0
     */
    public boolean fetchAll(ResultSet resultSet, DbRowProcessor rowProcessor)
    throws DatabaseException {
        if (null == rowProcessor) throw new IllegalArgumentException("rowProcessor can't be null.");

        var result = false;

        while (fetch(resultSet, rowProcessor)) {
            result = true;

            if (rowProcessor != null &&
                !rowProcessor.wasSuccessful()) {
                break;
            }
        }

        return result;
    }

    /**
     * Convenience alternative to {@link #fetchAll(ResultSet, DbRowProcessor)))}
     * that uses a simplified <code>RowProcessor</code> that can be implemented with a lambda.
     *
     * @param resultSet    a valid <code>ResultSet</code> instance
     * @param rowProcessor a <code>RowProcessor</code> instance, if it's
     *                     <code>null</code> no processing will be performed on the fetched rows
     * @return <code>true</code> if rows were fetched; or
     * <p><code>false</code> if the result set contained no rows.
     * @throws DatabaseException when an error occurred during the fetch of
     *                           the next rows in the result set
     * @see #fetchAll(ResultSet, DbRowProcessor)
     * @see RowProcessor
     * @since 1.0
     */
    public boolean fetchAll(ResultSet resultSet, RowProcessor rowProcessor)
    throws DatabaseException {
        if (rowProcessor == null) {
            return fetchAll(resultSet, (DbRowProcessor) null);
        }

        return fetchAll(resultSet, new DbRowProcessor() {
            public boolean processRow(ResultSet resultSet)
            throws SQLException {
                rowProcessor.processRow(resultSet);
                return true;
            }
        });
    }

    /**
     * Obtains a <code>DbConnection</code> of this <code>DbQueryManager</code>'s
     * <code>Datasource</code>. Functions exactly as the wrapped {@link
     * Datasource#getConnection()} method.
     *
     * @return the requested <code>DbConnection</code>
     * @throws DatabaseException see {@link Datasource#getConnection()}
     * @see Datasource#getConnection()
     * @since 1.0
     */
    public DbConnection getConnection()
    throws DatabaseException {
        return datasource_.getConnection();
    }

    /**
     * Retrieves the <code>Datasource</code> of this
     * <code>DbQueryManager</code>.
     *
     * @return the requested <code>Datasource</code>
     * @since 1.0
     */
    public Datasource getDatasource() {
        return datasource_;
    }

    /**
     * Simply clones the instance with the default clone method. This creates
     * a shallow copy of all fields and the clone will in fact just be another
     * reference to the same underlying data. The independence of each cloned
     * instance is consciously not respected since they rely on resources that
     * can't be cloned.
     *
     * @return a clone of this instance
     * @since 1.0
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.database").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }

    private void defensiveClose(InputStream stream) {
        if (null == stream) {
            return;
        }

        try {
            stream.close();
        } catch (IOException e) {
            // couldn't close stream since it probably already has been
            // closed after an exception
            // proceed without reporting an error message.
        }
    }

    private void defensiveClose(Reader reader) {
        if (null == reader) {
            return;
        }

        try {
            reader.close();
        } catch (IOException e) {
            // couldn't close reader since it probably already has been
            // closed after an exception
            // proceed without reporting an error message.
        }
    }

    private void defensiveClose(DbStatement statement) {
        if (null == statement) {
            return;
        }

        try {
            statement.close();
        } catch (DatabaseException e) {
            // couldn't close statement since it probably already has been
            // closed after an exception
            // proceed without reporting an error message.
        }
    }
}

