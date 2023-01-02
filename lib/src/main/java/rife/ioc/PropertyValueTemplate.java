/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import rife.ioc.exceptions.PropertyValueException;
import rife.ioc.exceptions.TemplateFactoryUnknownException;
import rife.template.Template;
import rife.template.TemplateFactory;

/**
 * Retrieves a property value as template instance of a particular type.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class PropertyValueTemplate implements PropertyValue {
    private final String type_;
    private final String name_;

    /**
     * The constructor that stores the retrieval parameters.
     * The template type will be set to "html"
     *
     * @param name the template name
     * @since 1.0
     */
    public PropertyValueTemplate(String name) {
        this(null, name);
    }

    /**
     * The constructor that stores the retrieval parameters.
     *
     * @param type the template factory type; if this argument is <code>null</code>
     *             the template type will be "html"
     * @param name the template name
     * @since 1.0
     */
    public PropertyValueTemplate(String type, String name) {
        if (null == type) {
            type = "html";
        }
        type_ = type;
        name_ = name;
    }

    public Template getValue()
    throws PropertyValueException {
        TemplateFactory factory = TemplateFactory.getFactory(type_);
        if (null == factory) {
            throw new TemplateFactoryUnknownException(type_);
        }
        return factory.get(name_);
    }

    public String getValueString()
    throws PropertyValueException {
        return getValue().getContent();
    }

    public String toString() {
        return getValueString();
    }

    public boolean isNegligible() {
        return false;
    }

    public boolean isStatic() {
        return false;
    }
}
