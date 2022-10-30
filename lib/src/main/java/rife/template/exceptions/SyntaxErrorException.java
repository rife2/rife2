/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import rife.datastructures.DocumentPosition;

import java.io.Serial;

public class SyntaxErrorException extends TemplateException {
    @Serial private static final long serialVersionUID = 7687067640946628293L;

    private final String templateName_;
    private final DocumentPosition errorLocation_;

    public SyntaxErrorException(String templateName, DocumentPosition errorLocation, String message, Throwable cause) {
        super(formatError(templateName, errorLocation, message), cause);

        templateName_ = templateName;
        errorLocation_ = errorLocation;
    }

    public String getTemplateName() {
        return templateName_;
    }

    public DocumentPosition getErrorLocation() {
        return errorLocation_;
    }
}
