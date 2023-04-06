/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.util.List;

/**
 * Cache to store previous dependency resolvers.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.18
 */
public interface DependencyResolverCache {
    /**
     * Dummy cache instance that can be used as a placeholder until a real cache is registered.
     *
     * @since 1.5.18
     */
    DependencyResolverCache DUMMY = new DependencyResolverCache() {};

    /**
     * Retrieves an existing resolver or creates a new one of no existing one could be found.
     * <p>
     * New resolvers will be automatically stored in the cache for later use.
     *
     * @param repositories the repositories that will be registered if a new resolver is created
     * @param dependency the dependency to get or create the resolver for
     * @return the dependency resolver
     * @since 1.5.18
     */
    default DependencyResolver getOrCreateResolver(List<Repository> repositories, Dependency dependency) {
        return new DependencyResolver(repositories, dependency);
    }

    default Xml2MavenPom getMavenPom(PomDependency dependency) {
        return null;
    }

    default void cacheMavenPom(PomDependency dependency, Xml2MavenPom pom) {
    }
}
