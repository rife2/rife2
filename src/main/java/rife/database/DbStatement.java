/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.config.RifeConfig;
import rife.database.exceptions.BatchExecutionErrorException;
import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.ExecutionErrorException;
import rife.database.exceptions.MissingResultsException;
import rife.database.exceptions.StatementCloseErrorException;
import rife.database.queries.Query;
import rife.database.queries.ReadQuery;
import rife.tools.ExceptionUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a wrapper around the regular JDBC {@code Statement} class. It
 * can only be instantiated by calling the {@code createStatement} method on
 * an existing {@code DbConnection} instance.
 * <p>This class hooks into the database connection pool and cleans up as much
 * as possible in case of errors. The thrown {@code DatabaseException}
 * exceptions should thus only be used for error reporting and not for
 * releasing resources used by the framework.
 * <p>The {@code execute} and {@code executeQuery} methods store
 * their result set in the executing {@code DbStatement} instance. It's
 * recommended to use the {@code DbQueryManager}'s {@code fetch}
 * method to process the result set. If needed, one can also use the
 * {@code getResultSet} method to manually process the results through
 * plain JDBC. However, when exceptions are thrown during this procedure, it's
 * also the responsability of the user to correctly clean up all resources.
 * <p>Additional methods have been implemented to facilitate the retrieval of
 * queries which return only a single field and to easily check if a query
 * returned any result rows.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see #executeQuery(String)
 * @see #execute(String)
 * @see #execute(String, int)
 * @see #execute(String, int[])
 * @see #execute(String, String[])
 * @see #getResultSet()
 * @see rife.database.DbConnection#createStatement
 * @see rife.database.DbQueryManager#fetch(ResultSet, DbRowProcessor)
 * @see java.sql.ResultSet
 * @see java.sql.Statement
 * @since 1.0
 */
public class DbStatement implements Cloneable, AutoCloseable {
    private DbResultSet resultSet_ = null;

    final Statement statement_;
    final DbConnection connection_;

    /**
     * Constructs a new {@code DbStatement} from an existing
     * {@code DbConnection} and {@code Statement}. This constructor
     * will never be called by a user of the api. The
     * {@code createStatement} of an existing {@code DbConnection}
     * instance should be used instead.
     *
     * @param connection a {@code DbConnection} instance
     * @param statement  a JDBC {@code Statement} instance
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    DbStatement(DbConnection connection, Statement statement)
    throws DatabaseException {
        assert connection != null;
        assert statement != null;

        connection_ = connection;
        statement_ = statement;
    }

    /**
     * Adds the given SQL command to the current list of commmands for this
     * {@code Statement} object. The commands in this list can be
     * executed as a batch by calling the method {@code executeBatch}.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql typically this is a static SQL {@code INSERT} or
     *            {@code UPDATE} statement
     * @throws DatabaseException if a database access error occurs, or the
     *                           driver does not support batch updates
     * @see #executeBatch
     * @since 1.0
     */
    public void addBatch(String sql)
    throws DatabaseException {
        try {
            statement_.addBatch(sql);
            traceBatch(sql);
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Cancels this {@code DbStatement} object if both the DBMS and
     * driver support aborting a SQL statement. This method can be used by one
     * thread to cancel a statement that is being executed by another thread.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public void cancel()
    throws DatabaseException {
        try {
            statement_.cancel();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Empties this {@code Statement} object's current list of SQL
     * commands.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @throws DatabaseException if a database access error occurs or the
     *                           driver does not support batch updates
     * @see #addBatch
     * @since 1.0
     */
    public void clearBatch()
    throws DatabaseException {
        try {
            statement_.clearBatch();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Clears all the warnings reported on this {@code DbStatement}
     * object. After a call to this method, the method
     * {@code getWarnings} will return {@code null} until a new
     * warning is reported for this {@code DbStatement} object.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public void clearWarnings()
    throws DatabaseException {
        try {
            statement_.clearWarnings();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Releases this {@code DbStatement} object's database and JDBC
     * resources immediately instead of waiting for this to happen when it is
     * automatically closed. It is generally good practice to release
     * resources as soon as you are finished with them to avoid tying up
     * database resources.
     * <p>Calling the method {@code close} on a {@code DbStatement}
     * object that is already closed has no effect.
     * <p><b>Note:</b> A {@code DbStatement} object is automatically
     * closed when it is garbage collected. When a {@code DbStatement}
     * object is closed, its current {@code ResultSet} object, if one
     * exists, is also closed.
     *
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public void close()
    throws DatabaseException {
        try {
            connection_.releaseStatement(this);

            // cleanup this statement
            cleanResultSet();

            statement_.close();
        } catch (SQLException e) {
            throw new StatementCloseErrorException(connection_.getDatasource(), e);
        }
    }

    protected long startTrace() {
        if (RifeConfig.database().getSqlDebugTrace()) {
            Logger logger = Logger.getLogger("rife.database");
            if (logger.isLoggable(Level.INFO)) {
                return System.currentTimeMillis();
            }
        }

        return 0;
    }

    protected void outputTrace(long start, String sql) {
        if (start != 0) {
            StringBuilder output = new StringBuilder();

            output.append(System.currentTimeMillis() - start);
            output.append("ms : ");
            output.append(sql);

            Logger.getLogger("rife.database").info(output.toString());
        }
    }

    protected void traceBatch(String sql) {
        if (RifeConfig.database().getSqlDebugTrace()) {
            Logger logger = Logger.getLogger("rife.database");
            if (logger.isLoggable(Level.INFO)) {
                logger.info("batched : " + sql);
            }
        }
    }

    /**
     * Executes the given SQL statement, which may return multiple results. In
     * some (uncommon) situations, a single SQL statement may return multiple
     * result sets and/or update counts. Normally you can ignore this unless
     * you are (1) executing a stored procedure that you know may return
     * multiple results or (2) you are dynamically executing an unknown SQL
     * string.
     * <p>The {@code execute} method executes a SQL statement and
     * indicates the form of the first result. You must then use the methods
     * {@code getResultSet} or {@code getUpdateCount} to retrieve
     * the result, and {@code getMoreResults} to move to any subsequent
     * result(s).
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql any SQL statement
     * @return {@code true} if the first result is a
     * {@code ResultSet} object; or
     * <p>{@code false} if it is an update count or there are no results
     * @throws DatabaseException if a database access error occurs
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @since 1.0
     */
    public boolean execute(String sql)
    throws DatabaseException {
        try {
            waitForConnection();

            cleanResultSet();

            long start = startTrace();
            boolean result = statement_.execute(sql);
            outputTrace(start, sql);

            setResultset(statement_.getResultSet());

            return result;
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that any auto-generated keys should be made
     * available for retrieval. The driver will ignore this signal if the SQL
     * statement is not an {@code INSERT} statement.
     * <p>In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts. Normally you can ignore this
     * unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an unknown
     * SQL string.
     * <p>The {@code execute} method executes a SQL statement and
     * indicates the form of the first result. You must then use the methods
     * {@code getResultSet} or {@code getUpdateCount} to retrieve
     * the result, and {@code getMoreResults} to move to any subsequent
     * result(s).
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql               any SQL statement
     * @param autoGeneratedKeys a constant indicating whether auto-generated
     *                          keys should be made available for retrieval using the method
     *                          {@code getGeneratedKeys}; one of the following constants:
     *                          {@code Statement.RETURN_GENERATED_KEYS} or
     *                          {@code Statement.NO_GENERATED_KEYS}
     * @return {@code true} if the first result is a
     * {@code ResultSet} object; or
     * <p>{@code false} if it is an update count or there are no results
     * @throws DatabaseException if a database access error occurs
     * @see Statement
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @see #getGeneratedKeys
     * @since 1.0
     */
    public boolean execute(String sql, int autoGeneratedKeys)
    throws DatabaseException {
        try {
            waitForConnection();

            cleanResultSet();

            long start = startTrace();
            boolean result = statement_.execute(sql, autoGeneratedKeys);
            outputTrace(start, sql);

            setResultset(statement_.getResultSet());

            return result;
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that the auto-generated keys indicated in the
     * given array should be made available for retrieval. This array contains
     * the indexes of the columns in the target table that contain the
     * auto-generated keys that should be made available. The driver will
     * ignore the array if the given SQL statement is not an
     * {@code INSERT} statement.
     * <p>Under some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts. Normally you can ignore this
     * unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an unknown
     * SQL string.
     * <p>The {@code execute} method executes a SQL statement and
     * indicates the form of the first result. You must then use the methods
     * {@code getResultSet} or {@code getUpdateCount} to retrieve
     * the result, and {@code getMoreResults} to move to any subsequent
     * result(s).
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql           any SQL statement
     * @param columnIndexes an array of the indexes of the columns in the
     *                      inserted row that should be made available for retrieval by a call to
     *                      the method {@code getGeneratedKeys}
     * @return {@code true} if the first result is a
     * {@code ResultSet} object; or
     * <p>{@code false} if it is an update count or there are no results
     * @throws DatabaseException if a database access error occurs
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @since 1.0
     */
    public boolean execute(String sql, int[] columnIndexes)
    throws DatabaseException {
        try {
            waitForConnection();

            cleanResultSet();

            long start = startTrace();
            boolean result = statement_.execute(sql, columnIndexes);
            outputTrace(start, sql);

            setResultset(statement_.getResultSet());

            return result;
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that the auto-generated keys indicated in the
     * given array should be made available for retrieval. This array contains
     * the names of the columns in the target table that contain the
     * auto-generated keys that should be made available. The driver will
     * ignore the array if the given SQL statement is not an
     * {@code INSERT} statement.
     * <p>In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts. Normally you can ignore this
     * unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an unknown
     * SQL string.
     * <p>The {@code execute} method executes a SQL statement and
     * indicates the form of the first result. You must then use the methods
     * {@code getResultSet} or {@code getUpdateCount} to retrieve
     * the result, and {@code getMoreResults} to move to any subsequent
     * result(s).
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql         any SQL statement
     * @param columnNames an array of the names of the columns in the inserted
     *                    row that should be made available for retrieval by a call to the method
     *                    {@code getGeneratedKeys}
     * @return {@code true} if the next result is a
     * {@code ResultSet} object; or
     * <p>{@code false} if it is an update count or there are no more
     * results
     * @throws DatabaseException if a database access error occurs
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @see #getGeneratedKeys
     * @since 1.0
     */
    public boolean execute(String sql, String[] columnNames)
    throws DatabaseException {
        try {
            waitForConnection();

            cleanResultSet();

            long start = startTrace();
            boolean result = statement_.execute(sql, columnNames);
            outputTrace(start, sql);

            setResultset(statement_.getResultSet());

            return result;
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Submits a batch of commands to the database for execution and if all
     * commands execute successfully, returns an array of update counts. The
     * {@code int} elements of the array that is returned are ordered to
     * correspond to the commands in the batch, which are ordered according to
     * the order in which they were added to the batch. The elements in the
     * array returned by the method {@code executeBatch} may be one of
     * the following:
     * <ol>
     * <li>A number greater than or equal to zero -- indicates that the
     * command was processed successfully and is an update count giving the
     * number of rows in the database that were affected by the command's
     * execution
     * <li>A value of {@code SUCCESS_NO_INFO} -- indicates that the
     * command was processed successfully but that the number of rows affected
     * is unknown
     * <p>If one of the commands in a batch update fails to execute properly,
     * this method throws a {@code BatchUpdateException}, and a JDBC
     * driver may or may not continue to process the remaining commands in the
     * batch. However, the driver's behavior must be consistent with a
     * particular DBMS, either always continuing to process commands or never
     * continuing to process commands. If the driver continues processing
     * after a failure, the array returned by the method
     * {@code BatchUpdateException.getUpdateCounts} will contain as many
     * elements as there are commands in the batch, and at least one of the
     * elements will be the following:
     * <p>
     * <li>A value of {@code EXECUTE_FAILED} -- indicates that the
     * command failed to execute successfully and occurs only if a driver
     * continues to process commands after a command fails
     * </ol>
     * <p>A driver is not required to implement this method.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return an array of update counts containing one element for each
     * command in the batch. The elements of the array are ordered according
     * to the order in which commands were added to the batch.
     * @throws DatabaseException if a database access error occurs or the
     *                           driver does not support batch statements. The cause is a {@link
     *                           java.sql.BatchUpdateException} (a subclass of {@code SQLException})
     *                           if one of the commands sent to the database fails to execute properly
     *                           or attempts to return a result set.
     * @since 1.0
     */
    public int[] executeBatch()
    throws DatabaseException {
        try {
            waitForConnection();

            return statement_.executeBatch();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new BatchExecutionErrorException(connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given SQL statement. The returned {@code ResultSet}
     * object is stored and can be retrieved with the
     * {@code getResultSet} method.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql a SQL statement to be sent to the database, typically a
     *            static SQL {@code SELECT} statement
     * @throws DatabaseException if a database access error occurs or the
     *                           given SQL statement produces anything other than a single
     *                           {@code ResultSet} object
     * @see #getResultSet
     * @since 1.0
     */
    public void executeQuery(String sql)
    throws DatabaseException {
        try {
            waitForConnection();

            cleanResultSet();

            long start = startTrace();
            statement_.execute(sql);
            outputTrace(start, sql);

            setResultset(statement_.getResultSet());

            return;
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given {@code Query} builder's SQL statement. The
     * returned {@code ResultSet} object is stored and can be retrieved
     * with the {@code getResultSet} method.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param query a {@code Query} builder instance which provides a SQL
     *              statement that queries the database
     * @throws DatabaseException if a database access error occurs or the
     *                           given SQL statement produces anything other than a single
     *                           {@code ResultSet} object
     * @see #getResultSet
     * @since 1.0
     */
    public void executeQuery(ReadQuery query)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        executeQuery(query.getSql());
    }

    /**
     * Executes the given SQL statement, which may be an {@code INSERT},
     * {@code UPDATE}, or {@code DELETE} statement or an SQL
     * statement that returns nothing, such as an SQL DDL statement.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql an SQL {@code INSERT}, {@code UPDATE} or
     *            {@code DELETE} statement or a SQL statement that returns nothing
     * @return the row count for {@code INSERT}, {@code UPDATE} or
     * {@code DELETE} statements; or
     * <p>{@code 0} for SQL statements that return nothing
     * @throws DatabaseException if a database access error occurs or the
     *                           given SQL statement produces a {@code ResultSet} object
     * @since 1.0
     */
    public int executeUpdate(String sql)
    throws DatabaseException {
        try {
            waitForConnection();

            long start = startTrace();
            int result = statement_.executeUpdate(sql);
            outputTrace(start, sql);

            return result;
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given {@code Query} builder's SQL statement, which
     * may be an {@code INSERT}, {@code UPDATE}, or
     * {@code DELETE} statement or a SQL statement that returns nothing,
     * such as an SQL DDL statement.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param query a {@code Query} builder instance which provides a SQL
     *              statement that modifies the database
     * @return the row count for {@code INSERT}, {@code UPDATE} or
     * {@code DELETE} statements; or
     * <p>{@code 0} for SQL statements that return nothing
     * @throws DatabaseException if a database access error occurs or the
     *                           given SQL statement produces a {@code ResultSet} object
     * @since 1.0
     */
    public int executeUpdate(Query query)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        return executeUpdate(query.getSql());
    }

    /**
     * Retrieves the direction for fetching rows from database tables that is
     * the default for result sets generated from this
     * {@code DbStatement} object. If this {@code DbStatement}
     * object has not set a fetch direction by calling the method
     * {@code setFetchDirection}, the return value is
     * implementation-specific.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the default fetch direction for result sets generated from this
     * {@code DbStatement} object
     * @throws DatabaseException if a database access error occurs
     * @see #setFetchDirection
     * @since 1.0
     */
    public int getFetchDirection()
    throws DatabaseException {
        try {
            return statement_.getFetchDirection();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the number of result set rows that is the default fetch size
     * for {@code ResultSet} objects generated from this
     * {@code DbStatement} object. If this {@code DbStatement}
     * object has not set a fetch size by calling the method
     * {@code setFetchSize}, the return value is implementation-specific.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the default fetch size for result sets generated from this
     * {@code DbStatement} object
     * @throws DatabaseException if a database access error occurs
     * @see #setFetchSize
     * @since 1.0
     */
    public int getFetchSize()
    throws DatabaseException {
        try {
            return statement_.getFetchSize();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves any auto-generated keys created as a result of executing this
     * {@code DbStatement} object. If this DbStatement object did not
     * generate any keys, an empty {@code DbResultSet} object is
     * returned.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return a {@code DbResultSet} object containing the auto-generated
     * key(s) generated by the execution of this {@code DbStatement}
     * object
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public DbResultSet getGeneratedKeys()
    throws DatabaseException {
        try {
            return wrapWithDbResultSet(statement_.getGeneratedKeys());
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the first auto-generated key created as a result of executing
     * this {@code DbStatement} object as an integer. If this
     * {@code DbStatement} object did not generate any keys, an exception
     * is thrown.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first auto-generated key as an integer
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public int getFirstGeneratedIntKey()
    throws DatabaseException {
        try {
            DbResultSet resultset = getGeneratedKeys();
            resultset.next();
            return resultset.getInt(1);
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the maximum number of bytes that can be returned for
     * character and binary column values in a {@code ResultSet} object
     * produced by this {@code Statement} object. This limit applies only
     * to {@code BINARY}, {@code VARBINARY},
     * {@code LONGVARBINARY}, {@code CHAR}, {@code VARCHAR},
     * and {@code LONGVARCHAR} columns. If the limit is exceeded, the
     * excess data is silently discarded.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the current column size limit for columns storing character and
     * binary values; or
     * <p>{@code 0} if there's no limit
     * @throws DatabaseException if a database access error occurs
     * @see #setMaxFieldSize
     * @since 1.0
     */
    public int getMaxFieldSize()
    throws DatabaseException {
        try {
            return statement_.getMaxFieldSize();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the maximum number of rows that a {@code ResultSet}
     * object produced by this {@code DbStatement} object can contain. If
     * this limit is exceeded, the excess rows are silently dropped.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the current maximum number of rows for a {@code ResultSet}
     * object produced by this {@code Statement} object; or
     * <p>{@code 0} if there's no limit
     * @throws DatabaseException if a database access error occurs
     * @see #setMaxRows
     * @since 1.0
     */
    public int getMaxRows()
    throws DatabaseException {
        try {
            return statement_.getMaxRows();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Moves to this {@code DbStatement} object's next result, returns
     * {@code true} if it is a {@code ResultSet} object, and
     * implicitly closes any current {@code ResultSet} object(s) obtained
     * with the method {@code getResultSet}.
     * <p>There are no more results when the following is true:
     * <pre>
     * {@code (!getMoreResults() &amp;&amp; (getUpdateCount() == -1)}
     * </pre>
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return {@code true} if the next result is a
     * {@code ResultSet} object; or
     * <p>{@code false} if it is an update count or there are no more
     * results
     * @throws DatabaseException if a database access error occurs
     * @see #execute
     * @since 1.0
     */
    public boolean getMoreResults()
    throws DatabaseException {
        try {
            cleanResultSet();

            boolean result = statement_.getMoreResults();
            setResultset(statement_.getResultSet());
            return result;
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Moves to this {@code DbStatement} object's next result, deals with
     * any current {@code ResultSet} object(s) according to the
     * instructions specified by the given flag, and returns {@code true}
     * if the next result is a {@code ResultSet} object.
     * <p>There are no more results when the following is true:
     * <pre>
     *      {@code (!getMoreResults() &amp;&amp; (getUpdateCount() == -1)}
     * </pre>
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param current one of the following {@code Statement} constants
     *                indicating what should happen to current {@code ResultSet} objects
     *                obtained using the method {@code getResultSet}:
     *                {@code CLOSE_CURRENT_RESULT}, {@code KEEP_CURRENT_RESULT}, or
     *                {@code CLOSE_ALL_RESULTS}
     * @return {@code true} if the next result is a
     * {@code ResultSet} object; or
     * <p>{@code false} if it is an update count or there are no more
     * results
     * @throws DatabaseException if a database access error occurs
     * @see Statement
     * @see #execute
     * @since 1.0
     */
    public boolean getMoreResults(int current)
    throws DatabaseException {
        try {
            cleanResultSet();

            boolean result = statement_.getMoreResults(current);
            setResultset(statement_.getResultSet());
            return result;
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the number of seconds the driver will wait for a
     * {@code DbStatement} object to execute. If the limit is exceeded, a
     * {@code DatabaseException} is thrown.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the current query timeout limit in seconds; or
     * <p>{@code 0} if there's no limit
     * @throws DatabaseException if a database access error occurs
     * @see #setQueryTimeout
     * @since 1.0
     */
    public int getQueryTimeout()
    throws DatabaseException {
        try {
            return statement_.getQueryTimeout();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Reports whether the last column read had a value of SQL
     * {@code NULL}. Note that you must first call one of the getter
     * methods on a column to try to read its value and then call the method
     * {@code wasNull} to see if the value read was SQL {@code NULL}.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return {@code true} if the last column value read was SQL
     * {@code NULL}; or
     * <p>{@code false} otherwise
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public boolean wasNull()
    throws DatabaseException {
        if (null == resultSet_) {
            throw new MissingResultsException(getConnection().getDatasource());
        }

        try {
            return resultSet_.wasNull();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the result set concurrency for {@code ResultSet} objects
     * generated by this {@code DbStatement} object.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return either {@code ResultSet.CONCUR_READ_ONLY} or
     * {@code ResultSet.CONCUR_UPDATABLE}
     * @throws DatabaseException if a database access error occurs
     * @see ResultSet
     * @since 1.0
     */
    public int getResultSetConcurrency()
    throws DatabaseException {
        try {
            return statement_.getResultSetConcurrency();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the result set holdability for {@code ResultSet} objects
     * generated by this {@code DbStatement} object.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return either {@code ResultSet.HOLD_CURSORS_OVER_COMMIT} or
     * {@code ResultSet.CLOSE_CURSORS_AT_COMMIT}
     * @throws DatabaseException if a database access error occurs
     * @see ResultSet
     * @since 1.0
     */
    public int getResultSetHoldability()
    throws DatabaseException {
        try {
            return statement_.getResultSetHoldability();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the result set type for {@code ResultSet} objects
     * generated by this {@code DbStatement} object.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return one of {@code ResultSet.TYPE_FORWARD_ONLY},
     * {@code ResultSet.TYPE_SCROLL_INSENSITIVE}, or
     * {@code ResultSet.TYPE_SCROLL_SENSITIVE}
     * @throws DatabaseException if a database access error occurs
     * @see ResultSet
     * @since 1.0
     */
    public int getResultSetType()
    throws DatabaseException {
        try {
            return statement_.getResultSetType();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the current result as an update count; if the result is a
     * {@code ResultSet} object or there are no more results, -1 is
     * returned. This method should be called only once per result.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the current result as an update count; or
     * <p>{@code -1} if the current result is a {@code ResultSet}
     * object or there are no more results
     * @throws DatabaseException if a database access error occurs
     * @see #execute
     * @since 1.0
     */
    public int getUpdateCount()
    throws DatabaseException {
        try {
            return statement_.getUpdateCount();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the first warning reported by calls on this
     * {@code Statement} object. Subsequent {@code DbStatement}
     * object warnings will be chained to this {@code SQLWarning} object.
     * <p>The warning chain is automatically cleared each time a statement is
     * (re)executed. This method may not be called on a closed
     * {@code DbStatement} object; doing so will cause an
     * {@code SQLException} to be thrown.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     * <p><b>Note:</b> If you are processing a {@code ResultSet} object,
     * any warnings associated with reads on that {@code ResultSet}
     * object will be chained on it rather than on the
     * {@code DbStatement} object that produced it.
     *
     * @return the first {@code SQLWarning} object; or
     * <p>{@code null} if there are no warnings
     * @throws DatabaseException if a database access error occurs or this
     *                           method is called on a closed statement
     * @since 1.0
     */
    public SQLWarning getWarnings()
    throws DatabaseException {
        try {
            return statement_.getWarnings();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the current result as a {@code ResultSet} object. This
     * method returns the internally stored result and can be called as many
     * times as wanted, contrary to the regular JDBC counterpart.
     *
     * @return the current result as a {@code ResultSet} object; or
     * <p>{@code NULL} if the result is an update count.
     * @see #execute
     * @since 1.0
     */
    public DbResultSet getResultSet() {
        return resultSet_;
    }

    /**
     * Gives the driver a hint as to the direction in which rows will be
     * processed in {@code ResultSet} objects created using this
     * {@code DbStatement} object. The default value is
     * {@code ResultSet.FETCH_FORWARD}.
     * <p>Note that this method sets the default fetch direction for result
     * sets generated by this {@code DbStatement} object. Each result set
     * has its own methods for getting and setting its own fetch direction.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param direction the initial direction for processing rows
     * @throws DatabaseException if a database access error occurs or the
     *                           given direction is not one of {@code ResultSet.FETCH_FORWARD},
     *                           {@code ResultSet.FETCH_REVERSE}, or
     *                           {@code ResultSet.FETCH_UNKNOWN}
     * @see #getFetchDirection
     * @see ResultSet
     * @since 1.0
     */
    public void setFetchDirection(int direction)
    throws DatabaseException {
        try {
            statement_.setFetchDirection(direction);
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should be
     * fetched from the database when more rows are needed. The number of rows
     * specified affects only result sets created using this statement. If the
     * value specified is zero, then the hint is ignored. The default value is
     * zero.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param rows the number of rows to fetch
     * @throws DatabaseException if a database access error occurs, or the
     *                           condition 0 &lt;= {@code rows} &lt;=
     *                           {@code this.getMaxRows()} is not satisfied.
     * @see #getFetchSize
     * @see #getMaxRows
     * @since 1.0
     */
    public void setFetchSize(int rows)
    throws DatabaseException {
        try {
            statement_.setFetchSize(rows);
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Sets the limit for the maximum number of bytes in a
     * {@code ResultSet} column storing character or binary values to the
     * given number of bytes. This limit applies only to {@code BINARY},
     * {@code VARBINARY}, {@code LONGVARBINARY}, {@code CHAR},
     * {@code VARCHAR}, and {@code LONGVARCHAR} fields. If the limit
     * is exceeded, the excess data is silently discarded. For maximum
     * portability, use values greater than 256.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param max the new column size limit in bytes; zero means there is no
     *            limit
     * @throws DatabaseException if a database access error occurs or the
     *                           condition max &gt;= 0 is not satisfied
     * @see #getMaxFieldSize
     * @since 1.0
     */
    public void setMaxFieldSize(int max)
    throws DatabaseException {
        try {
            statement_.setMaxFieldSize(max);
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Sets the limit for the maximum number of rows that any
     * {@code ResultSet} object can contain to the given number. If the
     * limit is exceeded, the excess rows are silently dropped.
     * <p>If an exception is thrown, this {@code DbStatement} is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param max the new max rows limit; zero means there is no limit
     * @throws DatabaseException if a database access error occurs or the
     *                           condition max &gt;= 0 is not satisfied
     * @see #getMaxRows
     * @since 1.0
     */
    public void setMaxRows(int max)
    throws DatabaseException {
        try {
            statement_.setMaxRows(max);
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Sets the number of seconds the driver will wait for a
     * {@code DbStatement} object to execute to the given number of
     * seconds. If the limit is exceeded, an {@code DatabaseException} is
     * thrown.
     *
     * @param max the new query timeout limit in seconds; zero means there is
     *            no limit
     * @throws DatabaseException if a database access error occurs or the
     *                           condition seconds &gt;= 0 is not satisfied
     * @see #getQueryTimeout
     * @since 1.0
     */
    public void setQueryTimeout(int max)
    throws DatabaseException {
        try {
            statement_.setQueryTimeout(max);
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw new DatabaseException(e);
        }
    }

    /**
     * Returns the {@code DbConnection} object from which this
     * {@code DbStatement} object has been instantiated.
     *
     * @return the instantiating {@code DbConnection} object.
     * @since 1.0
     */
    public DbConnection getConnection() {
        return connection_;
    }

    /**
     * Waits until the {@code DbConnection} method is available for use.
     * This method is used by all the execution methods to effectively
     * integrate with the connection pool.
     *
     * @throws DatabaseException when a database access error occurs or the
     *                           connection isn't open or has timed-out
     * @since 1.0
     */
    void waitForConnection()
    throws DatabaseException {
        if (connection_.isClosed()) {
            connection_.handleException();
            throw new DatabaseException("The connection is not open.");
        }

        while (true) {
            if (!connection_.isFree()) {
                try {
                    synchronized (connection_) {
                        connection_.wait();
                    }
                } catch (InterruptedException e) {
                    throw new DatabaseException("Timeout while waiting for the connection to become available.");
                }
            } else {
                break;
            }
        }
    }

    /**
     * Checks if there's a {@code ResultSet} object present.
     *
     * @return {@code true} if a {@code ResultSet} object is
     * available; or
     * <p>{@code false} otherwise.
     * @since 1.0
     */
    boolean hasResultset() {
        return null != resultSet_;
    }

    /**
     * Set the current {@code ResultSet} object and cleans up the
     * previous {@code ResultSet} object automatically.
     *
     * @param resultSet the new current {@code ResultSet} object
     * @throws DatabaseException if a database access error occurred.
     * @since 1.0
     */
    protected void setResultset(ResultSet resultSet)
    throws DatabaseException {
        if (null == resultSet) {
            resultSet_ = null;
        } else {
            resultSet_ = wrapWithDbResultSet(resultSet);
        }
    }

    private DbResultSet wrapWithDbResultSet(ResultSet resultSet)
    throws DatabaseException {
        return new DbResultSet(this, resultSet);
    }

    /**
     * Cleans up and closes the current {@code ResultSet} object.
     *
     * @throws DatabaseException if a database access error occurred.
     * @since 1.0
     */
    void cleanResultSet()
    throws DatabaseException {
        if (null != resultSet_) {
            try {
                resultSet_.close();
                resultSet_ = null;
            } catch (SQLException e) {
                resultSet_ = null;
                close();
                throw new DatabaseException(e);
            }
        }
    }

    /**
     * Performs the cleanup logic in case an exception is thrown during
     * execution. The statement will be closed and if a transaction is active,
     * it will be automatically rolled back.
     *
     * @param e the exception that was thrown
     * @throws DatabaseException when an error occurs during the cleanup of
     *                           the connection, or when an error occurs during the roll-back.
     */
    protected void handleException(Exception e)
    throws DatabaseException {
        synchronized (this) {
            try {
                close();
            } catch (DatabaseException ignored) {
                // this is a defensive close, if it can't be closed again, it
                // probably already is
            }

            if (connection_.isTransactionValidForThread()) {
                connection_.rollback();
            } else {
                synchronized (connection_) {
                    connection_.notifyAll();
                }
            }

            // looks for conditions where the network connection was interrupted,
            // requiring the database connection to handle the exception, as well
            // as this statement
            if (e instanceof SQLException sql_exception) {
                if (connection_.getDatasource().getAliasedDriver().equals(Datasource.DRIVER_NAME_PGSQL)) {
                    var state = sql_exception.getSQLState();
                    if (state != null && state.startsWith("57P")) { // Class 57 — Operator Intervention : https://www.postgresql.org/docs/current/errcodes-appendix.html
                        connection_.handleException();
                    }
                } else if (connection_.getDatasource().getAliasedDriver().equals(Datasource.DRIVER_NAME_MYSQL)) {
                    if (e.getCause() != null) {
                        if (IOException.class.isAssignableFrom(e.getCause().getClass())) { // MariaDB
                            connection_.handleException();
                        } else if (e.getCause().getCause() != null && IOException.class.isAssignableFrom(e.getCause().getCause().getClass())) { // MySQL
                            connection_.handleException();
                        }
                    }
                } else if (connection_.getDatasource().getAliasedDriver().equals(Datasource.DRIVER_NAME_ORACLE)) {
                    if (sql_exception.getErrorCode() == 17410) { // ORA-17410: No more data to read from socket
                        connection_.handleException();
                    }
                }
            }
        }
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
            Logger.getLogger("rife.database").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }
}

