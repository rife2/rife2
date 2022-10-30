/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.*;

import rife.database.queries.Query;
import rife.tools.ExceptionUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Represents one connection to a database. A connection has to be obtained by
 * using the <code>getConnection</code> method on a <code>Datasource</code>
 * instance. The resulting <code>DbConnection</code> instance can be used to
 * obtain statement objects from and to manage transactions.
 * <p>Statements are used to execute SQL queries either in a static or in a
 * prepared fashion. This corresponds to the <code>DbStatement</code> and
 * <code>DbPreparedStatement</code> classes. Look there for details about how
 * to use them. A <code>DbConnection</code> keeps track of which statements
 * have been openened and will automatically close them when database access
 * errors occur or when the connection itself is closed.
 * <p>Several statements can be executed as a whole through the use of
 * transactions. Only if they all succeeded, the transaction should be
 * committed and all the modifications will be preserved. Otherwise, the
 * transaction should be rolled back, and the modifications will not be
 * integrated into the general data storage. When a transaction has been
 * started through the <code>beginTransaction()</code> method, it will be
 * bound to the currently executing thread. Other threads will not be able to
 * manipulate the transaction status and if they obtain and execute
 * statements, they will be put in a wait state and woken up again after the
 * transaction has finished.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.database.Datasource#getConnection()
 * @see rife.database.DbStatement
 * @see rife.database.DbPreparedStatement
 * @since 1.0
 */
public class DbConnection implements AutoCloseable {
    private static final int TRANSACTIONS_SUPPORT_UNKNOWN = -1;
    private static final int TRANSACTIONS_UNSUPPORTED = 0;
    private static final int TRANSACTIONS_SUPPORTED = 1;

    private final Datasource datasource_;
    private Connection connection_;
    private ArrayList<DbStatement> statements_;
    private int mSupportsTransactions = TRANSACTIONS_SUPPORT_UNKNOWN;
    private Thread transactionThread_ = null;

    /**
     * Creates a new <code>DbConnection</code> instance and binds it to a
     * <code>Datasource</code> and a regular JDBC <code>Connection</code>.
     *
     * @param connection the JDBC <code>Connection</code> that will be used
     * @param datasource the <code>Datasource</code> this connection been
     *                   obtained from
     * @since 1.0
     */
    DbConnection(Connection connection, Datasource datasource) {
        assert connection != null;
        assert datasource != null;

        datasource_ = datasource;
        connection_ = connection;
        statements_ = new ArrayList<>();
    }

    /**
     * Retrieves the datasource this connection has been obtained from.
     *
     * @return the <code>Datasource</code> instance this connection has been
     * obtained from
     * @since 1.0
     */
    public Datasource getDatasource() {
        return datasource_;
    }

    /**
     * Releases all the resources that are being used by this connection. If
     * the connection has been obtained from a pooled datasource, it will not
     * be closed, but reused later. If the datasource isn't pooled, the
     * connection itself will be closed as expected.
     * <p>Any ongoing transactions will be automatically rolled-back.
     * <p>All open statements will be closed and if a transaction is active,
     * it will be automatically rolled back and unregistered.
     *
     * @throws DatabaseException if a database access error occurs, or if
     *                           an error occurred during the closing of an ongoing transaction, or if
     *                           an error occurred during the closing of the opened statements, or if an
     *                           error occurred during the closing of the underlying JDBC connection.
     * @since 1.0
     */
    public void close()
    throws DatabaseException {
        if (isClosed()) {
            synchronized (this) {
                this.notifyAll();
            }
            return;
        }

        synchronized (this) {
            if (hasTransactionThread() &&
                !isTransactionValidForThread()) {
                return;
            }

            try {
                // only close the connection when no pool is active and not
                // inside a transaction
                if (!datasource_.isPooled() &&
                    !(hasTransactionThread() && isTransactionValidForThread())) {
                    try {
                        try {
                            while (statements_.size() > 0) {
                                statements_.get(0).close();
                            }
                            statements_ = new ArrayList<DbStatement>();
                        } finally {
                            try {
                                connection_.close();
                            } catch (SQLException e) {
                                throw new ConnectionCloseErrorException(datasource_, e);
                            }
                        }
                    } finally {
                        connection_ = null;
                        mSupportsTransactions = TRANSACTIONS_SUPPORT_UNKNOWN;
                    }
                }
            } finally {
                this.notifyAll();
            }
        }
    }

    /**
     * Indicates whether this <code>DbConnection</code> instance has been
     * cleaned up or not.
     *
     * @return <code>true</code> if it has been cleaned up; or
     * <p><code>false</code> otherwise.
     */
    boolean isCleanedUp() {
        return null == connection_;
    }

    /**
     * This method is used to check if this <code>DbConnection</code> instance
     * has been cleaned up before using the underlying JDBC connection.
     *
     * @throws SQLException when it has been cleaned up.
     */
    void detectCleanup()
    throws SQLException {
        if (isCleanedUp()) {
            throw new SQLException("The connection is closed.");
        }
    }

    /**
     * Cleans up all the resources that are used by this
     * <code>DbConnection</code> instance. This is mainly used to correctly
     * clean up in case of errors during execution.
     *
     * @throws DatabaseException if an error occurred during the closing of
     *                           an ongoing transaction, or if an error occurred during the closing of
     *                           the opened statements,
     * @since 1.0
     */
    void cleanup()
    throws DatabaseException {
        Thread transaction_thread = null;
        // unregister a transaction thread
        if (transactionThread_ != null) {
            transaction_thread = transactionThread_;
            transactionThread_ = null;
        }

        try {
            DbStatement statement = null;

            // close all active statements
            synchronized (this) {
                while (statements_.size() > 0) {
                    statement = statements_.get(0);
                    statement.close();
                    try {
                        statement.cancel();
                    } catch (DatabaseException e) {
                        // don't do anything since some DBs don't correct support statement cancelling
                    }
                }
                statements_ = new ArrayList<DbStatement>();
            }

            // reset the connection state
            if (connection_ != null) {
                // rollback an ongoing transaction
                try {
                    connection_.rollback();
                    connection_.setAutoCommit(true);
                } catch (SQLException ignored) {
                }

                // close the JDBC connection
                try {
                    connection_.close();
                } catch (SQLException ignored) {
                }
            }
            connection_ = null;
            mSupportsTransactions = TRANSACTIONS_SUPPORT_UNKNOWN;
        } finally {
            if (transaction_thread != null) {
                datasource_.getPool().unregisterThreadConnection(transaction_thread);
            }
        }
    }

    /**
     * Performs the cleanup logic in case an exception is thrown during
     * execution. If the connection is part of a pooled datasource, all
     * connections in the pool will be closed and the whole pool will be setup
     * cleanly again. If the connection isn't pooled, it will be cleaned up
     * properly.
     *
     * @throws DatabaseException when an error occurs during the cleanup of
     *                           the connection, or when an error occurs when the pool is set up again.
     */
    void handleException()
    throws DatabaseException {
        if (!datasource_.isPooled()) {
            synchronized (datasource_) {
                synchronized (this) {
                    // cleanup the connection resources
                    cleanup();
                    this.notifyAll();
                }
            }
        } else {
            // recreate all the pooled connections
            datasource_.getPool().recreateConnection(this);
        }
    }

    /**
     * Creates a new <code>DbStatement</code> instance for this connection. It
     * will be registered and automatically closed when this
     * <code>DbConnection</code> cleans up. It is recommended though to
     * manually close the statement when it's not needed anymore for sensible
     * resource preservation.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @return a new <code>DbStatement</code> instance
     * @throws DatabaseException when an exception has occurred during the
     *                           creation of the <code>DbStatement</code> instance
     * @see rife.database.DbStatement
     * @see #getPreparedStatement(String)
     * @see #getPreparedStatement(Query)
     * @since 1.0
     */
    public DbStatement createStatement()
    throws DatabaseException {
        try {
            detectCleanup();

            Statement statement = connection_.createStatement();

            synchronized (this) {
                DbStatement db_statement = new DbStatement(this, statement);
                statements_.add(db_statement);

                return db_statement;
            }
        } catch (SQLException e) {
            handleException();
            throw new StatementCreationErrorException(datasource_, e);
        }
    }

    /**
     * Creates a new <code>DbStatement</code> instance for this connection with
     * the given type and concurrency. It
     * will be registered and automatically closed when this
     * <code>DbConnection</code> cleans up. It is recommended though to
     * manually close the statement when it's not needed anymore for sensible
     * resource preservation.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @param resultSetType        a result set type; one of ResultSet.TYPE_FORWARD_ONLY,
     *                             ResultSet.TYPE_SCROLL_INSENSITIVE, or ResultSet.TYPE_SCROLL_SENSITIVE
     * @param resultSetConcurrency a concurrency type; one of
     *                             ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
     * @return a new <code>DbStatement</code> instance
     * @throws DatabaseException when an exception has occurred during the
     *                           creation of the <code>DbStatement</code> instance
     * @see rife.database.DbStatement
     * @see #getPreparedStatement(String)
     * @see #getPreparedStatement(Query)
     * @since 1.0
     */
    public DbStatement createStatement(int resultSetType, int resultSetConcurrency)
    throws DatabaseException {
        try {
            detectCleanup();

            Statement statement = connection_.createStatement(resultSetType, resultSetConcurrency);

            synchronized (this) {
                DbStatement db_statement = new DbStatement(this, statement);
                statements_.add(db_statement);

                return db_statement;
            }
        } catch (SQLException e) {
            handleException();
            throw new StatementCreationErrorException(datasource_, e);
        }
    }

    /**
     * Creates a new <code>DbStatement</code> instance for this connection with
     * the given type, concurrency, and holdability.. It
     * will be registered and automatically closed when this
     * <code>DbConnection</code> cleans up. It is recommended though to
     * manually close the statement when it's not needed anymore for sensible
     * resource preservation.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @param resultSetType        a result set type; one of ResultSet.TYPE_FORWARD_ONLY,
     *                             ResultSet.TYPE_SCROLL_INSENSITIVE, or ResultSet.TYPE_SCROLL_SENSITIVE
     * @param resultSetConcurrency a concurrency type; one of
     *                             ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
     * @param resultSetHoldability one of the following ResultSet constants:
     *                             ResultSet.HOLD_CURSORS_OVER_COMMIT or ResultSet.CLOSE_CURSORS_AT_COMMIT
     * @return a new <code>DbStatement</code> instance
     * @throws DatabaseException when an exception has occurred during the
     *                           creation of the <code>DbStatement</code> instance
     * @see rife.database.DbStatement
     * @see #getPreparedStatement(String)
     * @see #getPreparedStatement(Query)
     * @since 1.0
     */
    public DbStatement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    throws DatabaseException {
        try {
            detectCleanup();

            Statement statement = connection_.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);

            synchronized (this) {
                DbStatement db_statement = new DbStatement(this, statement);
                statements_.add(db_statement);

                return db_statement;
            }
        } catch (SQLException e) {
            handleException();
            throw new StatementCreationErrorException(datasource_, e);
        }
    }

    /**
     * Creates a new <code>DbPreparedStatement</code> instance for this
     * connection from a regular SQL query string. Since the statement is
     * created from a <code>String</code> and not a
     * <code>ParametrizedQuery</code> instance, information is lacking to be
     * able to fully use the features of the resulting
     * <code>DbPreparedStatement</code> instance. It's recommended to use the
     * {@link #getPreparedStatement(Query)} method instead if this is
     * possible.
     * <p>The new statement will be registered and automatically closed when
     * this <code>DbConnection</code> cleans up. It is recommended though to
     * manually close the statement when it's not needed anymore for sensible
     * resource preservation.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @param sql a <code>String</code> instance with the SQL that is used to
     *            set up the prepared statement
     * @return a new <code>DbPreparedStatement</code> instance
     * @throws DatabaseException when an exception has occurred during the
     *                           creation of the <code>DbPreparedStatement</code> instance
     * @see rife.database.DbPreparedStatement
     * @see #createStatement()
     * @see #getPreparedStatement(Query)
     * @since 1.0
     */
    public DbPreparedStatement getPreparedStatement(String sql)
    throws DatabaseException {
        if (null == sql) throw new IllegalArgumentException("sql can't be null.");
        if (0 == sql.length()) throw new IllegalArgumentException("sql can't be empty.");

        try {
            detectCleanup();

            PreparedStatement prepared_statement = connection_.prepareStatement(sql);

            synchronized (this) {
                DbPreparedStatement db_prepared_statement = new DbPreparedStatement(this, sql, prepared_statement);
                statements_.add(db_prepared_statement);

                return db_prepared_statement;
            }
        } catch (SQLException e) {
            handleException();
            throw new PreparedStatementCreationErrorException(datasource_, e);
        }
    }

    /**
     * Creates a new <code>DbPreparedStatement</code> instance for this
     * connection from a <code>Query</code> instance that has the capability
     * to retrieve auto-generated keys. The given constant tells the driver
     * whether it should make auto-generated keys available for retrieval.
     * This parameter is ignored if the SQL statement is not an INSERT
     * statement.
     * <p>Since the statement is created from a <code>String</code> and not a
     * <code>ParametrizedQuery</code> instance, information is lacking to be
     * able to fully use the features of the resulting
     * <code>DbPreparedStatement</code> instance. It's recommended to use the
     * {@link #getPreparedStatement(Query)} method instead if this is
     * possible.
     * <p>The new statement will be registered and automatically closed when
     * this <code>DbConnection</code> cleans up. It is recommended though to
     * manually close the statement when it's not needed anymore for sensible
     * resource preservation.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @param sql               a <code>String</code> instance with the SQL that is used to
     *                          set up the prepared statement
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys
     *                          should be returned; one of <code>Statement.RETURN_GENERATED_KEYS</code>
     *                          or <code>Statement.NO_GENERATED_KEYS</code>
     * @return a new <code>DbPreparedStatement</code> instance
     * @throws DatabaseException when an exception has occurred during the
     *                           creation of the <code>DbPreparedStatement</code> instance
     * @see rife.database.DbPreparedStatement
     * @see #createStatement()
     * @see #getPreparedStatement(Query)
     * @since 1.0
     */
    public DbPreparedStatement getPreparedStatement(String sql, int autoGeneratedKeys)
    throws DatabaseException {
        if (null == sql) throw new IllegalArgumentException("sql can't be null.");
        if (0 == sql.length()) throw new IllegalArgumentException("sql can't be empty.");

        try {
            detectCleanup();

            PreparedStatement prepared_statement = connection_.prepareStatement(sql, autoGeneratedKeys);

            synchronized (this) {
                DbPreparedStatement db_prepared_statement = new DbPreparedStatement(this, sql, prepared_statement);
                statements_.add(db_prepared_statement);

                return db_prepared_statement;
            }
        } catch (SQLException e) {
            handleException();
            throw new PreparedStatementCreationErrorException(datasource_, e);
        }
    }

    /**
     * Creates a new <code>DbPreparedStatement</code> instance for this
     * connection from a <code>Query</code> instance. Thanks to the additional
     * meta-data that's stored in a <code>Query</code> object, it's possible
     * to use the additional features that the
     * <code>DbPreparedStatement</code> provides on top of regular JDBC
     * methods.
     * <p>The new statement will be registered and automatically closed when
     * this <code>DbConnection</code> cleans up. It is recommended though to
     * manually close the statement when it's not needed anymore for sensible
     * resource preservation.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @param query a <code>Query</code> instance that is used to set up the
     *              prepared statement
     * @return a new <code>DbPreparedStatement</code> instance
     * @throws DatabaseException when an exception has occurred during the
     *                           creation of the <code>DbPreparedStatement</code> instance
     * @see rife.database.DbPreparedStatement
     * @see #createStatement()
     * @see #getPreparedStatement(String)
     * @since 1.0
     */
    public DbPreparedStatement getPreparedStatement(Query query)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");
        if (null == query.getSql()) throw new IllegalArgumentException("query can't be empty.");

        try {
            detectCleanup();

            String sql = query.getSql();
            PreparedStatement prepared_statement = connection_.prepareStatement(sql);

            synchronized (this) {
                DbPreparedStatement db_prepared_statement = new DbPreparedStatement(this, query, prepared_statement);
                statements_.add(db_prepared_statement);

                return db_prepared_statement;
            }
        } catch (SQLException e) {
            handleException();
            throw new PreparedStatementCreationErrorException(datasource_, e);
        }
    }

    /**
     * Creates a new <code>DbPreparedStatement</code> instance for this
     * connection from a <code>Query</code> instance that has the capability
     * to retrieve auto-generated keys. The given constant tells the driver
     * whether it should make auto-generated keys available for retrieval.
     * This parameter is ignored if the SQL statement is not an INSERT
     * statement.
     * <p>Thanks to the additional meta-data that's stored in a
     * <code>Query</code> object, it's possible to use the additional features
     * that the <code>DbPreparedStatement</code> provides on top of regular
     * JDBC methods.
     * <p>The new statement will be registered and automatically closed when
     * this <code>DbConnection</code> cleans up. It is recommended though to
     * manually close the statement when it's not needed anymore for sensible
     * resource preservation.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @param query             a <code>Query</code> instance that is used to set up the
     *                          prepared statement
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys
     *                          should be returned; one of <code>Statement.RETURN_GENERATED_KEYS</code>
     *                          or <code>Statement.NO_GENERATED_KEYS</code>
     * @return a new <code>DbPreparedStatement</code> instance
     * @throws DatabaseException when an exception has occurred during the
     *                           creation of the <code>DbPreparedStatement</code> instance
     * @see rife.database.DbPreparedStatement
     * @see #createStatement()
     * @see #getPreparedStatement(String)
     * @since 1.0
     */
    public DbPreparedStatement getPreparedStatement(Query query, int autoGeneratedKeys)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");
        if (null == query.getSql()) throw new IllegalArgumentException("query can't be empty.");

        try {
            detectCleanup();

            String sql = query.getSql();
            PreparedStatement prepared_statement = connection_.prepareStatement(sql, autoGeneratedKeys);

            synchronized (this) {
                DbPreparedStatement db_prepared_statement = new DbPreparedStatement(this, query, prepared_statement);
                statements_.add(db_prepared_statement);

                return db_prepared_statement;
            }
        } catch (SQLException e) {
            handleException();
            throw new PreparedStatementCreationErrorException(datasource_, e);
        }
    }

    /**
     * Creates a new <code>DbPreparedStatement</code> instance for this
     * connection from a <code>Query</code> instance that will generate
     * <code>ResultSet</code> objects with the given type, concurrency,
     * and holdability.
     * <p>
     * This method is the same as the <code>getPreparedStatement</code> method
     * above, but it allows the default result set
     * type, concurrency, and holdability to be overridden.
     * <p>Thanks to the additional meta-data that's stored in a
     * <code>Query</code> object, it's possible to use the additional features
     * that the <code>DbPreparedStatement</code> provides on top of regular
     * JDBC methods.
     * <p>The new statement will be registered and automatically closed when
     * this <code>DbConnection</code> cleans up. It is recommended though to
     * manually close the statement when it's not needed anymore for sensible
     * resource preservation.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @param query                a <code>Query</code> instance that is used to set up the
     *                             prepared statement
     * @param resultSetType        one of the following <code>ResultSet</code>
     *                             constants:
     *                             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *                             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *                             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency one of the following <code>ResultSet</code>
     *                             constants:
     *                             <code>ResultSet.CONCUR_READ_ONLY</code> or
     *                             <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability one of the following <code>ResultSet</code>
     *                             constants:
     *                             <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *                             <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return a new <code>DbPreparedStatement</code> instance, that will generate
     * <code>ResultSet</code> objects with the given type,
     * concurrency, and holdability
     * @throws DatabaseException when an exception has occurred during the
     *                           creation of the <code>DbPreparedStatement</code> instance
     * @see rife.database.DbPreparedStatement
     * @see #createStatement()
     * @see #getPreparedStatement(String)
     * @since 1.2
     */
    public DbPreparedStatement getPreparedStatement(Query query, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    throws DatabaseException {
        if (null == query) throw new IllegalArgumentException("query can't be null.");
        if (null == query.getSql()) throw new IllegalArgumentException("query can't be empty.");

        try {
            detectCleanup();

            String sql = query.getSql();
            PreparedStatement prepared_statement = connection_.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);

            synchronized (this) {
                DbPreparedStatement db_prepared_statement = new DbPreparedStatement(this, query, prepared_statement);
                statements_.add(db_prepared_statement);

                return db_prepared_statement;
            }
        } catch (SQLException e) {
            handleException();
            throw new PreparedStatementCreationErrorException(datasource_, e);
        }
    }

    /**
     * Removes a <code>DbStatement</code> instance from the collection of
     * managed statements. If the statement is not present, no error or
     * exception is thrown.
     *
     * @param statement the <code>DbStatement</code> that has to be removed.
     * @since 1.0
     */
    void releaseStatement(DbStatement statement) {
        synchronized (this) {
            statements_.remove(statement);
        }
    }

    /**
     * Indicates whether the <code>Datasource</code> of this
     * <code>DbConnection</code> supports transactions or not.
     * <p>This information is only retrieved once and cached for the rest of
     * the lifetime of this connection.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @return <code>true</code> if the <code>Datasource</code> supports
     * transactions; or
     * <p><code>false</code> if the <code>Datasource</code> doesn't support
     * transactions.
     * @throws DatabaseException when an error occurred during the
     *                           verification of the transaction support
     * @see #beginTransaction()
     * @see #commit()
     * @see #rollback()
     * @see #isFree()
     * @see #isTransactionValidForThread()
     * @since 1.0
     */
    public boolean supportsTransactions()
    throws DatabaseException {
        try {
            synchronized (this) {
                detectCleanup();

                if (TRANSACTIONS_SUPPORT_UNKNOWN == mSupportsTransactions) {
                    if (connection_.getMetaData().supportsTransactions()) {
                        mSupportsTransactions = TRANSACTIONS_SUPPORTED;
                    } else {
                        mSupportsTransactions = TRANSACTIONS_UNSUPPORTED;
                    }
                }

                return TRANSACTIONS_SUPPORTED == mSupportsTransactions;

            }
        } catch (SQLException e) {
            handleException();
            throw new TransactionSupportCheckErrorException(datasource_, e);
        }
    }

    /**
     * <p><strong>Warning:</strong> only use the raw transaction methods if
     * you really know what you're doing. It's almost always better to use the
     * {@link DbQueryManager#inTransaction(DbTransactionUser) inTransaction}
     * method of the <code>DbQueryManager</code> class instead.
     * <p>Starts a new transaction if the <code>Datasource</code> supports it.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @return <code>true</code> if the transaction was successfully started;
     * or
     * <p><code>false</code> if the <code>Datasource</code> doesn't support
     * transactions, or if a transaction is already active on this
     * <code>DbConnection</code>.
     * @throws DatabaseException when an error occurred during the creation
     *                           of the new transaction, or when the active transaction has timed-out.
     * @see DbQueryManager#inTransaction(DbTransactionUser)
     * @see #supportsTransactions()
     * @see #commit()
     * @see #rollback()
     * @see #isFree()
     * @see #isTransactionValidForThread()
     * @since 1.0
     */
    public boolean beginTransaction()
    throws DatabaseException {
        // if the datasource doesn't support transactions,
        // don't start any
        if (!supportsTransactions()) {
            return false;
        }

        synchronized (this) {
            // check if a thread has already got a hold of this connection
            if (hasTransactionThread()) {
                // if it's still active, it's impossible to begin a new
                // transaction
                return false;
            }

            // setup the new transaction
            transactionThread_ = Thread.currentThread();
        }

        try {
            detectCleanup();

            connection_.setAutoCommit(false);
        } catch (SQLException e) {

            if (transactionThread_ != null) {
                transactionThread_ = null;
            }
            handleException();
            throw new TransactionBeginErrorException(datasource_, e);
        }

        datasource_.getPool().registerThreadConnection(transactionThread_, this);

        return true;
    }

    /**
     * <p><strong>Warning:</strong> only use the raw transaction methods if
     * you really know what you're doing. It's almost always better to use the
     * {@link DbQueryManager#inTransaction(DbTransactionUser) inTransaction}
     * method of the <code>DbQueryManager</code> class instead.
     * <p>Commits an active transaction.
     * <p>All transaction-related resources are cleared and all the threads
     * that are waiting for the transaction to terminate are woken up.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @return <code>true</code> if the transaction was successfully
     * committed; or
     * <p><code>false</code> if the <code>Datasource</code> doesn't support
     * transactions, or when no transaction is active on this
     * <code>DbConnection</code>, or when the executing thread isn't the
     * thread that began the transaction.
     * @throws DatabaseException when an error occurred during the commit
     *                           of the active transaction, or when the active transaction has
     *                           timed-out.
     * @see DbQueryManager#inTransaction(DbTransactionUser)
     * @see #supportsTransactions()
     * @see #beginTransaction()
     * @see #rollback()
     * @see #isFree()
     * @see #isTransactionValidForThread()
     * @since 1.0
     */
    public boolean commit()
    throws DatabaseException {
        // if the datasource doesn't support transactions,
        // it's impossible to commit one
        if (!supportsTransactions()) {
            return false;
        }

        synchronized (this) {
            // if the transaction isn't valid for the current thread
            // refuse to commit it
            if (!isTransactionValidForThread()) {
                return false;
            }
        }

        try {
            detectCleanup();

            connection_.commit();
            connection_.setAutoCommit(true);
        } catch (SQLException e) {
            handleException();
            throw new TransactionCommitErrorException(datasource_, e);
        } finally {
            synchronized (this) {
                if (transactionThread_ != null) {
                    transactionThread_ = null;
                }

                this.notifyAll();
            }

            datasource_.getPool().unregisterThreadConnection(Thread.currentThread());
        }

        return true;
    }

    /**
     * <p><strong>Warning:</strong> only use the raw transaction methods if
     * you really know what you're doing. It's almost always better to use the
     * {@link DbQueryManager#inTransaction(DbTransactionUser) inTransaction}
     * method of the <code>DbQueryManager</code> class instead.
     * <p>Rolls-back an active transaction.
     * <p>All transaction-related resources are cleared and all the threads
     * that are waiting for the transaction to terminate are woken up.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @return <code>true</code> if the transaction was successfully
     * rolled-back; or
     * <p><code>false</code> if the <code>Datasource</code> doesn't support
     * transactions, or when no transaction is active on this
     * <code>DbConnection</code>, or when the executing thread isn't the
     * thread that began the transaction.
     * @throws DatabaseException when an error occurred during the rollback
     *                           of the active transaction, or when the active transaction has
     *                           timed-out.
     * @see DbQueryManager#inTransaction(DbTransactionUser)
     * @see #supportsTransactions()
     * @see #beginTransaction()
     * @see #commit()
     * @see #isFree()
     * @see #isTransactionValidForThread()
     * @since 1.0
     */
    public boolean rollback()
    throws DatabaseException {
        // if the datasource doesn't support transactions,
        // it's impossible to roll one back
        if (!supportsTransactions()) {
            return false;
        }

        synchronized (this) {
            // if the transaction isn't valid for the current thread
            // refuse to roll it back
            if (!isTransactionValidForThread()) {
                return false;
            }
        }

        try {
            detectCleanup();

            connection_.rollback();
            connection_.setAutoCommit(true);
        } catch (SQLException e) {
            handleException();
            throw new TransactionRollbackErrorException(datasource_, e);
        } finally {
            synchronized (this) {
                if (transactionThread_ != null) {
                    transactionThread_ = null;
                }

                this.notifyAll();
            }

            datasource_.getPool().unregisterThreadConnection(Thread.currentThread());
        }

        return true;
    }

    /**
     * Indicates whether this <code>DbConnection</code> is free to execute
     * statements for the current thread.
     *
     * @return <code>true</code> if a statement can be executed by the current
     * thread on this <code>DbConnection</code>; or
     * <p><code>false</code> if the connection is closed or when a transaction
     * is already active on this <code>DbConnection</code> for another thread.
     * @see #supportsTransactions()
     * @see #beginTransaction()
     * @see #commit()
     * @see #rollback()
     * @see #isTransactionValidForThread()
     * @since 1.0
     */
    public boolean isFree() {
        synchronized (this) {
            if (isCleanedUp()) {
                return false;
            }

            if (!hasTransactionThread()) {
                return true;
            }

            return isTransactionValidForThread();
        }
    }

    /**
     * Indicates whether this connections has an active transaction thread.
     *
     * @return <code>true</code> is an active transaction thread is present;
     * or
     * <p><code>false</code> otherwise.
     */
    private boolean hasTransactionThread() {
        return transactionThread_ != null;
    }

    /**
     * Indicates whether the current thread has a valid transaction going on
     * for the execution of statements.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again.
     *
     * @return <code>true</code> if a transaction is active that can be used
     * by the current thread; or
     * <p><code>false</code> if the connection is closed, doesn't support
     * transactions, has no active transaction or has a transaction that was
     * started by another thread.
     * @throws DatabaseException when errors occurred during the
     *                           verification of the connection's open status and support for
     *                           transactions
     * @see #supportsTransactions()
     * @see #beginTransaction()
     * @see #commit()
     * @see #rollback()
     * @see #isFree()
     * @since 1.0
     */
    public boolean isTransactionValidForThread() {
        synchronized (this) {
            if (!hasTransactionThread()) {
                return false;
            }

            return transactionThread_ == Thread.currentThread();

        }
    }

    /**
     * Indicates whether this <code>DbConnection</code>'s connection to the
     * database is closed.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically cleaned up. Also, any ongoing transaction will be
     * rolled-back automatically.
     *
     * @return <code>true</code> when this <code>DbConnection</code> is
     * closed; or
     * <p><code>false</code> if it's connected.
     * @throws DatabaseException when an error occurred during the
     *                           verification of the JDBC connection's closed status
     * @since 1.0
     */
    public boolean isClosed()
    throws DatabaseException {
        try {
            if (null == connection_) {
                return true;
            }

            return connection_.isClosed();
        } catch (SQLException e) {
            cleanup();
            throw new ConnectionStatusErrorException(datasource_, e);
        }
    }

    /**
     * Retrieves a <code>DatabaseMetaData</code> object that contains metadata
     * about the database to which this <code>DbConnection</code> object
     * represents a connection. The metadata includes information about the
     * database's tables, its supported SQL grammar, its stored procedures,
     * the capabilities of this connection, and so on.
     * <p>If an exception is thrown, this <code>DbConnection</code> is
     * automatically closed and if it's part of a pool, all the other
     * connections are closed too and the pool is set up again. Also, any
     * ongoing transaction will be rolled-back automatically.
     *
     * @return a <code>DatabaseMetaData</code> object for this
     * <code>DbConnection</code> instance; or
     * <p><code>null</code> if the <code>DbConnection</code> instance is not
     * connected.
     * @throws DatabaseException if a database access error occurs
     */
    public DatabaseMetaData getMetaData()
    throws DatabaseException {
        try {
            detectCleanup();

            return connection_.getMetaData();
        } catch (SQLException e) {
            handleException();
            throw new ConnectionMetaDataErrorException(datasource_, e);
        }
    }

    /**
     * Attempts to change the transaction isolation level for this
     * <code>DbConnection</code> object to the one given. The constants
     * defined in the interface <code>Connection</code> are the possible
     * transaction isolation levels.
     *
     * @param level transaction isolation level constant defined in the <code>{@link
     *              java.sql.Connection Connection}</code> interface
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Connection
     */
    public void setTransactionIsolation(int level)
    throws DatabaseException {
        try {
            detectCleanup();

            connection_.setTransactionIsolation(level);
        } catch (SQLException e) {
            handleException();
            throw new ConnectionMetaDataErrorException(datasource_, e);
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


