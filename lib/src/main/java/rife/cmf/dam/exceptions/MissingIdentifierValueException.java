/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class MissingIdentifierValueException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -1157200832197263833L;

    private final Class beanClass_;
    private final String identifierName_;

    public MissingIdentifierValueException(Class beanClass, String identifierName) {
        super("The instance of bean '" + beanClass.getName() + "' should have value for the identifier '" + identifierName + "'.");

        beanClass_ = beanClass;
        identifierName_ = identifierName;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getIdentifierName() {
        return identifierName_;
    }
}
