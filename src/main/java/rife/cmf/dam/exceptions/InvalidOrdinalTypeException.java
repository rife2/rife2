/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class InvalidOrdinalTypeException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 931403852026897190L;

    private final Class beanClass_;
    private final String property_;

    public InvalidOrdinalTypeException(Class beanClass, String property) {
        super("The property '" + property + "' of bean '" + beanClass.getName() + "' declares itself as being an ordinal, but it is not an integer.");

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
