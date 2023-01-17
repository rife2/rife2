/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class InvalidOrdinalRestrictionTypeException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -4099426985586611966L;

    private final Class beanClass_;
    private final String property_;
    private final String restriction_;
    private final Class type_;

    public InvalidOrdinalRestrictionTypeException(Class beanClass, String property, String restriction, Class type) {
        super("The property '" + property + "' of bean '" + beanClass.getName() + "' declares itself as being a restricted ordinal, but the restriction property '" + restriction + "' with type " + type.getName() + " is not a Number.");

        beanClass_ = beanClass;
        property_ = property;
        restriction_ = restriction;
        type_ = type;
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

    public Class getType() {
        return type_;
    }
}
