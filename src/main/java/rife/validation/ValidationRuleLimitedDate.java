/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.lang.reflect.Array;
import java.util.Date;

import static rife.validation.ValidityChecks.checkLimitedDate;

public class ValidationRuleLimitedDate extends PropertyValidationRule {
    private final Date min_;
    private final Date max_;

    public ValidationRuleLimitedDate(String propertyName, Date min, Date max) {
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

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (!checkLimitedDate(Array.get(value, i), min_, max_)) {
                    return false;
                }
            }

            return true;
        } else {
            return checkLimitedDate(value, min_, max_);
        }
    }

    public ValidationError getError() {
        return new ValidationError.INVALID(getSubject());
    }
}
