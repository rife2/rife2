/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class UnstoredPropertyException extends CrudException {
    @Serial private static final long serialVersionUID = 1060964119413610758L;

    private final Class beanClass_;
    private final String property_;
    private final String role_;

    public UnstoredPropertyException(Class beanClass, String property, String role) {
        super("The " + role + " of bean '" + beanClass.getName() + "' is the property '" + property + "', which isn't stored in the database while a column is needed for it.");

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
