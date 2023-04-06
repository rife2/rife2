/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.*;
import rife.selector.NameSelector;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Contains a collection of {@code Datasource} instances.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.database.Datasource
 * @since 1.0
 */
public class Datasources {
    private final ConcurrentMap<String, Datasource> map_ = new ConcurrentHashMap<>();

    /**
     * Creates a new empty {@code Datasources} instance.
     *
     * @since 1.0
     */
    public Datasources()
    throws DatasourcesException {
    }

    /**
     * Returns the shared singleton instance of the
     * {@code Datasources} class.
     *
     * @return the singleton {@code Datasources} instance
     * @since 1.0
     */
    public static Datasources instance() {
        return DatasourcesSingleton.INSTANCE;
    }

    /**
     * Retrieves the {@code Datasource} that corresponds to a provided
     * name.
     *
     * @param name a {@code String} that identifies the
     *             {@code Datasource} that has to be retrieved
     * @return the requested {@code Datasource} instance; or
     * <p>
     * {@code null} if name isn't known
     * @since 1.0
     */
    public Datasource getDatasource(String name) {
        return map_.get(name);
    }

    /**
     * Retrieves the {@code Datasource} that corresponds an automatically
     * selected name, or a fallback datasource of none could be found that matches
     * the selected name.
     *
     * @param selector     a {@code NameSelector} that will automatically select
     *                     a name based on the current environment
     * @param fallbackName a {@code String} that identifies the
     *                     fallback {@code Datasource} that has to be used
     * @return the requested {@code Datasource} instance; or
     * <p>
     * {@code null} if no {@code Datasource} could be found
     * @since 1.0
     */
    public Datasource getDatasourceForSelector(NameSelector selector, String fallbackName) {
        var datasource = Datasources.instance().getDatasource(selector.getActiveName());
        if (datasource == null) {
            datasource = Datasources.instance().getDatasource(fallbackName);
        }

        return datasource;
    }

    /**
     * Stores a {@code Datasource} with a provided name to be able to
     * reference it later.
     *
     * @param name       a {@code String} that identifies the
     *                   {@code Datasource}
     * @param datasource the {@code Datasource} instance that has to be
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
     * Retrieves a collection of all the {@code Datasource} names that are
     * known by this {@code Datasources} instance.
     *
     * @return the requested {@code Collection}
     * @since 1.0
     */
    public Collection<String> getDatasourceNames() {
        return map_.keySet();
    }

    /**
     * Cleans up all connections that have been reserved by this datasource.
     *
     * @throws DatabaseException when an error occurred during the cleanup
     * @since 1.0
     */
    public void cleanup()
    throws DatabaseException {
        map_.forEach((s, datasource) -> datasource.cleanup());
        map_.clear();
    }
}
