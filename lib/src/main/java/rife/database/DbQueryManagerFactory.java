/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.UnsupportedJdbcDriverException;
import rife.tools.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class allows for {@link DbQueryManager}s to be created more
 * dynamically and with more features than by direct instantiation.
 * <p>By using the <code>DbQueryManagerFactory</code>,
 * <code>DbQueryManager</code> child classes can have custom methods that are
 * implemented by different "drivers", based on the database software behind
 * the {@link Datasource}. Database "drivers" are looked up through the
 * manager's classpath according to the package name and the encoded class
 * name of the JDBC driver (dots are replaced by underscores). The default, or
 * "generic" driver, must be created under this package and will be used when
 * no specific driver can be found for a particular <code>Datasource</code>.
 * All the created DbQueryManagers are cached in the provided cache and are
 * re-used on successive calls rather than being re-instantiated.
 *
 * @author JR Boyens (jboyens[remove] at uwyn dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class DbQueryManagerFactory {
    private static final String GENERIC_DRIVER = "generic";

    /**
     * Get a <code>DbQueryManager</code> instance.
     *
     * @param managerPackageName the package name that corresponds to the
     *                           location of the manager
     * @param cache              the cache to be used to cache the
     *                           <code>DbQueryManager</code>s
     * @param datasource         the datasource to instantiate the
     *                           <code>DbQueryManager</code> for
     * @return the created <code>DbQueryManager</code> instance
     * @since 1.0
     */
    protected static DbQueryManager getInstance(String managerPackageName, DbQueryManagerCache cache, Datasource datasource) {
        return getInstance(managerPackageName, cache, datasource, "");
    }

    /**
     * Get a <code>DbQueryManager</code> instance.
     *
     * @param managerPackageName the package name that corresponds to the
     *                           location of the manager
     * @param cache              the cache to be used to cache the
     *                           <code>DbQueryManager</code>s
     * @param datasource         the datasource to instantiate the
     *                           <code>DbQueryManager</code> for
     * @param identifier         the identifier to be used to uniquely identify
     *                           this <code>DbQueryManager</code>
     * @return the created <code>DbQueryManager</code> instance
     * @since 1.0
     */
    protected static DbQueryManager getInstance(String managerPackageName, DbQueryManagerCache cache, Datasource datasource, String identifier) {
        if (null == managerPackageName) throw new IllegalArgumentException("managerPackageName can't be null.");
        if (0 == managerPackageName.length()) throw new IllegalArgumentException("managerPackageName can't be empty.");
        if (null == cache) throw new IllegalArgumentException("cache can't be null.");
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");
        if (null == identifier) throw new IllegalArgumentException("identifier can't be null.");

        DbQueryManager dbquery_manager = null;

        synchronized (cache) {
            dbquery_manager = cache.get(datasource, identifier);
            if (dbquery_manager != null) {
                return dbquery_manager;
            }

            // construct an uniform package name
            StringBuilder package_name = new StringBuilder(managerPackageName);
            if (!managerPackageName.endsWith(".")) {
                package_name.append(".");
            }

            // construct the specialized driver class name
            StringBuilder specialized_name = new StringBuilder(package_name.toString());

            String driver = datasource.getAliasedDriver();
            specialized_name.append(StringUtils.encodeClassname(driver));

            try {
                try {
                    Class<DbQueryManager> specialized_class = (Class<DbQueryManager>) Class.forName(specialized_name.toString());
                    Constructor<DbQueryManager> specialized_constructor = specialized_class.getConstructor(Datasource.class);

                    dbquery_manager = specialized_constructor.newInstance(datasource);
                } catch (ClassNotFoundException e) {
                    // could not find a specialized class, try to get a generic driver
                    try {
                        // construct the generic driver class name
                        StringBuilder generic_name = new StringBuilder(package_name.toString());
                        generic_name.append(GENERIC_DRIVER);

                        Class<DbQueryManager> generic_class = (Class<DbQueryManager>) Class.forName(generic_name.toString());
                        Constructor<DbQueryManager> generic_constructor = generic_class.getConstructor(Datasource.class);

                        dbquery_manager = generic_constructor.newInstance(datasource);
                    } catch (ClassNotFoundException e2) {
                        throw new UnsupportedJdbcDriverException(driver, e);
                    }
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException e) {
                throw new UnsupportedJdbcDriverException(driver, e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() != null) {
                    throw new RuntimeException(e.getTargetException());
                } else {
                    throw new UnsupportedJdbcDriverException(driver, e);
                }
            }

            cache.put(datasource, identifier, dbquery_manager);
        }

        assert datasource == dbquery_manager.getDatasource();

        return dbquery_manager;
    }
}
