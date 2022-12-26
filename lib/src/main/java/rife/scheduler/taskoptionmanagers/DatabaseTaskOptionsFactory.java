/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

public abstract class DatabaseTaskOptionsFactory extends DbQueryManagerFactory {
    private static final String MANAGER_PACKAGE_NAME = DatabaseTaskOptionsFactory.class.getPackage().getName() + ".databasedrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    public static DatabaseTaskOptions instance(Datasource datasource) {
        return (DatabaseTaskOptions) instance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}
