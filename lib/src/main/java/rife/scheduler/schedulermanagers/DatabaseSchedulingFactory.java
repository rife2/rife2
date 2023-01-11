/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.schedulermanagers;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

public abstract class DatabaseSchedulingFactory extends DbQueryManagerFactory {
    public static final String MANAGER_PACKAGE_NAME = DatabaseSchedulingFactory.class.getPackage().getName() + ".databasedrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    public static DatabaseScheduling instance(Datasource datasource) {
        return (DatabaseScheduling) instance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}
