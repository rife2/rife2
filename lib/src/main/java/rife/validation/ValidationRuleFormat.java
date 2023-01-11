/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import static rife.validation.ValidityChecks.checkFormat;

import java.lang.reflect.Array;
import java.text.Format;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

public class ValidationRuleFormat extends PropertyValidationRule {
    private final Format format_;

    public ValidationRuleFormat(String propertyName, Format format) {
        super(propertyName);

        format_ = format;
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

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (!checkFormat(Array.get(value, i), format_)) {
                    return false;
                }
            }

            return true;
        } else {
            return checkFormat(value, format_);
        }
    }

    public ValidationError getError() {
        return new ValidationError.INVALID(getSubject());
    }
}
