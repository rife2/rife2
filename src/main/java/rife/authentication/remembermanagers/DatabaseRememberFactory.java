/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

/**
 * Factory for {@link DatabaseRemember} manager instances that creates singletons
 * based on the {@code Datasource}.
 *
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class DatabaseRememberFactory extends DbQueryManagerFactory {
    /**
     * The package name of the datasource-specific implementations
     */
    public static final String MANAGER_PACKAGE_NAME = DatabaseRememberFactory.class.getPackage().getName() + ".databasedrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    /**
     * Return an instance of {@code DatabaseRemember} for the provided
     * {@code Datasource}.
     *
     * @param datasource the datasource that will be used to create the manager
     * @return the requested {@code DatabaseRemember} instance
     * @since 1.0
     */
    public static DatabaseRemember instance(Datasource datasource) {
        return (DatabaseRemember) instance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}
