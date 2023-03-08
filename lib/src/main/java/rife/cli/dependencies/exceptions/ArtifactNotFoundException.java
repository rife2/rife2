/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.dependencies.exceptions;

import rife.cli.dependencies.Dependency;

import java.io.Serial;

public class ArtifactNotFoundException extends DependencyException {
    @Serial private static final long serialVersionUID = -4463592998915863162L;

    private final Dependency dependency_;
    private final String uri_;
    private final int statusCode_;

    public ArtifactNotFoundException(Dependency dependency, String uri, int statusCode) {
        super("Couldn't find artifact for dependency '" + dependency + "' at '" + uri + "' (status code: " + statusCode + ")");

        dependency_ = dependency;
        uri_ = uri;
        statusCode_ = statusCode;
    }

    public Dependency getDependency() {
        return dependency_;
    }

    public String getUrl() {
        return uri_;
    }

    public int getStatusCode() {
        return statusCode_;
    }
}