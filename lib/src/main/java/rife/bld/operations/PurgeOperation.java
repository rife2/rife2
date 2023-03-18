/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.Project;
import rife.bld.dependencies.*;

import java.io.File;
import java.util.*;

/**
 * Transitively checks all the artifacts for dependencies in the directories
 * that are separated out by scope, any files that aren't required will be deleted.
 * <p>
 * If a directory is not provided, no purge will occur for that
 * dependency scope.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class PurgeOperation {
    private final List<Repository> repositories_ = new ArrayList<>();
    private final DependencyScopes dependencies_ = new DependencyScopes();
    private File libCompileDirectory_;
    private File libRuntimeDirectory_;
    private File libStandaloneDirectory_;
    private File libTestDirectory_;

    /**
     * Performs the purge operation.
     *
     * @since 1.5
     */
    public void execute() {
        executePurgeCompileDependencies();
        executePurgeRuntimeDependencies();
        executePurgeStandaloneDependencies();
        executePurgeTestDependencies();
    }

    /**
     * Part of the {@link #execute} operation, purge the {@code compile} scope artifacts.
     *
     * @since 1.5
     */
    public void executePurgeCompileDependencies() {
        executePurgeScopedDependencies(Scope.compile, libCompileDirectory(), Scope.compile);
    }

    /**
     * Part of the {@link #execute} operation, purge the {@code runtime} scope artifacts.
     *
     * @since 1.5
     */
    public void executePurgeRuntimeDependencies() {
        executePurgeScopedDependencies(Scope.runtime, libRuntimeDirectory(), Scope.runtime);
    }

    /**
     * Part of the {@link #execute} operation, purge the {@code standalone} scope artifacts.
     *
     * @since 1.5
     */
    public void executePurgeStandaloneDependencies() {
        executePurgeScopedDependencies(Scope.standalone, libStandaloneDirectory(), Scope.compile, Scope.runtime);
    }

    /**
     * Part of the {@link #execute} operation, purge the {@code test} scope artifacts.
     *
     * @since 1.5
     */
    public void executePurgeTestDependencies() {
        executePurgeScopedDependencies(Scope.test, libTestDirectory(), Scope.compile, Scope.runtime);
    }

    /**
     * Part of the {@link #execute} operation, purge the artifacts for a particular dependency scope.
     *
     * @param scope                the scope whose artifacts should be purged
     * @param destinationDirectory the directory from which the artifacts should be purged
     * @param transitiveScopes     the scopes to use to resolve the transitive dependencies
     * @since 1.5
     */
    public void executePurgeScopedDependencies(Scope scope, File destinationDirectory, Scope... transitiveScopes) {
        if (destinationDirectory == null) {
            return;
        }

        var scoped_dependencies = dependencies().get(scope);
        if (scoped_dependencies != null) {
            var all_dependencies = new DependencySet();
            for (var dependency : scoped_dependencies) {
                all_dependencies.addAll(new DependencyResolver(repositories(), dependency).getAllDependencies(transitiveScopes));
            }

            var filenames = new HashSet<String>();
            for (var dependency : all_dependencies) {
                for (var url : new DependencyResolver(repositories(), dependency).getDownloadUrls()) {
                    filenames.add(url.substring(url.lastIndexOf("/") + 1));
                }
            }

            for (var file : destinationDirectory.listFiles()) {
                if (!filenames.contains(file.getName())) {
                    System.out.println("Deleting : " + file.getName());
                    file.delete();
                }
            }
        }
    }

    /**
     * Configures a compile operation from a {@link Project}.
     *
     * @param project the project to configure the compile operation from
     * @since 1.5
     */
    public PurgeOperation fromProject(Project project) {
        return repositories(project.repositories())
            .dependencies(project.dependencies())
            .libCompileDirectory(project.libCompileDirectory())
            .libRuntimeDirectory(project.libRuntimeDirectory())
            .libStandaloneDirectory(project.libStandaloneDirectory())
            .libTestDirectory(project.libTestDirectory());
    }

    /**
     * Provides repositories to resolve the dependencies against.
     *
     * @param repositories the repositories against which dependencies will be resolved
     * @return this operation instance
     * @since 1.5
     */
    public PurgeOperation repositories(List<Repository> repositories) {
        repositories_.addAll(repositories);
        return this;
    }

    /**
     * Provides scoped dependencies for artifact purge.
     *
     * @param dependencies the dependencies that will be resolved for artifact purge
     * @return this operation instance
     * @since 1.5
     */
    public PurgeOperation dependencies(DependencyScopes dependencies) {
        dependencies_.include(dependencies);
        return this;
    }

    /**
     * Provides the {@code compile} scope purge directory.
     *
     * @param directory the directory to purge the {@code compile} scope artifacts from
     * @return this operation instance
     * @since 1.5
     */
    public PurgeOperation libCompileDirectory(File directory) {
        libCompileDirectory_ = directory;
        return this;
    }

    /**
     * Provides the {@code runtime} scope purge directory.
     *
     * @param directory the directory to purge the {@code runtime} scope artifacts from
     * @return this operation instance
     * @since 1.5
     */
    public PurgeOperation libRuntimeDirectory(File directory) {
        libRuntimeDirectory_ = directory;
        return this;
    }

    /**
     * Provides the {@code standalone} scope purge directory.
     *
     * @param directory the directory to purge the {@code standalone} scope artifacts from
     * @return this operation instance
     * @since 1.5
     */
    public PurgeOperation libStandaloneDirectory(File directory) {
        libStandaloneDirectory_ = directory;
        return this;
    }

    /**
     * Provides the {@code test} scope purge directory.
     *
     * @param directory the directory to purge the {@code test} scope artifacts from
     * @return this operation instance
     * @since 1.5
     */
    public PurgeOperation libTestDirectory(File directory) {
        libTestDirectory_ = directory;
        return this;
    }

    /**
     * Retrieves the repositories in which the dependencies will be resolved.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the repositories used for dependency resolution
     * @since 1.5
     */
    public List<Repository> repositories() {
        return repositories_;
    }

    /**
     * Retrieves the scoped dependencies that will be used for artifact purge.
     * <p>
     * This is a modifiable structure that can be retrieved and changed.
     *
     * @return the scoped dependencies
     * @since 1.5
     */
    public DependencyScopes dependencies() {
        return dependencies_;
    }

    /**
     * Retrieves the {@code compile} scope purge directory.
     *
     * @return the {@code compile} scope purge directory
     * @since 1.5
     */
    public File libCompileDirectory() {
        return libCompileDirectory_;
    }

    /**
     * Retrieves the {@code runtime} scope purge directory.
     *
     * @return the {@code runtime} scope purge directory
     * @since 1.5
     */
    public File libRuntimeDirectory() {
        return libRuntimeDirectory_;
    }

    /**
     * Retrieves the {@code standalone} scope purge directory.
     *
     * @return the {@code standalone} scope purge directory
     * @since 1.5
     */
    public File libStandaloneDirectory() {
        return libStandaloneDirectory_;
    }

    /**
     * Retrieves the {@code test} scope purge directory.
     *
     * @return the {@code test} scope purge directory
     * @since 1.5
     */
    public File libTestDirectory() {
        return libTestDirectory_;
    }
}
