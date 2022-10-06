/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a wrapper around the regular JDBC <code>Statement</code> class. It
 * can only be instantiated by calling the <code>createStatement</code> method on
 * an existing <code>DbConnection</code> instance.
 * <p>This class hooks into the database connection pool and cleans up as much
 * as possible in case of errors. The thrown <code>DatabaseException</code>
 * exceptions should thus only be used for error reporting and not for
 * releasing resources used by the framework.
 * <p>The <code>execute</code> and <code>executeQuery</code> methods store
 * their result set in the executing <code>DbStatement</code> instance. It's
 * recommended to use the <code>DbQueryManager</code>'s <code>fetch</code>
 * method to process the result set. If needed, one can also use the
 * <code>getResultSet</code> method to manually process the results through
 * plain JDBC. However, when exceptions are thrown during this procedure, it's
 * also the responsability of the user to correctly clean up all resources.
 * <p>Additional methods have been implemented to facilitate the retrieval of
 * queries which return only a single field and to easily check if a query
 * returned any result rows.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
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
     * Constructs a new <code>DbStatement</code> from an existing
     * <code>DbConnection</code> and <code>Statement</code>. This constructor
     * will never be called by a user of the api. The
     * <code>createStatement</code> of an existing <code>DbConnection</code>
     * instance should be used instead.
     *
     * @param connection a <code>DbConnection</code> instance
     * @param statement  a JDBC <code>Statement</code> instance
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
     * <code>Statement</code> object. The commands in this list can be
     * executed as a batch by calling the method <code>executeBatch</code>.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql typically this is a static SQL <code>INSERT</code> or
     *            <code>UPDATE</code> statement
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Cancels this <code>DbStatement</code> object if both the DBMS and
     * driver support aborting a SQL statement. This method can be used by one
     * thread to cancel a statement that is being executed by another thread.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Empties this <code>Statement</code> object's current list of SQL
     * commands.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Clears all the warnings reported on this <code>DbStatement</code>
     * object. After a call to this method, the method
     * <code>getWarnings</code> will return <code>null</code> until a new
     * warning is reported for this <code>DbStatement</code> object.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Releases this <code>DbStatement</code> object's database and JDBC
     * resources immediately instead of waiting for this to happen when it is
     * automatically closed. It is generally good practice to release
     * resources as soon as you are finished with them to avoid tying up
     * database resources.
     * <p>Calling the method <code>close</code> on a <code>DbStatement</code>
     * object that is already closed has no effect.
     * <p><b>Note:</b> A <code>DbStatement</code> object is automatically
     * closed when it is garbage collected. When a <code>DbStatement</code>
     * object is closed, its current <code>ResultSet</code> object, if one
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
     * <p>The <code>execute</code> method executes a SQL statement and
     * indicates the form of the first result. You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code> to retrieve
     * the result, and <code>getMoreResults</code> to move to any subsequent
     * result(s).
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql any SQL statement
     * @return <code>true</code> if the first result is a
     * <code>ResultSet</code> object; or
     * <p><code>false</code> if it is an update count or there are no results
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
        } catch (SQLException e) {
            handleException();
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that any auto-generated keys should be made
     * available for retrieval. The driver will ignore this signal if the SQL
     * statement is not an <code>INSERT</code> statement.
     * <p>In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts. Normally you can ignore this
     * unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an unknown
     * SQL string.
     * <p>The <code>execute</code> method executes a SQL statement and
     * indicates the form of the first result. You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code> to retrieve
     * the result, and <code>getMoreResults</code> to move to any subsequent
     * result(s).
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql               any SQL statement
     * @param autoGeneratedKeys a constant indicating whether auto-generated
     *                          keys should be made available for retrieval using the method
     *                          <code>getGeneratedKeys</code>; one of the following constants:
     *                          <code>Statement.RETURN_GENERATED_KEYS</code> or
     *                          <code>Statement.NO_GENERATED_KEYS</code>
     * @return <code>true</code> if the first result is a
     * <code>ResultSet</code> object; or
     * <p><code>false</code> if it is an update count or there are no results
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
        } catch (SQLException e) {
            handleException();
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
     * <code>INSERT</code> statement.
     * <p>Under some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts. Normally you can ignore this
     * unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an unknown
     * SQL string.
     * <p>The <code>execute</code> method executes a SQL statement and
     * indicates the form of the first result. You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code> to retrieve
     * the result, and <code>getMoreResults</code> to move to any subsequent
     * result(s).
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql           any SQL statement
     * @param columnIndexes an array of the indexes of the columns in the
     *                      inserted row that should be made available for retrieval by a call to
     *                      the method <code>getGeneratedKeys</code>
     * @return <code>true</code> if the first result is a
     * <code>ResultSet</code> object; or
     * <p><code>false</code> if it is an update count or there are no results
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
        } catch (SQLException e) {
            handleException();
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
     * <code>INSERT</code> statement.
     * <p>In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts. Normally you can ignore this
     * unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an unknown
     * SQL string.
     * <p>The <code>execute</code> method executes a SQL statement and
     * indicates the form of the first result. You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code> to retrieve
     * the result, and <code>getMoreResults</code> to move to any subsequent
     * result(s).
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql         any SQL statement
     * @param columnNames an array of the names of the columns in the inserted
     *                    row that should be made available for retrieval by a call to the method
     *                    <code>getGeneratedKeys</code>
     * @return <code>true</code> if the next result is a
     * <code>ResultSet</code> object; or
     * <p><code>false</code> if it is an update count or there are no more
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
        } catch (SQLException e) {
            handleException();
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Submits a batch of commands to the database for execution and if all
     * commands execute successfully, returns an array of update counts. The
     * <code>int</code> elements of the array that is returned are ordered to
     * correspond to the commands in the batch, which are ordered according to
     * the order in which they were added to the batch. The elements in the
     * array returned by the method <code>executeBatch</code> may be one of
     * the following:
     * <ol>
     * <li>A number greater than or equal to zero -- indicates that the
     * command was processed successfully and is an update count giving the
     * number of rows in the database that were affected by the command's
     * execution
     * <li>A value of <code>SUCCESS_NO_INFO</code> -- indicates that the
     * command was processed successfully but that the number of rows affected
     * is unknown
     * <p>If one of the commands in a batch update fails to execute properly,
     * this method throws a <code>BatchUpdateException</code>, and a JDBC
     * driver may or may not continue to process the remaining commands in the
     * batch. However, the driver's behavior must be consistent with a
     * particular DBMS, either always continuing to process commands or never
     * continuing to process commands. If the driver continues processing
     * after a failure, the array returned by the method
     * <code>BatchUpdateException.getUpdateCounts</code> will contain as many
     * elements as there are commands in the batch, and at least one of the
     * elements will be the following:
     * <p>
     * <li>A value of <code>EXECUTE_FAILED</code> -- indicates that the
     * command failed to execute successfully and occurs only if a driver
     * continues to process commands after a command fails
     * </ol>
     * <p>A driver is not required to implement this method.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return an array of update counts containing one element for each
     * command in the batch. The elements of the array are ordered according
     * to the order in which commands were added to the batch.
     * @throws DatabaseException if a database access error occurs or the
     *                           driver does not support batch statements. The cause is a {@link
     *                           java.sql.BatchUpdateException} (a subclass of <code>SQLException</code>)
     *                           if one of the commands sent to the database fails to execute properly
     *                           or attempts to return a result set.
     * @since 1.0
     */
    public int[] executeBatch()
    throws DatabaseException {
        try {
            waitForConnection();

            return statement_.executeBatch();
        } catch (SQLException e) {
            handleException();
            throw new BatchExecutionErrorException(connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given SQL statement. The returned <code>ResultSet</code>
     * object is stored and can be retrieved with the
     * <code>getResultSet</code> method.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql a SQL statement to be sent to the database, typically a
     *            static SQL <code>SELECT</code> statement
     * @throws DatabaseException if a database access error occurs or the
     *                           given SQL statement produces anything other than a single
     *                           <code>ResultSet</code> object
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
        } catch (SQLException e) {
            handleException();
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given <code>Query</code> builder's SQL statement. The
     * returned <code>ResultSet</code> object is stored and can be retrieved
     * with the <code>getResultSet</code> method.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param query a <code>Query</code> builder instance which provides a SQL
     *              statement that queries the database
     * @throws DatabaseException if a database access error occurs or the
     *                           given SQL statement produces anything other than a single
     *                           <code>ResultSet</code> object
     * @see #getResultSet
     * @since 1.0
     */
    public void executeQuery(ReadQuery query)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        executeQuery(query.getSql());
    }

    /**
     * Executes the given SQL statement, which may be an <code>INSERT</code>,
     * <code>UPDATE</code>, or <code>DELETE</code> statement or an SQL
     * statement that returns nothing, such as an SQL DDL statement.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param sql an SQL <code>INSERT</code>, <code>UPDATE</code> or
     *            <code>DELETE</code> statement or a SQL statement that returns nothing
     * @return the row count for <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code> statements; or
     * <p><code>0</code> for SQL statements that return nothing
     * @throws DatabaseException if a database access error occurs or the
     *                           given SQL statement produces a <code>ResultSet</code> object
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
        } catch (SQLException e) {
            handleException();
            throw new ExecutionErrorException(sql, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the given <code>Query</code> builder's SQL statement, which
     * may be an <code>INSERT</code>, <code>UPDATE</code>, or
     * <code>DELETE</code> statement or a SQL statement that returns nothing,
     * such as an SQL DDL statement.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param query a <code>Query</code> builder instance which provides a SQL
     *              statement that modifies the database
     * @return the row count for <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code> statements; or
     * <p><code>0</code> for SQL statements that return nothing
     * @throws DatabaseException if a database access error occurs or the
     *                           given SQL statement produces a <code>ResultSet</code> object
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
     * <code>DbStatement</code> object. If this <code>DbStatement</code>
     * object has not set a fetch direction by calling the method
     * <code>setFetchDirection</code>, the return value is
     * implementation-specific.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the default fetch direction for result sets generated from this
     * <code>DbStatement</code> object
     * @throws DatabaseException if a database access error occurs
     * @see #setFetchDirection
     * @since 1.0
     */
    public int getFetchDirection()
    throws DatabaseException {
        try {
            return statement_.getFetchDirection();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the number of result set rows that is the default fetch size
     * for <code>ResultSet</code> objects generated from this
     * <code>DbStatement</code> object. If this <code>DbStatement</code>
     * object has not set a fetch size by calling the method
     * <code>setFetchSize</code>, the return value is implementation-specific.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the default fetch size for result sets generated from this
     * <code>DbStatement</code> object
     * @throws DatabaseException if a database access error occurs
     * @see #setFetchSize
     * @since 1.0
     */
    public int getFetchSize()
    throws DatabaseException {
        try {
            return statement_.getFetchSize();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves any auto-generated keys created as a result of executing this
     * <code>DbStatement</code> object. If this DbStatement object did not
     * generate any keys, an empty <code>DbResultSet</code> object is
     * returned.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return a <code>DbResultSet</code> object containing the auto-generated
     * key(s) generated by the execution of this <code>DbStatement</code>
     * object
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public DbResultSet getGeneratedKeys()
    throws DatabaseException {
        try {
            return wrapWithDbResultSet(statement_.getGeneratedKeys());
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the first auto-generated key created as a result of executing
     * this <code>DbStatement</code> object as an integer. If this
     * <code>DbStatement</code> object did not generate any keys, a exception
     * is thrown.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the maximum number of bytes that can be returned for
     * character and binary column values in a <code>ResultSet</code> object
     * produced by this <code>Statement</code> object. This limit applies only
     * to <code>BINARY</code>, <code>VARBINARY</code>,
     * <code>LONGVARBINARY</code>, <code>CHAR</code>, <code>VARCHAR</code>,
     * and <code>LONGVARCHAR</code> columns. If the limit is exceeded, the
     * excess data is silently discarded.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the current column size limit for columns storing character and
     * binary values; or
     * <p><code>0</code> if there's no limit
     * @throws DatabaseException if a database access error occurs
     * @see #setMaxFieldSize
     * @since 1.0
     */
    public int getMaxFieldSize()
    throws DatabaseException {
        try {
            return statement_.getMaxFieldSize();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the maximum number of rows that a <code>ResultSet</code>
     * object produced by this <code>DbStatement</code> object can contain. If
     * this limit is exceeded, the excess rows are silently dropped.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the current maximum number of rows for a <code>ResultSet</code>
     * object produced by this <code>Statement</code> object; or
     * <p><code>0</code> if there's no limit
     * @throws DatabaseException if a database access error occurs
     * @see #setMaxRows
     * @since 1.0
     */
    public int getMaxRows()
    throws DatabaseException {
        try {
            return statement_.getMaxRows();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Moves to this <code>DbStatement</code> object's next result, returns
     * <code>true</code> if it is a <code>ResultSet</code> object, and
     * implicitly closes any current <code>ResultSet</code> object(s) obtained
     * with the method <code>getResultSet</code>.
     * <p>There are no more results when the following is true:
     * <pre>
     * <code>(!getMoreResults() &amp;&amp; (getUpdateCount() == -1)</code>
     * </pre>
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return <code>true</code> if the next result is a
     * <code>ResultSet</code> object; or
     * <p><code>false</code> if it is an update count or there are no more
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Moves to this <code>DbStatement</code> object's next result, deals with
     * any current <code>ResultSet</code> object(s) according to the
     * instructions specified by the given flag, and returns <code>true</code>
     * if the next result is a <code>ResultSet</code> object.
     * <p>There are no more results when the following is true:
     * <pre>
     * 	  <code>(!getMoreResults() &amp;&amp; (getUpdateCount() == -1)</code>
     * </pre>
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param current one of the following <code>Statement</code> constants
     *                indicating what should happen to current <code>ResultSet</code> objects
     *                obtained using the method <code>getResultSet</code>:
     *                <code>CLOSE_CURRENT_RESULT</code>, <code>KEEP_CURRENT_RESULT</code>, or
     *                <code>CLOSE_ALL_RESULTS</code>
     * @return <code>true</code> if the next result is a
     * <code>ResultSet</code> object; or
     * <p><code>false</code> if it is an update count or there are no more
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the number of seconds the driver will wait for a
     * <code>DbStatement</code> object to execute. If the limit is exceeded, a
     * <code>DatabaseException</code> is thrown.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the current query timeout limit in seconds; or
     * <p><code>0</code> if there's no limit
     * @throws DatabaseException if a database access error occurs
     * @see #setQueryTimeout
     * @since 1.0
     */
    public int getQueryTimeout()
    throws DatabaseException {
        try {
            return statement_.getQueryTimeout();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Reports whether the last column read had a value of SQL
     * <code>NULL</code>. Note that you must first call one of the getter
     * methods on a column to try to read its value and then call the method
     * <code>wasNull</code> to see if the value read was SQL <code>NULL</code>.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return <code>true</code> if the last column value read was SQL
     * <code>NULL</code>; or
     * <p><code>false</code> otherwise
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the result set concurrency for <code>ResultSet</code> objects
     * generated by this <code>DbStatement</code> object.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return either <code>ResultSet.CONCUR_READ_ONLY</code> or
     * <code>ResultSet.CONCUR_UPDATABLE</code>
     * @throws DatabaseException if a database access error occurs
     * @see ResultSet
     * @since 1.0
     */
    public int getResultSetConcurrency()
    throws DatabaseException {
        try {
            return statement_.getResultSetConcurrency();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the result set holdability for <code>ResultSet</code> objects
     * generated by this <code>DbStatement</code> object.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return either <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     * <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws DatabaseException if a database access error occurs
     * @see ResultSet
     * @since 1.0
     */
    public int getResultSetHoldability()
    throws DatabaseException {
        try {
            return statement_.getResultSetHoldability();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the result set type for <code>ResultSet</code> objects
     * generated by this <code>DbStatement</code> object.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return one of <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     * <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     * <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @throws DatabaseException if a database access error occurs
     * @see ResultSet
     * @since 1.0
     */
    public int getResultSetType()
    throws DatabaseException {
        try {
            return statement_.getResultSetType();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the current result as an update count; if the result is a
     * <code>ResultSet</code> object or there are no more results, -1 is
     * returned. This method should be called only once per result.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the current result as an update count; or
     * <p><code>-1</code> if the current result is a <code>ResultSet</code>
     * object or there are no more results
     * @throws DatabaseException if a database access error occurs
     * @see #execute
     * @since 1.0
     */
    public int getUpdateCount()
    throws DatabaseException {
        try {
            return statement_.getUpdateCount();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the first warning reported by calls on this
     * <code>Statement</code> object. Subsequent <code>DbStatement</code>
     * object warnings will be chained to this <code>SQLWarning</code> object.
     * <p>The warning chain is automatically cleared each time a statement is
     * (re)executed. This method may not be called on a closed
     * <code>DbStatement</code> object; doing so will cause an
     * <code>SQLException</code> to be thrown.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     * <p><b>Note:</b> If you are processing a <code>ResultSet</code> object,
     * any warnings associated with reads on that <code>ResultSet</code>
     * object will be chained on it rather than on the
     * <code>DbStatement</code> object that produced it.
     *
     * @return the first <code>SQLWarning</code> object; or
     * <p><code>null</code> if there are no warnings
     * @throws DatabaseException if a database access error occurs or this
     *                           method is called on a closed statement
     * @since 1.0
     */
    public SQLWarning getWarnings()
    throws DatabaseException {
        try {
            return statement_.getWarnings();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the current result as a <code>ResultSet</code> object. This
     * method returns the internally stored result and can be called as many
     * times as wanted, contrary to the regular JDBC counterpart.
     *
     * @return the current result as a <code>ResultSet</code> object; or
     * <p><code>NULL</code> if the result is an update count.
     * @see #execute
     * @since 1.0
     */
    public DbResultSet getResultSet() {
        return resultSet_;
    }

    /**
     * Gives the driver a hint as to the direction in which rows will be
     * processed in <code>ResultSet</code> objects created using this
     * <code>DbStatement</code> object. The default value is
     * <code>ResultSet.FETCH_FORWARD</code>.
     * <p>Note that this method sets the default fetch direction for result
     * sets generated by this <code>DbStatement</code> object. Each result set
     * has its own methods for getting and setting its own fetch direction.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param direction the initial direction for processing rows
     * @throws DatabaseException if a database access error occurs or the
     *                           given direction is not one of <code>ResultSet.FETCH_FORWARD</code>,
     *                           <code>ResultSet.FETCH_REVERSE</code>, or
     *                           <code>ResultSet.FETCH_UNKNOWN</code>
     * @see #getFetchDirection
     * @see ResultSet
     * @since 1.0
     */
    public void setFetchDirection(int direction)
    throws DatabaseException {
        try {
            statement_.setFetchDirection(direction);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should be
     * fetched from the database when more rows are needed. The number of rows
     * specified affects only result sets created using this statement. If the
     * value specified is zero, then the hint is ignored. The default value is
     * zero.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param rows the number of rows to fetch
     * @throws DatabaseException if a database access error occurs, or the
     *                           condition 0 &lt;= <code>rows</code> &lt;=
     *                           <code>this.getMaxRows()</code> is not satisfied.
     * @see #getFetchSize
     * @see #getMaxRows
     * @since 1.0
     */
    public void setFetchSize(int rows)
    throws DatabaseException {
        try {
            statement_.setFetchSize(rows);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Sets the limit for the maximum number of bytes in a
     * <code>ResultSet</code> column storing character or binary values to the
     * given number of bytes. This limit applies only to <code>BINARY</code>,
     * <code>VARBINARY</code>, <code>LONGVARBINARY</code>, <code>CHAR</code>,
     * <code>VARCHAR</code>, and <code>LONGVARCHAR</code> fields. If the limit
     * is exceeded, the excess data is silently discarded. For maximum
     * portability, use values greater than 256.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Sets the limit for the maximum number of rows that any
     * <code>ResultSet</code> object can contain to the given number. If the
     * limit is exceeded, the excess rows are silently dropped.
     * <p>If an exception is thrown, this <code>DbStatement</code> is
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Sets the number of seconds the driver will wait for a
     * <code>DbStatement</code> object to execute to the given number of
     * seconds. If the limit is exceeded, an <code>DatabaseException</code> is
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
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Returns the <code>DbConnection</code> object from which this
     * <code>DbStatement</code> object has been instantiated.
     *
     * @return the instantiating <code>DbConnection</code> object.
     * @since 1.0
     */
    public DbConnection getConnection() {
        return connection_;
    }

    /**
     * Waits until the <code>DbConnection</code> method is available for use.
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
     * Checks if there's a <code>ResultSet</code> object present.
     *
     * @return <code>true</code> if a <code>ResultSet</code> object is
     * available; or
     * <p><code>false</code> otherwise.
     * @since 1.0
     */
    boolean hasResultset() {
        return null != resultSet_;
    }

    /**
     * Set the current <code>ResultSet</code> object and cleans up the
     * previous <code>ResultSet</code> object automatically.
     *
     * @param resultSet the new current <code>ResultSet</code> object
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
     * Cleans up and closes the current <code>ResultSet</code> object.
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
     * @throws DatabaseException when an error occurs during the cleanup of
     *                           the connection, or when an error occurs during the roll-back.
     */
    protected void handleException()
    throws DatabaseException {
        synchronized (this) {
            try {
                close();
            } catch (DatabaseException e) {
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

