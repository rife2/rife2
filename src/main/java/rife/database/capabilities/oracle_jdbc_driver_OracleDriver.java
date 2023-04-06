/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.capabilities;

import rife.database.DbConnection;
import rife.database.DbPreparedStatement;
import rife.database.DbResultSet;
import rife.database.DbResultSetHandler;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.Query;

public class oracle_jdbc_driver_OracleDriver extends AbstractCapabilitiesCompensator {
    private LimitOffsetCompensator mLimitOffsetCompensator = new LimitOffsetCompensator();

    public DbPreparedStatement getCapablePreparedStatement(Query query, DbResultSetHandler handler, DbConnection connection)
    throws DatabaseException {
        query.setExcludeUnsupportedCapabilities(true);

        // either create a new prepared statement or get it from the handler
        DbPreparedStatement statement = null;
        if (null == handler) {
            statement = connection.getPreparedStatement(query);
        } else {
            statement = handler.getPreparedStatement(query, connection);
        }

        mLimitOffsetCompensator.handleCapablePreparedStatement(statement);

        return statement;
    }

    public DbResultSet getCapableResultSet(DbPreparedStatement statement)
    throws DatabaseException {
        mLimitOffsetCompensator.handleCapableResultSet(statement);

        return statement.getResultSet();
    }
}

