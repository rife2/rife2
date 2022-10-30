/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class BeanRemovalErrorException extends TemplateException {
    @Serial private static final long serialVersionUID = -9178572416144354823L;

    private final Object bean_;

    public BeanRemovalErrorException(Object bean, Throwable cause) {
        super("Unexpected error while trying to remove the values and properties of bean '" + bean + "'" + (bean == null ? "." : " with class '" + bean.getClass().getName() + "'."), cause);

        bean_ = bean;
    }

    public Object getBean() {
        return bean_;
    }
}
