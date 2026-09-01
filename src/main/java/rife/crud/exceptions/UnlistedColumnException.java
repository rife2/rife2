/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class UnlistedColumnException extends CrudException {
    @Serial private static final long serialVersionUID = -1741607485295637282L;

    private final Class beanClass_;
    private final String property_;

    public UnlistedColumnException(Class beanClass, String property) {
        super("The browse table of bean '" + beanClass.getName() + "' doesn't show the property '" + property + "', so there's no column of it to render.");

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
