/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.rawstoredrivers;

import rife.database.Datasource;
import rife.database.DbConnection;
import rife.database.DbPreparedStatement;
import rife.database.queries.Query;

import java.sql.ResultSet;

public class oracle_jdbc_driver_OracleDriver extends generic {
    public oracle_jdbc_driver_OracleDriver(Datasource datasource) {
        super(datasource);
    }

    protected DbPreparedStatement getStreamPreparedStatement(Query query, DbConnection connection) {
        var statement = connection.getPreparedStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        statement.setFetchDirection(ResultSet.FETCH_FORWARD);
        statement.setFetchSize(1);
        return statement;
    }
}
