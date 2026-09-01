/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class BeanIntrospectionException extends CrudException {
    @Serial private static final long serialVersionUID = 5647791953493247425L;

    private final Class beanClass_;

    public BeanIntrospectionException(Class beanClass, Throwable cause) {
        super("The properties of bean '" + beanClass.getName() + "' couldn't be introspected.", cause);

        beanClass_ = beanClass;
    }

    public Class getBeanClass() {
        return beanClass_;
    }
}
