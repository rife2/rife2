/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class InstanceCreationException extends CrudException {
    @Serial private static final long serialVersionUID = 5147765093023700690L;

    private final Class beanClass_;

    public InstanceCreationException(Class beanClass, Throwable cause) {
        super("A new instance of bean '" + beanClass.getName() + "' couldn't be created.", cause);

        beanClass_ = beanClass;
    }

    public Class getBeanClass() {
        return beanClass_;
    }
}
