/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

public abstract class DatabaseImageStoreFactory extends DbQueryManagerFactory {
    public static final String MANAGER_PACKAGE_NAME = DatabaseImageStoreFactory.class.getPackage().getName() + ".imagestoredrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    public static DatabaseImageStore instance(Datasource datasource) {
        return (DatabaseImageStore) instance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}

