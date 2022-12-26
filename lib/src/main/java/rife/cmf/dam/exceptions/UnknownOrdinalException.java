/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class UnknownOrdinalException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -960376462623043020L;

    private final Class beanClass_;
    private final String property_;

    public UnknownOrdinalException(Class beanClass, String property) {
        super("The property '" + property + "' of bean '" + beanClass.getName() + "' can't be used as an ordinal since it can't be found.");

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
