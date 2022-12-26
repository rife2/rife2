/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class WrongConstrainedPropertyVariantException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 7752599853025679192L;

    private final Class beanClass_;
    private final String property_;

    public WrongConstrainedPropertyVariantException(Class beanClass, String property) {
        super("The constrained property '" + property + "' of bean '" + beanClass.getName() + "' is not an instance of CmfProperty.");

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
