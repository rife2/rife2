/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

import java.io.Serial;

public class BeanUtilsException extends Exception {
    @Serial private static final long serialVersionUID = -6557891499066342431L;

    private Class beanClass_ = null;

    public BeanUtilsException(String message, Class beanClass) {
        super(message);
        beanClass_ = beanClass;
    }

    public BeanUtilsException(String message, Class beanClass, Throwable cause) {
        super(message, cause);
        beanClass_ = beanClass;
    }

    public Class getBeanClass() {
        return beanClass_;
    }
}
