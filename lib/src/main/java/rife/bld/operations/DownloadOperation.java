/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.*;
import rife.bld.dependencies.*;
import rife.tools.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadOperation {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Downloads all dependencies of a RIFE2 application";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Downloads all dependencies of a RIFE2 application
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private DependencyScopes dependencies_ = new DependencyScopes();
    private List<Repository> repositories_ = new ArrayList<>();;
    private File libCompileDirectory_;
    private File libRuntimeDirectory_;
    private File libStandaloneDirectory_;
    private File libTestDirectory_;

    public DownloadOperation() {
    }

    public void execute() {
        downloadCompileDependencies();
        downloadRuntimeDependencies();
        downloadStandaloneDependencies();
        downloadTestDependencies();
    }

    public void downloadCompileDependencies() {
        var compile_deps = dependencies().get(Scope.compile);
        if (compile_deps != null) {
            for (var dependency : compile_deps) {
                new DependencyResolver(repositories(), dependency)
                    .downloadTransitivelyIntoFolder(libCompileDirectory(), Scope.compile);
            }
        }
    }

    public void downloadRuntimeDependencies() {
        var runtime_deps = dependencies().get(Scope.runtime);
        if (runtime_deps != null) {
            for (var dependency : runtime_deps) {
                new DependencyResolver(repositories(), dependency)
                    .downloadTransitivelyIntoFolder(libRuntimeDirectory(), Scope.runtime);
            }
        }
    }

    public void downloadStandaloneDependencies() {
        var standalone_deps = dependencies().get(Scope.standalone);
        if (standalone_deps != null) {
            for (var dependency : standalone_deps) {
                new DependencyResolver(repositories(), dependency)
                    .downloadTransitivelyIntoFolder(libStandaloneDirectory(), Scope.compile, Scope.runtime);
            }
        }
    }

    public void downloadTestDependencies() {
        var test_deps = dependencies().get(Scope.test);
        if (test_deps != null) {
            for (var dependency : test_deps) {
                new DependencyResolver(repositories(), dependency)
                    .downloadTransitivelyIntoFolder(libTestDirectory(), Scope.compile, Scope.runtime);
            }
        }
    }

    public DownloadOperation fromProject(Project project) {
        return dependencies(project.dependencies)
            .repositories(project.repositories)
            .libCompileDirectory(project.libCompileDirectory())
            .libRuntimeDirectory(project.libRuntimeDirectory())
            .libStandaloneDirectory(project.libStandaloneDirectory())
            .libTestDirectory(project.libTestDirectory());
    }

    public DownloadOperation dependencies(DependencyScopes deps) {
        dependencies_ = new DependencyScopes(deps);
        return this;
    }

    public DownloadOperation repositories(List<Repository> reps) {
        repositories_ = new ArrayList<>(reps);
        return this;
    }

    public DownloadOperation libCompileDirectory(File directory) {
        libCompileDirectory_ = directory;
        return this;
    }

    public DownloadOperation libRuntimeDirectory(File directory) {
        libRuntimeDirectory_ = directory;
        return this;
    }

    public DownloadOperation libStandaloneDirectory(File directory) {
        libStandaloneDirectory_ = directory;
        return this;
    }

    public DownloadOperation libTestDirectory(File directory) {
        libTestDirectory_ = directory;
        return this;
    }

    public DependencyScopes dependencies() {
        return dependencies_;
    }

    public List<Repository> repositories() {
        return repositories_;
    }

    public File libCompileDirectory() {
        return libCompileDirectory_;
    }

    public File libRuntimeDirectory() {
        return libRuntimeDirectory_;
    }

    public File libStandaloneDirectory() {
        return libStandaloneDirectory_;
    }

    public File libTestDirectory() {
        return libTestDirectory_;
    }
}
