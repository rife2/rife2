/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class ExpectedOrdinalConstraintException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 7487136951099088915L;

    private final Class beanClass_;
    private final String property_;

    public ExpectedOrdinalConstraintException(Class beanClass, String property) {
        super("The constrained property '" + property + "' of bean '" + beanClass.getName() + "' should have been constrained by an 'ordinal' constraint, but it wasn't.");

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
