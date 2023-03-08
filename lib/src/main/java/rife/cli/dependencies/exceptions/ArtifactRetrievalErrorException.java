/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.dependencies.exceptions;

import rife.cli.dependencies.Dependency;

import java.io.Serial;

public class ArtifactRetrievalErrorException extends DependencyException {
    @Serial private static final long serialVersionUID = 339863133681418524L;

    private final Dependency dependency_;
    private final String uri_;

    public ArtifactRetrievalErrorException(Dependency dependency, String uri, Throwable e) {
        super("Unexpected error while retrieving artifact for dependency '" + dependency + "' from '" + uri + "'", e);

        dependency_ = dependency;
        uri_ = uri;
    }

    public Dependency getDependency() {
        return dependency_;
    }

    public String getUrl() {
        return uri_;
    }
}