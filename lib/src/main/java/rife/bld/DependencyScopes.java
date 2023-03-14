/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.dependencies.Scope;

import java.util.LinkedHashMap;

public class DependencyScopes extends LinkedHashMap<Scope, DependencySet> {
    public DependencyScopes() {
    }

    public DependencyScopes(DependencyScopes other) {
        for (var dep : other.entrySet()) {
            put(dep.getKey(), new DependencySet(dep.getValue()));
        }
    }

    public DependencySet scope(Scope scope) {
        return computeIfAbsent(scope, k -> new DependencySet());
    }
}
