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
