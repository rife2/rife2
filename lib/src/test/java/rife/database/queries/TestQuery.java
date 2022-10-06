/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.TestDatasources;

public class TestQuery {
    // TODO
    protected static Datasource mPgsql = TestDatasources.mPgsql;
    protected static Datasource mOracle = null;
    protected static Datasource mHsqldb = TestDatasources.mHsqldb;
    protected static Datasource mH2 = TestDatasources.mH2;
    protected static Datasource mMysql = TestDatasources.mMysql;
    protected static Datasource mDerby = TestDatasources.mDerby;
}
