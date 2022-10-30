/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.lang.reflect.Array;

import static rife.validation.ValidityChecks.checkUrl;

public class ValidationRuleUrl extends PropertyValidationRule {
    public ValidationRuleUrl(String propertyName) {
        super(propertyName);
    }

    public boolean validate() {
        Object value;
        try {
            value = BeanUtils.getPropertyValue(getBean(), getPropertyName());
        } catch (BeanUtilsException e) {
            // an error occurred when obtaining the value of the property
            // just consider it valid to skip over it
            return true;
        }

        if (null == value) {
            return true;
        }

        ConstrainedProperty constrained_property = ConstrainedUtils.getConstrainedProperty(getBean(), getPropertyName());
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (!checkUrl(BeanUtils.formatPropertyValue(Array.get(value, i), constrained_property))) {
                    return false;
                }
            }

            return true;
        } else {
            return checkUrl(BeanUtils.formatPropertyValue(value, constrained_property));
        }
    }

    public ValidationError getError() {
        return new ValidationError.INVALID(getSubject());
    }
}
