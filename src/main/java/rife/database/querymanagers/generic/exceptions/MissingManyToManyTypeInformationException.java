/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.exceptions;

import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class MissingManyToManyTypeInformationException extends DatabaseException {
    @Serial private static final long serialVersionUID = -1852595871902260744L;

    private final Class beanClass_;
    private final String propertyName_;

    public MissingManyToManyTypeInformationException(Class beanClass, String propertyName) {
        super("The bean '" + beanClass.getName() + "' declares a many-to-many relationship on property '" + propertyName + "', however the type of the associated class hasn't been specified. This can either be done during the declaration of the manyToMany constraint or by specifying the property's collection elements through generics.");

        beanClass_ = beanClass;
        propertyName_ = propertyName;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getPropertyName() {
        return propertyName_;
    }
}
