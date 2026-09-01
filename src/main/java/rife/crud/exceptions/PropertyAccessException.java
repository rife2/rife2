/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class PropertyAccessException extends CrudException {
    @Serial private static final long serialVersionUID = 4403765483503151951L;

    private final Class beanClass_;
    private final String property_;

    public PropertyAccessException(Class beanClass, String property, Throwable cause) {
        super("The property '" + property + "' of bean '" + beanClass.getName() + "' couldn't be accessed.", cause);

        beanClass_ = beanClass;
        property_ = property;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getProperty() {
        return property_;
    }
}
