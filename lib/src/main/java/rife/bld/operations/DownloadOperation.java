/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.bld.dependencies.*;
import rife.tools.StringUtils;

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

    public final Project project;

    public DownloadOperation(Project project) {
        this.project = project;
    }

    public void execute() {
        downloadCompileDependencies();
        downloadRuntimeDependencies();
        downloadStandaloneDependencies();
        downloadTestDependencies();
    }

    public void downloadCompileDependencies() {
        var compile_deps = project.dependencies.get(Scope.compile);
        if (compile_deps != null) {
            for (var dependency : compile_deps) {
                new DependencyResolver(project.repositories, dependency)
                    .downloadTransitivelyIntoFolder(project.libCompileDirectory(), Scope.compile);
            }
        }
    }

    public void downloadRuntimeDependencies() {
        var runtime_deps = project.dependencies.get(Scope.runtime);
        if (runtime_deps != null) {
            for (var dependency : runtime_deps) {
                new DependencyResolver(project.repositories, dependency)
                    .downloadTransitivelyIntoFolder(project.libRuntimeDirectory(), Scope.runtime);
            }
        }
    }

    public void downloadStandaloneDependencies() {
        var standalone_deps = project.dependencies.get(Scope.standalone);
        if (standalone_deps != null) {
            for (var dependency : standalone_deps) {
                new DependencyResolver(project.repositories, dependency)
                    .downloadTransitivelyIntoFolder(project.libStandaloneDirectory(), Scope.compile, Scope.runtime);
            }
        }
    }

    public void downloadTestDependencies() {
        var test_deps = project.dependencies.get(Scope.test);
        if (test_deps != null) {
            for (var dependency : test_deps) {
                new DependencyResolver(project.repositories, dependency)
                    .downloadTransitivelyIntoFolder(project.libTestDirectory(), Scope.compile, Scope.runtime);
            }
        }
    }
}
