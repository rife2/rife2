/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.exceptions;

import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class MissingDefaultConstructorException extends DatabaseException {
    @Serial private static final long serialVersionUID = 5950649556111250164L;

    private final Class beanClass_;

    public MissingDefaultConstructorException(Class beanClass, Throwable cause) {
        super("The bean '" + beanClass.getName() + " has no default constructor. This constructor is required to make is usable by the generic query manager.", cause);
        beanClass_ = beanClass;
    }

    public Class getBeanClass() {
        return beanClass_;
    }
}
