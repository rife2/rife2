/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.capabilities;

import rife.database.DbConnection;
import rife.database.DbPreparedStatement;
import rife.database.DbResultSet;
import rife.database.DbResultSetHandler;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.Query;

public class AbstractCapabilitiesCompensator implements CapabilitiesCompensator {
    public DbPreparedStatement getCapablePreparedStatement(Query query, DbResultSetHandler handler, DbConnection connection)
    throws DatabaseException {
        query.setExcludeUnsupportedCapabilities(true);

        if (null == handler) {
            return connection.getPreparedStatement(query);
        }

        return handler.getPreparedStatement(query, connection);
    }

    public DbResultSet getCapableResultSet(DbPreparedStatement statement)
    throws DatabaseException {
        return statement.getResultSet();
    }
}

