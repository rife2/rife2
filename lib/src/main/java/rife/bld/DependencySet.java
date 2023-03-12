/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.dependencies.Dependency;

import java.util.*;

public class DependencySet extends LinkedHashSet<Dependency> {
    public DependencySet() {
    }

    public DependencySet(DependencySet other) {
        addAll(other);
    }

    public DependencySet include(Dependency dependency) {
        add(dependency);
        return this;
    }
}