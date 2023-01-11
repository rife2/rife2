/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.cmf.MimeType;
import rife.forms.FormBuilder;
import rife.forms.FormBuilderXml;
import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.util.Map;

public class BeanHandlerXml extends AbstractBeanHandler {
    private final FormBuilder formBuilder_ = new FormBuilderXml();

    BeanHandlerXml() {
    }

    public static BeanHandlerXml instance() {
        return BeanHandlerXmlSingleton.INSTANCE;
    }

    public MimeType getMimeType() {
        return MimeType.TEXT_XML;
    }

    public FormBuilder getFormBuilder() {
        return formBuilder_;
    }

    protected Map<String, Object> getPropertyValues(Template template, Object bean, String prefix)
    throws BeanUtilsException {
        return BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, bean, null, null, prefix);
    }
}

