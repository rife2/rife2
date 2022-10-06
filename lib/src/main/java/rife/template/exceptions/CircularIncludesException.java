/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import rife.datastructures.DocumentPosition;
import rife.tools.StringUtils;

import java.io.Serial;
import java.util.Stack;

public class CircularIncludesException extends ProcessingException {
    @Serial private static final long serialVersionUID = 3472405981484449892L;

    private final String templateName_;
    private final DocumentPosition errorLocation_;
    private final String included_;
    private final Stack<String> previousIncludes_;

    public CircularIncludesException(String templateName, DocumentPosition errorLocation, String included, Stack<String> previousIncludes) {
        super(formatError(templateName, errorLocation, "the template '" + included + "' has already been included, the include stack was : '" + StringUtils.join(previousIncludes, ", ") + "'"));

        templateName_ = templateName;
        errorLocation_ = errorLocation;
        included_ = included;
        previousIncludes_ = previousIncludes;
    }

    public String getTemplateName() {
        return templateName_;
    }

    public DocumentPosition getErrorLocation() {
        return errorLocation_;
    }

    public String getIncluded() {
        return included_;
    }

    public Stack<String> getPreviousIncludes() {
        return previousIncludes_;
    }
}
