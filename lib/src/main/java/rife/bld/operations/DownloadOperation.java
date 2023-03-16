/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.*;
import rife.bld.dependencies.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Transitively downloads all the artifacts for dependencies into
 * directories that are separated out by scope.
 * <p>
 * If a directory is not provided, no download will occur for that
 * dependency scope.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class DownloadOperation {
    private final List<Repository> repositories_ = new ArrayList<>();
    private final DependencyScopes dependencies_ = new DependencyScopes();
    private File libCompileDirectory_;
    private File libRuntimeDirectory_;
    private File libStandaloneDirectory_;
    private File libTestDirectory_;

    /**
     * Performs the download operation.
     *
     * @since 1.5
     */
    public void execute() {
        executeDownloadCompileDependencies();
        executeDownloadRuntimeDependencies();
        executeDownloadStandaloneDependencies();
        executeDownloadTestDependencies();
    }

    /**
     * Part of the {@link #execute} operation, download the {@code compile} scope artifacts.
     *
     * @since 1.5
     */
    public void executeDownloadCompileDependencies() {
        if (libCompileDirectory() == null) {
            return;
        }

        var compile_deps = dependencies().get(Scope.compile);
        if (compile_deps != null) {
            for (var dependency : compile_deps) {
                new DependencyResolver(repositories(), dependency)
                    .downloadTransitivelyIntoDirectory(libCompileDirectory(), Scope.compile);
            }
        }
    }

    /**
     * Part of the {@link #execute} operation, download the {@code runtime} scope artifacts.
     *
     * @since 1.5
     */
    public void executeDownloadRuntimeDependencies() {
        if (libRuntimeDirectory() == null) {
            return;
        }

        var runtime_deps = dependencies().get(Scope.runtime);
        if (runtime_deps != null) {
            for (var dependency : runtime_deps) {
                new DependencyResolver(repositories(), dependency)
                    .downloadTransitivelyIntoDirectory(libRuntimeDirectory(), Scope.runtime);
            }
        }
    }

    /**
     * Part of the {@link #execute} operation, download the {@code standalone} scope artifacts.
     *
     * @since 1.5
     */
    public void executeDownloadStandaloneDependencies() {
        if (libStandaloneDirectory() == null) {
            return;
        }

        var standalone_deps = dependencies().get(Scope.standalone);
        if (standalone_deps != null) {
            for (var dependency : standalone_deps) {
                new DependencyResolver(repositories(), dependency)
                    .downloadTransitivelyIntoDirectory(libStandaloneDirectory(), Scope.compile, Scope.runtime);
            }
        }
    }

    /**
     * Part of the {@link #execute} operation, download the {@code test} scope artifacts.
     *
     * @since 1.5
     */
    public void executeDownloadTestDependencies() {
        if (libTestDirectory() == null) {
            return;
        }

        var test_deps = dependencies().get(Scope.test);
        if (test_deps != null) {
            for (var dependency : test_deps) {
                new DependencyResolver(repositories(), dependency)
                    .downloadTransitivelyIntoDirectory(libTestDirectory(), Scope.compile, Scope.runtime);
            }
        }
    }

    /**
     * Configures a compile operation from a {@link Project}.
     *
     * @param project the project to configure the compile operation from
     * @since 1.5
     */
    public DownloadOperation fromProject(Project project) {
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
    public DownloadOperation repositories(List<Repository> repositories) {
        repositories_.addAll(repositories);
        return this;
    }

    /**
     * Provides scoped dependencies for artifact download.
     *
     * @param dependencies the dependencies that will be resolved for artifact download
     * @return this operation instance
     * @since 1.5
     */
    public DownloadOperation dependencies(DependencyScopes dependencies) {
        dependencies_.include(dependencies);
        return this;
    }

    /**
     * Provides the {@code compile} scope download directory.
     *
     * @param directory the directory to download the {@code compile} scope artifacts into
     * @return this operation instance
     * @since 1.5
     */
    public DownloadOperation libCompileDirectory(File directory) {
        libCompileDirectory_ = directory;
        return this;
    }

    /**
     * Provides the {@code runtime} scope download directory.
     *
     * @param directory the directory to download the {@code runtime} scope artifacts into
     * @return this operation instance
     * @since 1.5
     */
    public DownloadOperation libRuntimeDirectory(File directory) {
        libRuntimeDirectory_ = directory;
        return this;
    }

    /**
     * Provides the {@code standalone} scope download directory.
     *
     * @param directory the directory to download the {@code standalone} scope artifacts into
     * @return this operation instance
     * @since 1.5
     */
    public DownloadOperation libStandaloneDirectory(File directory) {
        libStandaloneDirectory_ = directory;
        return this;
    }

    /**
     * Provides the {@code test} scope download directory.
     *
     * @param directory the directory to download the {@code test} scope artifacts into
     * @return this operation instance
     * @since 1.5
     */
    public DownloadOperation libTestDirectory(File directory) {
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
     * Retrieves the scoped dependencies that will be used for artifact download.
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
     * Retrieves the {@code compile} scope download directory.
     *
     * @return the {@code compile} scope download directory
     * @since 1.5
     */
    public File libCompileDirectory() {
        return libCompileDirectory_;
    }

    /**
     * Retrieves the {@code runtime} scope download directory.
     *
     * @return the {@code runtime} scope download directory
     * @since 1.5
     */
    public File libRuntimeDirectory() {
        return libRuntimeDirectory_;
    }

    /**
     * Retrieves the {@code standalone} scope download directory.
     *
     * @return the {@code standalone} scope download directory
     * @since 1.5
     */
    public File libStandaloneDirectory() {
        return libStandaloneDirectory_;
    }

    /**
     * Retrieves the {@code test} scope download directory.
     *
     * @return the {@code test} scope download directory
     * @since 1.5
     */
    public File libTestDirectory() {
        return libTestDirectory_;
    }
}
