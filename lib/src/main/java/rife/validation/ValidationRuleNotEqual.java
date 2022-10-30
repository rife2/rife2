/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import static rife.validation.ValidityChecks.checkNotEqual;

public class ValidationRuleNotEqual extends PropertyValidationRule {
    private final Object reference_;

    public ValidationRuleNotEqual(String propertyName, Object reference) {
        super(propertyName);

        reference_ = reference;
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

        return checkNotEqual(value, reference_);
    }

    public ValidationError getError() {
        return new ValidationError.INVALID(getSubject());
    }
}
