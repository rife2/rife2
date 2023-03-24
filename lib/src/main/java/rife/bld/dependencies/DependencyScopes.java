/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.util.LinkedHashMap;

/**
 * Convenience class to map a {@link Scope} to its dependencies.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class DependencyScopes extends LinkedHashMap<Scope, DependencySet> {
    /**
     * Creates an empty dependency scope map.
     *
     * @since 1.5
     */
    public DependencyScopes() {
    }

    /**
     * Creates a dependency scope map from another one.
     *
     * @param other the other map to create this one from
     * @since 1.5
     */
    public DependencyScopes(DependencyScopes other) {
        for (var entry : other.entrySet()) {
            put(entry.getKey(), new DependencySet(entry.getValue()));
        }
    }

    /**
     * Includes all the dependencies from another dependency scope map.
     *
     * @param other the other map to include dependencies from
     * @since 1.5
     */
    public void include(DependencyScopes other) {
        for (var entry : other.entrySet()) {
            var dependencies = get(entry.getKey());
            if (dependencies == null) {
                dependencies = new DependencySet();
                put(entry.getKey(), dependencies);
            }
            dependencies.addAll(entry.getValue());
        }
    }

    /**
     * Retrieves the {@link DependencySet} for a particular scope.
     *
     * @param scope the scope to retrieve the dependencies for
     * @return the scope's {@code DependencySet};
     * or an empty {@code DependencySet} if none have been defined for the provided scope.
     * @since 1.5
     */
    public DependencySet scope(Scope scope) {
        return computeIfAbsent(scope, k -> new DependencySet());
    }
}
