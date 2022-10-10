/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import static rife.validation.ValidityChecks.checkInList;

import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

public class ValidationRuleInList extends PropertyValidationRule {
    private final String[] list_;

    public ValidationRuleInList(String propertyName, String[] list) {
        super(propertyName);

        list_ = list;
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

        return checkInList(value, list_);
    }

    public ValidationError getError() {
        return new ValidationError.INVALID(getSubject());
    }
}
