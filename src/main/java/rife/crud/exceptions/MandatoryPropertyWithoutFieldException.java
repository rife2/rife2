/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class MandatoryPropertyWithoutFieldException extends CrudException {
    @Serial private static final long serialVersionUID = 8785245626820170748L;

    private final Class beanClass_;
    private final String property_;

    public MandatoryPropertyWithoutFieldException(Class beanClass, String property) {
        super("The property '" + property + "' of bean '" + beanClass.getName() + "' has to be provided, while this administration can't generate a field for it yet, give it a value of its own when an instance is created.");

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
