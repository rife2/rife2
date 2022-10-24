/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

public abstract class DatabaseResourcesFactory extends DbQueryManagerFactory {
    public static final String MANAGER_PACKAGE_NAME = DatabaseResourcesFactory.class.getPackage().getName() + ".databasedrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    public static DatabaseResources getInstance(Datasource datasource) {
        return (DatabaseResources) getInstance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}

