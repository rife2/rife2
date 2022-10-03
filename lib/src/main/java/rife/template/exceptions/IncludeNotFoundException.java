/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import rife.datastructures.DocumentPosition;

public class IncludeNotFoundException extends ProcessingException {
    private static final long serialVersionUID = -8132650494489046526L;

    private final String templateName_ ;
    private final DocumentPosition errorLocation_;
    private final String included_;

    public IncludeNotFoundException(String templateName, DocumentPosition errorLocation, String included) {
        super(formatError(templateName, errorLocation, "couldn't find the included template '" + included + "'"));

        templateName_ = templateName;
        errorLocation_ = errorLocation;
        included_ = included;
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
}
