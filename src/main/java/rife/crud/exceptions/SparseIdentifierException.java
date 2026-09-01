/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class SparseIdentifierException extends CrudException {
    @Serial private static final long serialVersionUID = 5979300191593683678L;

    private final Class beanClass_;
    private final String property_;

    public SparseIdentifierException(Class beanClass, String property) {
        super("The identifier property '" + property + "' of bean '" + beanClass.getName() + "' is sparse, which means that it has to be provided, while this administration assigns it.");

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
