/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies.exceptions;

import rife.bld.dependencies.Dependency;

import java.io.Serial;

public class ArtifactNotFoundException extends DependencyException {
    @Serial private static final long serialVersionUID = -4463592998915863162L;

    private final Dependency dependency_;
    private final String uri_;

    public ArtifactNotFoundException(Dependency dependency, String uri) {
        super("Couldn't find artifact for dependency '" + dependency + "' at '" + uri);

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