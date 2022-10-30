/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.lang.reflect.Array;

import static rife.validation.ValidityChecks.checkLength;

public class ValidationRuleLimitedLength extends PropertyValidationRule {
    private final int min_;
    private final int max_;

    public ValidationRuleLimitedLength(String propertyName, int min, int max) {
        super(propertyName);

        min_ = min;
        max_ = max;
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
                if (!checkLength(BeanUtils.formatPropertyValue(Array.get(value, i), constrained_property), min_, max_)) {
                    return false;
                }
            }

            return true;
        } else {
            return checkLength(BeanUtils.formatPropertyValue(value, constrained_property), min_, max_);
        }
    }

    public ValidationError getError() {
        return new ValidationError.WRONGLENGTH(getSubject());
    }
}
