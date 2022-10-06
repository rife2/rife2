/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.exceptions;

import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class UnsupportedManyToOneAssociationPropertyTypeException extends DatabaseException {
    @Serial private static final long serialVersionUID = 221066901914357366L;

    private final Class beanClass_;
    private final String propertyName_;
    private final Class type_;

    public UnsupportedManyToOneAssociationPropertyTypeException(Class beanClass, String propertyName, Class type) {
        super("The bean '" + beanClass.getName() + "' declares a many-to-one association relationship on property '" + propertyName + "', however the property's type '" + type.getName() + "' is not supported. Only java.util.Collection, java.util.Set and java.util.List can be used.");

        beanClass_ = beanClass;
        propertyName_ = propertyName;
        type_ = type;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getPropertyName() {
        return propertyName_;
    }

    public Class getType() {
        return type_;
    }
}
