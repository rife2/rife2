/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class MissingManyToOneColumnException extends DatabaseException {
    @Serial private static final long serialVersionUID = 1390166791727531269L;

    private final Class constrainedClass_;
    private final String propertyName_;

    public MissingManyToOneColumnException(Class constrainedClass, String propertyName) {
        super("The property '" + propertyName + "' of '" + constrainedClass.getName() + "' has a manyToOne constraint, however the column of the associated table is missing. This can be provided when the constraint is declared.");

        constrainedClass_ = constrainedClass;
        propertyName_ = propertyName;
    }

    public Class getConstrainedClass() {
        return constrainedClass_;
    }

    public String getPropertyName() {
        return propertyName_;
    }
}
