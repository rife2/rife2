/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class UnknownOrdinalRestrictionException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -1656164424552067374L;

    private final Class beanClass_;
    private final String property_;
    private final String restriction_;

    public UnknownOrdinalRestrictionException(Class beanClass, String property, String restriction) {
        super("The property '" + property + "' of bean '" + beanClass.getName() + "' declares itself as being a restricted ordinal, but the restriction property '" + restriction + "' can't be found.");

        beanClass_ = beanClass;
        property_ = property;
        restriction_ = restriction;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getProperty() {
        return property_;
    }

    public String getRestriction() {
        return restriction_;
    }
}
