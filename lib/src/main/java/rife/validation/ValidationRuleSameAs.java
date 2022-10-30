/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import static rife.validation.ValidityChecks.checkEqual;

public class ValidationRuleSameAs extends PropertyValidationRule {
    private final String reference_;

    public ValidationRuleSameAs(String propertyName, String reference) {
        super(propertyName);

        reference_ = reference;
    }

    public boolean validate() {
        Object value;
        Object other;
        try {
            value = BeanUtils.getPropertyValue(getBean(), getPropertyName());
            other = BeanUtils.getPropertyValue(getBean(), reference_);
        } catch (BeanUtilsException e) {
            // an error occurred when obtaining the value of the property
            // just consider it valid to skip over it
            return true;
        }

        return checkEqual(value, other);
    }

    public ValidationError getError() {
        return new ValidationError.NOTSAMEAS(getPropertyName());
    }
}
