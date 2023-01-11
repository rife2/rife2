/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import static rife.validation.ValidityChecks.checkNotEmpty;

public class ValidationRuleNotEmpty extends PropertyValidationRule {
    public ValidationRuleNotEmpty(String propertyName) {
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

        return checkNotEmpty(value);
    }

    public ValidationError getError() {
        return new ValidationError.MANDATORY(getSubject());
    }
}
