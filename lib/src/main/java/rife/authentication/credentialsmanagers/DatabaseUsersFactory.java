/*
 * Copyright 2001-2008 Steven Grimm (koreth[remove] at midwinter dot com) and
 * Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.authentication.credentialsmanagers;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

/**
 * Factory for {@link DatabaseUsers} manager instances that creates singletons
 * based on the {@code Datasource} and an optional differentiating
 * identifier.
 *
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class DatabaseUsersFactory extends DbQueryManagerFactory {
    /**
     * The package name of the datasource-specific implementations
     */
    public static final String MANAGER_PACKAGE_NAME = DatabaseUsersFactory.class.getPackage().getName() + ".databasedrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    /**
     * Return an instance of {@code DatabaseUsers} for the provided
     * {@code Datasource}.
     *
     * @param datasource the datasource that will be used to create the manager
     * @return the requested {@code DatabaseUsers} instance
     * @since 1.0
     */
    public static DatabaseUsers instance(Datasource datasource) {
        return (DatabaseUsers) instance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}
