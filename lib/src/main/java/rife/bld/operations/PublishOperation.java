/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.Project;
import rife.bld.dependencies.*;
import rife.template.TemplateFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Published artifacts to a Maven repository.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.7
 */
public class PublishOperation extends AbstractOperation<PublishOperation> {
    private Repository repository_ = null;

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
     * Retrieves the repository that will be published to.
     *
     * @return the publishing repository
     * @since 1.5.7
     */
    public Repository repository() {
        return repository_;
    }
}
