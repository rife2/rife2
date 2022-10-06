/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.*;

import java.util.Collection;
import java.util.HashMap;

/**
 * Contains a collection of <code>Datasource</code> instances.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @version $Revision$
 * @see rife.database.Datasource
 * @since 1.0
 */
public class Datasources {
    private HashMap<String, Datasource> map_ = new HashMap<>();

    /**
     * Creates a new empty <code>Datasources</code> instance.
     *
     * @since 1.0
     */
    public Datasources()
    throws DatasourcesException {
    }

    /**
     * Returns the shared singleton instance of the
     * <code>Datasources</code> class.
     *
     * @return the singleton <code>Datasources</code> instance
     * @since 2.0
     */
    public static Datasources instance() {
        return DatasourcesSingleton.INSTANCE;
    }

    /**
     * Retrieves the <code>Datasource</code> that corresponds to a provided
     * name.
     *
     * @param name a <code>String</code> that identifies the
     *             <code>Datasource</code> that has to be retrieved
     * @return the requested <code>Datasource</code> instance; or
     * <p>
     * <code>null</code> if name isn't known
     * @since 1.0
     */
    public Datasource getDatasource(String name) {
        return map_.get(name);
    }

    /**
     * Stores a <code>Datasource</code> with a provided name to be able to
     * reference it later.
     *
     * @param name       a <code>String</code> that identifies the
     *                   <code>Datasource</code>
     * @param datasource the <code>Datasource</code> instance that has to be
     *                   stored
     * @since 1.0
     */
    public void setDatasource(String name, Datasource datasource) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        map_.put(name, datasource);
    }

    /**
     * Retrieves a collection of all the <code>Datasource</code> names that are
     * known by this <code>Datasources</code> instance.
     *
     * @return the requested <code>Collection</code>
     * @since 1.0
     */
    public Collection<String> getDatasourceNames() {
        return map_.keySet();
    }

    /**
     * Cleans up all connections that have been reserved by this datasource.
     *
     * @throws DatabaseException when an error occured during the cleanup
     * @since 1.0
     */
    public void cleanup()
    throws DatabaseException {
        synchronized (this) {
            if (null == map_) {
                return;
            }

            HashMap<String, Datasource> data_sources = map_;
            map_ = null;

            for (Datasource datasource : data_sources.values()) {
                datasource.cleanup();
            }
        }
    }
}
