/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.lang.reflect.Array;

import static rife.validation.ValidityChecks.checkRegexp;

public class ValidationRuleRegexp extends PropertyValidationRule {
    private final String regexp_;

    public ValidationRuleRegexp(String propertyName, String regexp) {
        super(propertyName);

        regexp_ = regexp;
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
                if (!checkRegexp(BeanUtils.formatPropertyValue(Array.get(value, i), constrained_property), regexp_)) {
                    return false;
                }
            }

            return true;
        } else {
            return checkRegexp(BeanUtils.formatPropertyValue(value, constrained_property), regexp_);
        }
    }

    public ValidationError getError() {
        return new ValidationError.INVALID(getSubject());
    }
}
