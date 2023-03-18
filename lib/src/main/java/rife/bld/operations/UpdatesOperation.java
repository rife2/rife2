/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.Project;
import rife.bld.dependencies.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Determines which updates are available for provides dependencies.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class UpdatesOperation {
    private final List<Repository> repositories_ = new ArrayList<>();
    private final DependencyScopes dependencies_ = new DependencyScopes();
    private DependencyScopes updates_ = new DependencyScopes();

    /**
     * Performs the updates operation.
     *
     * @since 1.5
     */
    public void execute() {
        var result = new DependencyScopes();
        for (var entry : dependencies_.entrySet()) {
            var scope = entry.getKey();
            for (var dependency : entry.getValue()) {
                var latest = new DependencyResolver(repositories(), dependency).latestVersion();
                if (latest.compareTo(dependency.version()) > 0) {
                    var latest_dependency = new Dependency(dependency.groupId(), dependency.artifactId(), latest,
                        dependency.classifier(), dependency.type());
                    result.scope(scope).include(latest_dependency);
                }
            }
        }

        if (result.isEmpty()) {
            System.out.println("No dependency updates found.");
        } else {
            System.out.println("The following dependency updates were found.");
            for (var entry : result.entrySet()) {
                var scope = entry.getKey();
                System.out.println(scope + ":");
                for (var dependency : entry.getValue()) {
                    System.out.println("    " + dependency);
                }
            }
        }
        updates_ = result;
    }

    /**
     * Configures a compile operation from a {@link Project}.
     *
     * @param project the project to configure the compile operation from
     * @since 1.5
     */
    public UpdatesOperation fromProject(Project project) {
        return repositories(project.repositories())
            .dependencies(project.dependencies());
    }

    /**
     * Provides repositories to resolve the dependencies against.
     *
     * @param repositories the repositories against which dependencies will be resolved
     * @return this operation instance
     * @since 1.5
     */
    public UpdatesOperation repositories(List<Repository> repositories) {
        repositories_.addAll(repositories);
        return this;
    }

    /**
     * Provides scoped dependencies that will be checked for updates.
     *
     * @param dependencies the dependencies that will be checked for updates
     * @return this operation instance
     * @since 1.5
     */
    public UpdatesOperation dependencies(DependencyScopes dependencies) {
        dependencies_.include(dependencies);
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
     * Retrieves the scoped dependencies that will be checked for updates.
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
     * Retrieves the scoped dependencies with updates found after execution.
     *
     * @return the scoped dependencies with updates
     * @since 1.5
     */
    public DependencyScopes updates() {
        return updates_;
    }
}
