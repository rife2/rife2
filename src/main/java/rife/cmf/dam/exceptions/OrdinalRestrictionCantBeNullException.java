/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class OrdinalRestrictionCantBeNullException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -7956803702851520790L;

    private final Class beanClass_;
    private final String property_;
    private final String restriction_;

    public OrdinalRestrictionCantBeNullException(Class beanClass, String property, String restriction) {
        super("The property '" + property + "' of bean '" + beanClass.getName() + "' declares itself as being a restricted ordinal, but the value restriction property '" + restriction + "' is null.");

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
