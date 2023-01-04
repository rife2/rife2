/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

/**
 * Factory for {@link DatabaseSessionValidator} manager instances that creates singletons
 * based on the {@code Datasource} and an optional differentiating
 * identifier.
 *
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class DatabaseSessionValidatorFactory extends DbQueryManagerFactory {
    /**
     * The package name of the datasource-specific implementations
     */
    public static final String MANAGER_PACKAGE_NAME = DatabaseSessionValidatorFactory.class.getPackage().getName() + ".databasedrivers.";

    private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();

    /**
     * Return an instance of {@code DatabaseSessionValidator} for the provided
     * {@code Datasource}.
     *
     * @param datasource the datasource that will be used to create the manager
     * @return the requested {@code DatabaseSessionValidator} instance
     * @since 1.0
     */
    public static DatabaseSessionValidator instance(Datasource datasource) {
        return (DatabaseSessionValidator) instance(MANAGER_PACKAGE_NAME, cache_, datasource);
    }
}
