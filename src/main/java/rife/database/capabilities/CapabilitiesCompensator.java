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

public interface CapabilitiesCompensator {
    public DbPreparedStatement getCapablePreparedStatement(Query query, DbResultSetHandler handler, DbConnection connection)
    throws DatabaseException;

    public DbResultSet getCapableResultSet(DbPreparedStatement statement)
    throws DatabaseException;
}

