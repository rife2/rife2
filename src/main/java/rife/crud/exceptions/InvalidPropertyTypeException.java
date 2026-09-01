/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class InvalidPropertyTypeException extends CrudException {
    @Serial private static final long serialVersionUID = 4212186219150287747L;

    private final Class beanClass_;
    private final String property_;
    private final String role_;
    private final Class type_;

    public InvalidPropertyTypeException(Class beanClass, String property, String role, Class type, String expected) {
        super("The " + role + " of bean '" + beanClass.getName() + "' is the property '" + property +
              "', which is a '" + type.getName() + "' while it has to be " + expected + ".");

        beanClass_ = beanClass;
        property_ = property;
        role_ = role;
        type_ = type;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getProperty() {
        return property_;
    }

    public String getRole() {
        return role_;
    }

    public Class getType() {
        return type_;
    }
}
