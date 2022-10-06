/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class BeanSettingErrorException extends TemplateException {
    @Serial private static final long serialVersionUID = 518347938529572778L;

    private final Object bean_;

    public BeanSettingErrorException(Object bean, Throwable cause) {
        super("Unexpected error while trying to set the values of bean '" + bean + "'" + (bean == null ? "." : " with class '" + bean.getClass().getName() + "'."), cause);

        bean_ = bean;
    }

    public Object getBean() {
        return bean_;
    }
}
