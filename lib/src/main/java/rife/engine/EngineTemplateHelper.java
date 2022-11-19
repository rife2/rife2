/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;
import rife.forms.FormBuilder;
import rife.template.BeanHandler;
import rife.template.Template;
import rife.tools.exceptions.BeanUtilsException;

import java.util.Collection;
import java.util.Collections;

class EngineTemplateHelper {
    static Collection<String> selectParameter(Template template, String name, String[] values) {
        assert name != null;
        assert name.length() > 0;

        BeanHandler bean_handler = template.getBeanHandler();
        if (null == bean_handler) {
            return Collections.emptyList();
        }
        FormBuilder form_builder = bean_handler.getFormBuilder();
        if (null == form_builder) {
            return Collections.emptyList();
        }

        return form_builder.selectParameter(template, name, values);
    }

    static void generateForm(Template template, Object beanInstance, String prefix)
    throws EngineException {
        BeanHandler bean_handler = template.getBeanHandler();
        if (null == bean_handler) {
            return;
        }
        FormBuilder form_builder = bean_handler.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanInstance.getClass(), prefix);
            form_builder.generateForm(template, beanInstance, null, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    static void generateEmptyForm(Template template, Class beanClass, String prefix)
    throws EngineException {
        BeanHandler bean_handler = template.getBeanHandler();
        if (null == bean_handler) {
            return;
        }
        FormBuilder form_builder = bean_handler.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanClass, prefix);
            form_builder.generateForm(template, beanClass, null, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    static void removeForm(Template template, Class beanClass, String prefix)
    throws EngineException {
        BeanHandler bean_handler = template.getBeanHandler();
        if (null == bean_handler) {
            return;
        }
        FormBuilder form_builder = bean_handler.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanClass, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }
}