/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class UnknownPropertyException extends CrudException {
    @Serial private static final long serialVersionUID = 4492066065358783292L;

    private final Class beanClass_;
    private final String property_;
    private final String role_;

    public UnknownPropertyException(Class beanClass, String property, String role) {
        super("The " + role + " of bean '" + beanClass.getName() + "' is the property '" + property + "', which the bean doesn't have.");

        beanClass_ = beanClass;
        property_ = property;
        role_ = role;
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
}
