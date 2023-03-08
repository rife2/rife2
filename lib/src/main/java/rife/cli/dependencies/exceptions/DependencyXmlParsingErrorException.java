/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.dependencies.exceptions;

import rife.cli.dependencies.Dependency;
import rife.tools.StringUtils;

import java.io.Serial;
import java.util.List;
import java.util.Set;

public class DependencyXmlParsingErrorException extends DependencyException {
    @Serial private static final long serialVersionUID = 6036121359540018004L;

    private final Dependency dependency_;
    private final String uri_;
    private final Set<String> errors_;

    public DependencyXmlParsingErrorException(Dependency dependency, String uri, Set<String> errors) {
        super("Unable to parse artifact document for dependency '" + dependency + "' from '" + uri + "' :\n" + StringUtils.join(errors, "\n"));

        dependency_ = dependency;
        uri_ = uri;
        errors_ = errors;
    }

    public Dependency getDependency() {
        return dependency_;
    }

    public String getUrl() {
        return uri_;
    }

    public Set<String> getErrors() {
        return errors_;
    }
}