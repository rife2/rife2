/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class DuplicateConstraintException extends CrudException {
    @Serial private static final long serialVersionUID = 5408131837688444020L;

    private final Class beanClass_;
    private final String role_;
    private final String property_;
    private final String otherProperty_;

    public DuplicateConstraintException(Class beanClass, String role, String property, String otherProperty) {
        super("Both the property '" + property + "' and the property '" + otherProperty + "' of bean '" +
              beanClass.getName() + "' are the " + role + ", constrain only one of them as such.");

        beanClass_ = beanClass;
        role_ = role;
        property_ = property;
        otherProperty_ = otherProperty;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getRole() {
        return role_;
    }

    public String getProperty() {
        return property_;
    }

    public String getOtherProperty() {
        return otherProperty_;
    }
}
