/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies.exceptions;

import rife.bld.dependencies.Dependency;

import java.io.File;
import java.io.Serial;

public class DependencyDownloadException extends DependencyException {
    @Serial private static final long serialVersionUID = -1606130902378889150L;

    private final Dependency dependency_;
    private final String url_;
    private final File destination_;

    public DependencyDownloadException(Dependency dependency, String url, File destination, Throwable e) {
        super("Unable to download dependency '" + dependency + "' from '" + url + "' into '" + destination + "'", e);

        dependency_ = dependency;
        url_ = url;
        destination_ = destination;
    }

    public Dependency getDependency() {
        return dependency_;
    }

    public String getUrl() {
        return url_;
    }

    public File getDestination() {
        return destination_;
    }
}