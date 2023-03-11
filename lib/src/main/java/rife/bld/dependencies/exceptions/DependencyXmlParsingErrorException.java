/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies.exceptions;

import rife.bld.dependencies.Dependency;
import rife.tools.StringUtils;

import java.io.Serial;
import java.util.Set;

public class DependencyXmlParsingErrorException extends DependencyException {
    @Serial private static final long serialVersionUID = 6036121359540018004L;

    private final Dependency dependency_;
    private final String url_;
    private final Set<String> errors_;

    public DependencyXmlParsingErrorException(Dependency dependency, String url, Set<String> errors) {
        super("Unable to parse artifact document for dependency '" + dependency + "' from '" + url + "' :\n" + StringUtils.join(errors, "\n"));

        dependency_ = dependency;
        url_ = url;
        errors_ = errors;
    }

    public Dependency getDependency() {
        return dependency_;
    }

    public String getUrl() {
        return url_;
    }

    public Set<String> getErrors() {
        return errors_;
    }
}