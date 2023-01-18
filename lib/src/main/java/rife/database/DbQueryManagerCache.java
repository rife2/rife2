/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is a simple cache for {@link DbQueryManager} objects. {@link
 * DbQueryManager} objects are cached by their related {@link Datasource} and
 * an identifier.
 *
 * @author JR Boyens (jboyens[remove] at uwyn dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class DbQueryManagerCache {
    private final ConcurrentMap<Datasource, ConcurrentHashMap<String, DbQueryManager>> cache_ = new ConcurrentHashMap<>();

    /**
     * Default constructor
     *
     * @since 1.0
     */
    public DbQueryManagerCache() {
    }

    /**
     * Retrieve a cached {@link DbQueryManager}
     *
     * @param datasource the {@link Datasource} associated with the
     *                   desired {@link DbQueryManager}
     * @param identifier the identifier associate with the desired {@link
     *                   DbQueryManager}
     * @return the cached {@link DbQueryManager}
     * @since 1.0
     */
    public DbQueryManager get(Datasource datasource, String identifier) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");
        if (null == identifier) throw new IllegalArgumentException("identifier can't be null.");

        var managers = cache_.get(datasource);
        if (null == managers) {
            return null;
        }

        return managers.get(identifier);
    }

    /**
     * Place a {@link DbQueryManager} in the cache
     *
     * @param datasource     the {@link Datasource} associated with the {@link
     *                       DbQueryManager} to put in the cache
     * @param identifier     the identifier associated with the {@link
     *                       DbQueryManager} to put in the cache
     * @param dbQueryManager the {@link DbQueryManager} to put in the
     *                       cache
     * @since 1.0
     */
    public void put(Datasource datasource, String identifier, DbQueryManager dbQueryManager) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");
        if (null == identifier) throw new IllegalArgumentException("identifier can't be null.");
        if (null == dbQueryManager) throw new IllegalArgumentException("dbQueryManager can't be null.");

        var managers = cache_.computeIfAbsent(datasource, k -> new ConcurrentHashMap<>());
        managers.put(identifier, dbQueryManager);

        assert cache_.containsKey(datasource);
        assert cache_.get(datasource).containsKey(identifier);
        assert cache_.get(datasource).get(identifier) == dbQueryManager;
    }
}
