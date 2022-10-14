/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.DbQueryManagerFactory;

/**
 * Factory for {@link DatabaseSessions} manager instances that creates singletons
 * based on the {@code Datasource}.
 *
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @version $Revision$
 * @since 1.0
 */
public class DatabaseSessionsFactory extends DbQueryManagerFactory
{
	/** The package name of the datasource-specific implementations */
	public static final String	MANAGER_PACKAGE_NAME = DatabaseSessionsFactory.class.getPackage().getName()+".databasedrivers.";
	
	private static final DbQueryManagerCache cache_ = new DbQueryManagerCache();
	
	/**
	 * Return an instance of {@code DatabaseSessions} for the provided
	 * {@code Datasource}.
	 *
	 * @param datasource the datasource that will be used to create the manager
	 * @return the requested {@code DatabaseSessions} instance
	 * @since 1.0
	 */
	public static DatabaseSessions getInstance(Datasource datasource)
	{
		return (DatabaseSessions)getInstance(MANAGER_PACKAGE_NAME, cache_, datasource);
	}

}
