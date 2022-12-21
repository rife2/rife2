/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.queries.Query;
import rife.tools.ExceptionUtils;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * By extending this class it's possible to easily customize the behaviour of
 * some methods in the {@link DbQueryManager} class.
 * <p>You're able to perform custom logic with the result set of a query by
 * overriding the {@link #concludeResults(DbResultSet) concludeResults} method
 * and returning an object.
 * <p>You're not supposed to close the resultset in this method.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see DbResultSet
 * @see DbQueryManager
 * @since 1.0
 */
public abstract class DbResultSetHandler implements Cloneable {
    public DbStatement createStatement(DbConnection connection) {
        return connection.createStatement();
    }

    public DbPreparedStatement getPreparedStatement(Query query, DbConnection connection) {
        return connection.getPreparedStatement(query);
    }

    public Object concludeResults(DbResultSet resultset)
    throws SQLException {
        return null;
    }

    /**
     * Simply clones the instance with the default clone method since this
     * class contains no member variables.
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
