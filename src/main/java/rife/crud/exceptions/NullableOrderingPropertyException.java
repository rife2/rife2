/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class NullableOrderingPropertyException extends CrudException {
    @Serial private static final long serialVersionUID = 1533592454207789046L;

    private final Class beanClass_;
    private final String property_;
    private final String role_;

    public NullableOrderingPropertyException(Class beanClass, String property, String role) {
        super("The " + role + " of bean '" + beanClass.getName() + "' is the property '" + property + "', which can be null, constrain it as notNull so that every instance can be ordered.");

        beanClass_ = beanClass;
        property_ = property;
        role_ = role;
    }

    public String getRole() {
        return role_;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getProperty() {
        return property_;
    }
}
