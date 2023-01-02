/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc.exceptions;

import java.io.Serial;

public class IncompatiblePropertyValueTypeException extends PropertyValueException {
    @Serial private static final long serialVersionUID = 6336950082309925343L;

    private final String propertyName_;
    private final Class expectedType_;
    private final Class actualType_;

    public IncompatiblePropertyValueTypeException(String propertyName, Class expectedType, Class actualType, Throwable e) {
        super("The property '" + propertyName + "' was expected to have the type '" + expectedType.getName() + "', however it's actual type '" + actualType.getName() + "' couldn't be cast to it.", e);

        propertyName_ = propertyName;
        expectedType_ = expectedType;
        actualType_ = actualType;
    }

    public String getPropertyName() {
        return propertyName_;
    }

    public Class getExpectedType() {
        return expectedType_;
    }

    public Class getActualType() {
        return actualType_;
    }
}
