/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.exceptions;

import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class IncompatibleManyToOneValueTypeException extends DatabaseException {
    @Serial private static final long serialVersionUID = -7028083340881018568L;

    private final Class beanClass_;
    private final String propertyName_;
    private final Class propertyType_;
    private final Class associatedType_;

    public IncompatibleManyToOneValueTypeException(Class beanClass, String propertyName, Class propertyType, Class associatedType) {
        super("The bean '" + beanClass.getName() + "' declares a many-to-one relationship on property '" + propertyName + "', however the property's type '" + propertyType.getName() + "' is not assignable from the associated class '" + associatedType.getName() + "' that has been declared through constraints.");

        beanClass_ = beanClass;
        propertyName_ = propertyName;
        propertyType_ = propertyType;
        associatedType_ = associatedType;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getPropertyName() {
        return propertyName_;
    }

    public Class getPropertyType() {
        return propertyType_;
    }

    public Class getAssociatedType() {
        return associatedType_;
    }
}
