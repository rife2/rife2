/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.forms.FormBuilder;
import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.util.Map;

public class BeanHandlerPlain extends AbstractBeanHandler {
    BeanHandlerPlain() {
    }

    public static BeanHandlerPlain instance() {
        return BeanHandlerPlainSingleton.INSTANCE;
    }

    // TODO : cmf
//	public MimeType getMimeType()
//	{
//		return MimeType.TEXT_PLAIN;
//	}

    public FormBuilder getFormBuilder() {
        return null;
    }

    protected Map<String, Object> getPropertyValues(Template template, Object bean, String prefix)
    throws BeanUtilsException {
        return BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, bean, template.getAvailableValueIds(), null, prefix);
    }
}

