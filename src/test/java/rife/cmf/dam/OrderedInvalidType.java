/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

public class OrderedInvalidType extends Validation {
    private int id_ = -1;
    private String name_ = null;
    private String priority_ = null;

    @Override
    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("name").maxLength(64).notNull(true).notEmpty(true));
        addConstraint(new ConstrainedProperty("priority").rangeBegin(0).ordinal(true));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setPriority(String priority) {
        priority_ = priority;
    }

    public String getPriority() {
        return priority_;
    }

    public OrderedInvalidType priority(String priority) {
        priority_ = priority;
        return this;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public OrderedInvalidType name(String name) {
        name_ = name;
        return this;
    }
}
