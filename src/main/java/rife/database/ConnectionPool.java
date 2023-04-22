/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.DatabaseException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a class designed for database connection pooling. By storing
 * connections, along with the thread that they are assigned to, thread-aware
 * operations can be performed safely, securely, and more efficiently.
 *
 * @author JR Boyens (jboyens[remove] at uwyn dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ConnectionPool implements AutoCloseable {
    private int poolSize_ = 0;
    private ArrayList<DbConnection> connectionPool_ = new ArrayList<>();
    private final HashMap<Thread, DbConnection> threadConnections_ = new HashMap<>();

    /**
     * Create a new ConnectionPool
     *
     * @since 1.0
     */
    ConnectionPool() {
    }

    /**
     * Set the size of the connection pool
     *
     * @param poolSize the new size of the pool
     * @since 1.0
     */
    void setPoolSize(int poolSize) {
        synchronized (this) {
            if (connectionPool_.size() > 0) {
                cleanup();
            }

            poolSize_ = poolSize;
        }
    }

    /**
     * Get the size of the connection pool
     *
     * @return int the size of the connection pool
     * @since 1.0
     */
    int getPoolSize() {
        return poolSize_;
    }

    /**
     * Check if the connection pool is initialized
     *
     * @return boolean true if initialized; false if not
     * @since 1.0
     */
    boolean isInitialized() {
        return connectionPool_.size() > 0;
    }

    /**
     * Fill the pool with connections. Prepare the pool by filling it with
     * connections from the provided datasource
     *
     * @param datasource the {@link Datasource} to fill the pool with
     *                   connections from
     * @throws DatabaseException when an error occurred during the
     *                           preparation of the pool
     * @since 1.0
     */
    void preparePool(Datasource datasource)
    throws DatabaseException {
        synchronized (this) {
            cleanup();

            connectionPool_.ensureCapacity(poolSize_);
            for (int i = 0; i < poolSize_; i++) {
                connectionPool_.add(datasource.createConnection());
            }

            assert poolSize_ == connectionPool_.size();
            this.notifyAll();
        }
    }

    /**
     * Cleans up all connections that have been reserved by this
     * datasource.
     *
     * @throws DatabaseException when an error occurred during the
     *                           clearing of the pool
     * @since 1.0
     */
    public void cleanup()
    throws DatabaseException {
        synchronized (this) {
            if (0 == connectionPool_.size()) {
                return;
            }

            ArrayList<DbConnection> previous_pool;

            previous_pool = connectionPool_;
            connectionPool_ = new ArrayList<>();

            if (previous_pool != null) {
                for (DbConnection connection : previous_pool) {
                    connection.cleanup();
                }

                previous_pool.clear();
            }

            threadConnections_.clear();
        }
    }

    /**
     * Remembers which connection has been reserved for a particular
     * thread. This makes sequential operations within the same
     * transaction in the same thread be executed on the same connection.
     * Otherwise, transaction deadlocks might appear.
     *
     * @param thread     the {@link Thread} to which the connection should be
     *                   registered to
     * @param connection the {@link DbConnection} that should be
     *                   registered to the thread
     * @since 1.0
     */
    void registerThreadConnection(Thread thread, DbConnection connection) {
        synchronized (this) {
            threadConnections_.put(thread, connection);
        }
    }

    /**
     * Removes the dedication of a connection for a specific thread.
     *
     * @param thread the {@link Thread} whose {@link DbConnection} should
     *               be unregistered.
     * @since 1.0
     */
    void unregisterThreadConnection(Thread thread) {
        synchronized (this) {
            threadConnections_.remove(thread);
            this.notifyAll();
        }
    }

    /**
     * Check if a connection reserved for a specific thread.
     *
     * @param thread the {@link Thread} to check for a reserved connection
     * @return true if the passed-in thread has a connection; false if not
     * @since 1.0
     */
    boolean hasThreadConnection(Thread thread) {
        synchronized (this) {
            return threadConnections_.containsKey(thread);
        }
    }

    /**
     * Recreate the connection.
     *
     * @param connection the {@link DbConnection} to be recreated
     * @throws DatabaseException when there is a problem recreating the
     *                           connection or cleaning up the old connection
     * @since 1.0
     */
    void recreateConnection(DbConnection connection)
    throws DatabaseException {
        synchronized (this) {
            if (connectionPool_.remove(connection)) {
                connectionPool_.add(connection.getDatasource().createConnection());
            }
            connection.cleanup();
        }
    }

    /**
     * Retrieve this thread's connection.
     * <p>Connections are allocated from the pool and assigned to the
     * current calling thread. If the thread does not have a current
     * connection or the pool size is 0, then a new connection is created
     * and assigned to the calling thread.
     *
     * @param datasource the datasource to create the connection to
     * @return the created or retrieved DbConnection
     * @throws DatabaseException when an error occurred retrieving or
     *                           creating the connection
     * @since 1.0
     */
    DbConnection getConnection(Datasource datasource)
    throws DatabaseException {
        synchronized (this) {
            // check if the connection threads contains an entry for the
            // current thread so that transactions are treated in a
            // continuous fashion
            if (threadConnections_.containsKey(Thread.currentThread())) {
                DbConnection connection = threadConnections_.get(Thread.currentThread());
                if (connection != null) {
                    return connection;
                }
            }

            // if there's no pool, create a new connection
            if (0 == poolSize_) {
                return datasource.createConnection();
            }
            // otherwise, try to obtain a free connection in the pool
            else {
                DbConnection connection = null;

                // iterate over the available connections and try to obtain the
                // first free one
                DbConnection possible_connection = null;
                while (null == connection) {
                    // prepare the pool if it's currently empty
                    if (connectionPool_.size() < poolSize_) {
                        preparePool(datasource);
                    }

                    for (int i = 0; i < connectionPool_.size() && null == connection; i++) {
                        possible_connection = connectionPool_.get(i);
                        if (null == possible_connection ||
                            possible_connection.isCleanedUp()) {
                            connection = datasource.createConnection();
                            connectionPool_.set(i, connection);
                            break;
                        } else if (null != possible_connection &&
                                   possible_connection.isFree()) {
                            connection = possible_connection;
                            break;
                        }
                    }

                    if (null == connection) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            Thread.yield();
                        }
                    }
                }

                // move the obtained connection to the end of the connection
                // pool list
                connectionPool_.remove(connection);
                connectionPool_.add(connection);

                return connection;
            }
        }
    }

    @Override
    public void close()
    throws DatabaseException {
        cleanup();
    }
}

