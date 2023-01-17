/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

public abstract class TestDbQueryManagerFactoryImpl extends DbQueryManagerFactory {
    public static final String MANAGER_PACKAGE_NAME = TestDbQueryManagerFactoryImpl.class.getPackage().getName() + ".testdatabasedrivers.";

    private static DbQueryManagerCache mCache = new DbQueryManagerCache();

    public static TestDbQueryManagerImpl getInstance(Datasource datasource) {
        return (TestDbQueryManagerImpl) instance(MANAGER_PACKAGE_NAME, mCache, datasource);
    }

    public static TestDbQueryManagerImpl getInstance(Datasource datasource, String identifier) {
        return (TestDbQueryManagerImpl) instance(MANAGER_PACKAGE_NAME, mCache, datasource, identifier);
    }
}
