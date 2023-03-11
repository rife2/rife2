package rife.bld;

import rife.bld.dependencies.Scope;

import java.util.LinkedHashMap;

public class DependencyScopes extends LinkedHashMap<Scope, DependencySet> {
    public DependencySet scope(Scope scope) {
        return computeIfAbsent(scope, k -> new DependencySet());
    }
}
