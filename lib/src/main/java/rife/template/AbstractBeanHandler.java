/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.cmf.MimeType;
import rife.template.exceptions.BeanRemovalErrorException;
import rife.template.exceptions.BeanSettingErrorException;
import rife.template.exceptions.TemplateException;
import rife.tools.ArrayUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.ConstrainedProperty;
import rife.validation.ConstrainedUtils;

import java.util.Map;

public abstract class AbstractBeanHandler implements BeanHandler {
    protected abstract MimeType getMimeType();

    protected abstract Map<String, Object> getPropertyValues(Template template, Object bean, String prefix)
    throws BeanUtilsException;

    public void setBean(Template template, Object bean, String prefix, boolean encode)
    throws TemplateException {
        if (null == template) throw new IllegalArgumentException("template can't be null.");
        if (null == bean) {
            return;
        }

        var constrained = ConstrainedUtils.makeConstrainedInstance(bean);

        try {
            var property_values = getPropertyValues(template, bean, prefix);
            Object property_value = null;
            String[] property_value_strings = null;
            String[] property_values_encoded = null;
            var encoder = template.getEncoder();

            ConstrainedProperty constrained_property = null;
            for (var property_name : property_values.keySet()) {
                property_value = property_values.get(property_name);
                property_values_encoded = null;

                if (null == property_value) {
                    property_values_encoded = new String[1];
                    property_values_encoded[0] = "";
                } else {
                    // get the constrained property if that's appropriate
                    if (constrained != null) {
                        if (prefix != null) {
                            constrained_property = constrained.getConstrainedProperty(property_name.substring(prefix.length()));
                        } else {
                            constrained_property = constrained.getConstrainedProperty(property_name);
                        }
                    }

                    // handle multiple values
                    property_value_strings = ArrayUtils.createStringArray(property_value, constrained_property);
                    if (property_value_strings != null &&
                        property_value_strings.length > 0) {
                        // encode the value if that's necessary
                        property_values_encoded = new String[property_value_strings.length];
                        for (var i = 0; i < property_values_encoded.length; i++) {
                            if (null == encoder ||
                                !encode ||
                                (constrained_property != null && constrained_property.isDisplayedRaw())) {
                                // still encode if the mime type of the constrained property is different
                                // from the mime type of the bean handler
                                if (encoder != null &&
                                    getMimeType() != null &&
                                    constrained_property != null &&
                                    constrained_property.getMimeType() != null &&
                                    constrained_property.getMimeType() != getMimeType()
                                ) {
                                    property_values_encoded[i] = encoder.encode(property_value_strings[i]);
                                } else {
                                    property_values_encoded[i] = property_value_strings[i];
                                }
                            } else {
                                property_values_encoded[i] = encoder.encode(property_value_strings[i]);
                            }
                        }
                    }
                }

                if (property_values_encoded != null) {
                    // plain values
                    if (template.hasValueId(property_name)) {
                        template.setValue(property_name, property_values_encoded[0]);
                    }

                    if (getFormBuilder() != null) {
                        // handle form values
                        getFormBuilder().selectParameter(template, property_name, property_value_strings);
                    }
                }
            }
        } catch (BeanUtilsException e) {
            throw new BeanSettingErrorException(bean, e);
        }
    }

    public void removeBean(Template template, Object bean, String prefix)
    throws TemplateException {
        if (null == template) throw new IllegalArgumentException("template can't be null.");
        if (null == bean) {
            return;
        }

        try {
            var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
            ConstrainedProperty constrained_property = null;

            var property_values = getPropertyValues(template, bean, prefix);
            Object property_value = null;
            String[] property_value_strings = null;
            for (var property_name : property_values.keySet()) {
                property_value = property_values.get(property_name);

                if (property_name != null) {
                    // plain values
                    if (template.hasValueId(property_name)) {
                        template.removeValue(property_name);
                    }
                }

                if (getFormBuilder() != null) {
                    if (property_value != null) {
                        // get the constrained property if that's appropriate
                        if (constrained != null) {
                            if (prefix != null) {
                                constrained_property = constrained.getConstrainedProperty(property_name.substring(prefix.length()));
                            } else {
                                constrained_property = constrained.getConstrainedProperty(property_name);
                            }
                        }
                        // handle multiple values
                        property_value_strings = ArrayUtils.createStringArray(property_value, constrained_property);
                        if (property_value_strings.length > 0) {
                            // handle form values
                            getFormBuilder().unselectParameter(template, property_name, property_value_strings);
                        }
                    }
                }
            }
        } catch (BeanUtilsException e) {
            throw new BeanRemovalErrorException(bean, e);
        }
    }
}

