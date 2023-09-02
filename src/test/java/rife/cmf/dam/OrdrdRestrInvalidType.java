/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

public class OrdrdRestrInvalidType extends Validation {
    private int id_ = -1;
    private String name_ = null;
    private int priority_ = -1;
    private String restricted_ = null;

    public OrdrdRestrInvalidType() {
        priority_ = 0;
    }

    @Override
    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("name").maxLength(64).notNull(true).notEmpty(true));
        addConstraint(new ConstrainedProperty("restricted").rangeBegin(0));
        addConstraint(new ConstrainedProperty("priority").rangeBegin(0).ordinal(true, "restricted"));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setPriority(int priority) {
        priority_ = priority;
    }

    public int getPriority() {
        return priority_;
    }

    public OrdrdRestrInvalidType priority(int priority) {
        priority_ = priority;
        return this;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public OrdrdRestrInvalidType name(String name) {
        name_ = name;
        return this;
    }

    public String getRestricted() {
        return restricted_;
    }

    public void setRestricted(String restricted) {
        restricted_ = restricted;
    }

    public OrdrdRestrInvalidType restricted(String restricted) {
        setRestricted(restricted);
        return this;
    }
}
