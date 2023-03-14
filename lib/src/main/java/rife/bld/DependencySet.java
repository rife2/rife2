/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.dependencies.Dependency;

import java.util.*;

/**
 * Convenience class to handle a set of {@link Dependency} object.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class DependencySet extends LinkedHashSet<Dependency> {
    /**
     * Creates an empty dependency set.
     *
     * @since 1.5
     */
    public DependencySet() {
    }

    /**
     * Creates a dependency set from another one.
     *
     * @param other the other set to create this one from
     * @since 1.5
     */
    public DependencySet(DependencySet other) {
        addAll(other);
    }

    /**
     * Includes a dependency into the dependency set.
     *
     * @param dependency the dependency to include
     * @return this dependency set instance
     * @since 1.5
     */
    public DependencySet include(Dependency dependency) {
        add(dependency);
        return this;
    }
}