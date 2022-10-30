/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.exceptions;

import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class IncompatibleValidationTypeException extends DatabaseException {
    @Serial private static final long serialVersionUID = -3877438782725574316L;

    private final Class incompatibleType_;
    private final Class expectedType_;

    public IncompatibleValidationTypeException(Class incompatibleType, Class expectedType) {
        super("Trying to validate a bean of class '" + incompatibleType.getName() + "' while this GenericQueryManager only supports beans of the '" + expectedType.getName() + "' class.");

        incompatibleType_ = incompatibleType;
        expectedType_ = expectedType;
    }

    public Class getExpectedType() {
        return expectedType_;
    }

    public Class getIncompatibleType() {
        return incompatibleType_;
    }
}
