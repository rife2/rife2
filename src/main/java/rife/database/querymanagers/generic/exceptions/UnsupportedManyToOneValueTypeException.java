/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.exceptions;

import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class UnsupportedManyToOneValueTypeException extends DatabaseException {
    @Serial private static final long serialVersionUID = 3431422753980243059L;

    private final Class beanClass_;
    private final String propertyName_;
    private final Object value_;

    public UnsupportedManyToOneValueTypeException(Class beanClass, String propertyName, Object value) {
        super("The bean '" + beanClass.getName() + "' declares a many-to-one relationship on property '" + propertyName + "', however the property's value type '" + value.getClass().getName() + "' is not supported. Only classes that implement the interfaces java.util.Collection can be used.");

        beanClass_ = beanClass;
        propertyName_ = propertyName;
        value_ = value;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getPropertyName() {
        return propertyName_;
    }

    public Object getValue() {
        return value_;
    }
}
