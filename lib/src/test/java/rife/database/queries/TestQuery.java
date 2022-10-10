/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.TestDatasources;

public class TestQuery {
    protected static Datasource PGSQL = TestDatasources.PGSQL;
    // TODO : oracle database
    protected static Datasource ORACLE = null;
    protected static Datasource HSQLDB = TestDatasources.HSQLDB;
    protected static Datasource H2 = TestDatasources.H2;
    protected static Datasource MYSQL = TestDatasources.MYSQL;
    protected static Datasource DERBY = TestDatasources.DERBY;
}
