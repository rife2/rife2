/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

public abstract class DatabaseTasksFactory extends DbQueryManagerFactory {
    private static final String MANAGER_PACKAGE_NAME = DatabaseTasksFactory.class.getPackage().getName() + ".databasedrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    public static DatabaseTasks getInstance(Datasource datasource) {
        return (DatabaseTasks) getInstance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}
