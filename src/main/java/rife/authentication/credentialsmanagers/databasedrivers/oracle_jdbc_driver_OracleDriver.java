/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.databasedrivers;

import rife.database.Datasource;
import rife.database.queries.Select;

public class oracle_jdbc_driver_OracleDriver extends generic {
    public oracle_jdbc_driver_OracleDriver(Datasource datasource) {
        super(datasource);

        getFreeUserId_ = new Select(getDatasource())
            .field("NVL(MAX(userId)+1, 0) as freeUserId")
            .from(createTableUser_.getTable());
    }
}
