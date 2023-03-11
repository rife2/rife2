/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies.exceptions;

import rife.bld.dependencies.Dependency;

import java.io.Serial;

public class ArtifactRetrievalErrorException extends DependencyException {
    @Serial private static final long serialVersionUID = 339863133681418524L;

    private final Dependency dependency_;
    private final String url_;

    public ArtifactRetrievalErrorException(Dependency dependency, String url, Throwable e) {
        super("Unexpected error while retrieving artifact for dependency '" + dependency + "' from '" + url + "'", e);

        dependency_ = dependency;
        url_ = url;
    }

    public Dependency getDependency() {
        return dependency_;
    }

    public String getUrl() {
        return url_;
    }
}