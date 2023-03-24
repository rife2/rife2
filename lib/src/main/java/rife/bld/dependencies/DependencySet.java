/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import rife.bld.dependencies.exceptions.DependencyDownloadException;

import java.io.File;
import java.util.*;

/**
 * Convenience class to handle a set of {@link Dependency} objects.
 * <p>
 * Only a single version of each dependency can exist in this set.
 * When adding a new dependency, it will only be added if it didn't exist
 * in the set yet, or if the new dependency has a higher version than
 * the existing one.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class DependencySet extends AbstractSet<Dependency> implements Set<Dependency> {
    private final Map<Dependency, Dependency> dependencies_ = new LinkedHashMap<>();
    private final Set<LocalDependency> localDependencies_ = new LinkedHashSet<>();

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

    /**
     * Includes a local dependency into the dependency set.
     * <p>
     * Local dependencies aren't resolved and point to a location on
     * the file system.
     *
     * @param dependency the dependency to include
     * @return this dependency set instance
     * @since 1.5.2
     */
    public DependencySet include(LocalDependency dependency) {
        localDependencies_.add(dependency);
        return this;
    }

    /**
     * Retrieves the local dependencies.
     *
     * @return the set of local dependencies
     * @since 1.5.2
     */
    public Set<LocalDependency> localDependencies() {
        return localDependencies_;
    }

    /**
     * Downloads the artifact for the dependencies into the provided directory.
     * <p>
     * The destination directory must exist and be writable.
     *
     * @param repositories the repositories to use for the download
     * @param directory    the directory to download the artifacts into
     * @throws DependencyDownloadException when an error occurred during the download
     * @since 1.5
     */
    public void downloadIntoDirectory(List<Repository> repositories, File directory) {
        for (var dependency : this) {
            new DependencyResolver(repositories, dependency).downloadIntoDirectory(directory);
        }
    }

    /**
     * Returns the dependency that was stored in the set.
     * <p>
     * The version can be different from the dependency passed in and this
     * method can be used to look up the actual version of the dependency in the set.
     *
     * @param dependency the dependency to look for
     * @return the dependency in the set; or
     * {@code null} if no such dependency exists
     * @since 1.5
     */
    public Dependency get(Dependency dependency) {
        return dependencies_.get(dependency);
    }

    public boolean add(Dependency dependency) {
        var existing = dependencies_.get(dependency);
        if (existing == null) {
            dependencies_.put(dependency, dependency);
            return true;
        }
        if (dependency.version().compareTo(existing.version()) > 0) {
            dependencies_.remove(dependency);
            dependencies_.put(dependency, dependency);
            return true;
        }
        return false;
    }

    public Iterator<Dependency> iterator() {
        return dependencies_.keySet().iterator();
    }

    public int size() {
        return dependencies_.size();
    }
}