/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.Project;
import rife.bld.dependencies.*;
import rife.bld.publish.PublishInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Published artifacts to a Maven repository.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.7
 */
public class PublishOperation extends AbstractOperation<PublishOperation> {
    private Repository repository_;
    private final DependencyScopes dependencies_ = new DependencyScopes();
    private final PublishInfo info_ = new PublishInfo();
    private final List<File> artifacts_ = new ArrayList<>();

    /**
     * Performs the publish operation.
     *
     * @since 1.5.7
     */
    public void execute() {
    }

    /**
     * Configures a publish operation from a {@link Project}.
     *
     * @param project the project to configure the publish operation from
     * @since 1.5.7
     */
    public PublishOperation fromProject(Project project) {
        dependencies().include(project.dependencies());
        artifacts(List.of(new File(project.buildDistDirectory(), project.jarFileName())));
        info()
            .groupId(project.groupId())
            .artifactId(project.artifactId())
            .version(project.version());
        return this;
    }

    /**
     * Provides the repository to publish to
     *
     * @param repository the repository that the artifacts will be published to
     * @return this operation instance
     * @since 1.5.7
     */
    public PublishOperation repository(Repository repository) {
        repository_ = repository;
        return this;
    }

    /**
     * Provides scoped dependencies to reference in the publication.
     *
     * @param dependencies the dependencies that will be references in the publication
     * @return this operation instance
     * @since 1.5.7
     */
    public PublishOperation dependencies(DependencyScopes dependencies) {
        dependencies_.include(dependencies);
        return this;
    }

    /**
     * Provides the artifacts that will be published.
     *
     * @param artifacts the artifacts to publish
     * @return this operation instance
     * @since 1.5.7
     */
    public PublishOperation artifacts(List<File> artifacts) {
        artifacts_.addAll(artifacts);
        return this;
    }

    /**
     * Retrieves the repository that will be published to.
     *
     * @return the publishing repository
     * @since 1.5.7
     */
    public Repository repository() {
        return repository_;
    }

    /**
     * Retrieves the scoped dependencies to reference in the publication.
     * <p>
     * This is a modifiable structure that can be retrieved and changed.
     *
     * @return the scoped dependencies
     * @since 1.5.7
     */
    public DependencyScopes dependencies() {
        return dependencies_;
    }

    /**
     * Retrieves the publication info structure.
     * <p>
     * This is a modifiable structure that can be retrieved and changed.
     *
     * @return the publication info
     * @since 1.5.7
     */
    public PublishInfo info() {
        return info_;
    }

    /**
     * Retrieves the list of artifacts that will be published.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the list of artifacts to publish
     * @since 1.5.7
     */
    public List<File> artifacts() {
        return artifacts_;
    }
}
