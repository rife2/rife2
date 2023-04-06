/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

public abstract class DatabaseRawStoreFactory extends DbQueryManagerFactory {
    public static final String MANAGER_PACKAGE_NAME = DatabaseRawStoreFactory.class.getPackage().getName() + ".rawstoredrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    public static DatabaseRawStore instance(Datasource datasource) {
        return (DatabaseRawStore) instance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}

