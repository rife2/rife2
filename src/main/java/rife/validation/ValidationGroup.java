/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import rife.validation.exceptions.ValidationException;
import rife.tools.BeanUtils;
import rife.tools.ExceptionUtils;
import rife.tools.exceptions.BeanUtilsException;

public class ValidationGroup implements Cloneable {
    private final String name_;

    private Validated validation_;
    private ArrayList<String> subjects_;
    private ValidationGroup parent_ = null;
    private ArrayList<String> propertyNames_ = null;

    ValidationGroup(String name, Validation validation) {
        name_ = name;
        validation_ = validation;
        subjects_ = new ArrayList<>();
    }

    void setParent(ValidationGroup parent) {
        parent_ = parent;
    }

    void setValidation(Validated validation) {
        validation_ = validation;
    }

    public void reinitializeProperties(Object bean)
    throws ValidationException {
        if (null == bean ||
            null == propertyNames_ ||
            0 == propertyNames_.size()) {
            return;
        }

        Object new_bean;
        try {
            new_bean = bean.getClass().getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            throw new ValidationException(e);
        }

        var property_names = new String[propertyNames_.size()];
        propertyNames_.toArray(property_names);
        try {
            for (var name : BeanUtils.getPropertyNames(bean.getClass(), property_names, null, null)) {
                BeanUtils.setPropertyValue(bean, name, BeanUtils.getPropertyValue(new_bean, name));
            }
        } catch (BeanUtilsException e) {
            throw new ValidationException(e);
        }
    }

    public String getName() {
        return name_;
    }

    public List<String> getPropertyNames() {
        return propertyNames_;
    }

    public List<String> getSubjects() {
        return subjects_;
    }

    public Validated getValidation() {
        return validation_;
    }

    public ValidationGroup addSubject(String subject) {
        addPropertyName(subject);

        if (subjects_.contains(subject)) {
            return this;
        }

        subjects_.add(subject);

        if (parent_ != null) {
            parent_.addSubject(subject);
        }

        return this;
    }

    private void addPropertyName(String name) {
        if (null == propertyNames_) {
            propertyNames_ = new ArrayList<>();
        }

        if (!propertyNames_.contains(name)) {
            propertyNames_.add(name);
        }
    }

    public ValidationGroup addRule(ValidationRule rule) {
        validation_.addRule(rule);
        addSubject(rule.getSubject());

        return this;
    }

    public ValidationGroup addConstraint(ConstrainedProperty constrainedProperty) {
        addPropertyName(constrainedProperty.getPropertyName());

        var rules = validation_.addConstrainedPropertyRules(constrainedProperty);
        for (ValidationRule rule : rules) {
            addSubject(rule.getSubject());
        }

        return this;
    }

    public ValidationGroup addGroup(String name) {
        var group = validation_.addGroup(name);
        group.setParent(this);
        return group;
    }

    public ValidationGroup clone() {
        ValidationGroup new_validationgroup = null;
        try {
            new_validationgroup = (ValidationGroup) super.clone();

            if (subjects_ != null) {
                new_validationgroup.subjects_ = new ArrayList<>(subjects_);
            }
            if (propertyNames_ != null) {
                new_validationgroup.propertyNames_ = new ArrayList<>(propertyNames_);
            }
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.validation").severe(ExceptionUtils.getExceptionStackTrace(e));
        }

        return new_validationgroup;
    }
}
