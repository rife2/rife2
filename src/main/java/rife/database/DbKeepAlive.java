/*
 * Copyright 2001-2008 Steven Grimm <koreth[remove] at midwinter dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id: DbConnection.java 3442 2006-08-10 09:26:43Z gbevin $
 */
package rife.database;

import rife.database.exceptions.DbQueryException;
import rife.scheduler.Executor;
import rife.scheduler.Task;

/**
 * Periodic probe job to keep connections non-idle and probe for dead ones.
 * This is useful for MySQL/MariaDB, which close connections after a
 * period of inactivity.
 *
 * <p>This should be run using a scheduler. For example, to
 * probe a datasource once an hour:
 *
 * <pre>
 * var keepAlive = new DbKeepAlive(datasource);
 * scheduler.addExecutor(keepAlive);
 * scheduler.addTask(keepAlive.createTask().frequency(Frequency.HOURLY));
 * </pre>
 *
 * <p>There is one optional parameter.
 * <dl>
 * <dt><code>query</code></td>
 * <dd>The dummy query to send. If not specified, the default is
 * "select 1".</dd>
 * </dl>
 *
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.6.2
 */
public class DbKeepAlive extends Executor {
    final Datasource datasource;

    public DbKeepAlive(Datasource ddatasource) {
        super();
        this.datasource = ddatasource;
    }

    @Override
    public boolean executeTask(Task task) {
        try {
            var query = task.getTaskOptionValue("query");
            if (null == query) {
                query = "select 1";
            }

            /*
             * Now fetch all the connections that should be in the pool,
             * and run a dummy statement on each of them to keep it from
             * going idle.
             *
             * This relies on the fact that ConnectionPool returns
             * DbConnection objects in a round-robin fashion. We can just
             * fetch the next connection the appropriate number of times
             * and be guaranteed to hit all of them.
             *
             * If there are transactions active on other threads, we will
             * not be given those threads' DbConnection objects, so we
             * might end up being handed the same connection twice. No
             * harm in that, and any connection that has an active
             * transaction isn't idle anyway so doesn't need to be probed.
             */
            for (int i = 0; i < datasource.getPoolSize(); i++) {
                try (var conn = datasource.getConnection()) {
                    if (null == conn) {
                        throw new DbQueryException("Can't get Datasource connection");
                    }

                    try (var stmt = conn.getPreparedStatement(query)) {
                        if (null == stmt) {
                            throw new DbQueryException("Can't prepare dummy statement");
                        }
                        stmt.executeQuery();
                    }
                }
            }
        } catch (Exception e) {
            throw new DbQueryException("Can't probe Database connection", e);
        }
        return true;
    }
}
