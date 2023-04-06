/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class BeanException extends DbQueryException {
    @Serial private static final long serialVersionUID = 7745938017589820114L;

    private final Class bean_;

    public BeanException(String message, Class bean) {
        super(message);
        bean_ = bean;
    }

    public BeanException(String message, Class bean, Throwable cause) {
        super(message, cause);
        bean_ = bean;
    }

    public Class getBean() {
        return bean_;
    }
}
