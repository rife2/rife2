/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import rife.template.Template;

import java.io.Serial;

public class BeanHandlerUnsupportedException extends TemplateException {
    @Serial private static final long serialVersionUID = 7166917351543261088L;

    private Template mTemplate = null;
    private Object mBean = null;

    public BeanHandlerUnsupportedException(Template template, Object bean) {
        super("The template '" + template.getClass().getName() + "' doesn't support the handling of bean values. This was attempted for bean '" + String.valueOf(bean) + "'" + (bean == null ? "." : " with class '" + bean.getClass().getName() + "'."));

        mTemplate = template;
        mBean = bean;
    }

    public Template getTemplate() {
        return mTemplate;
    }

    public Object getBean() {
        return mBean;
    }
}
