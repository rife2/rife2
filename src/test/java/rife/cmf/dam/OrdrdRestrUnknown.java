/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

public class OrdrdRestrUnknown extends Validation {
    private int id_ = -1;
    private String name_ = null;
    private int priority_ = -1;
    private int restricted_ = -1;

    public OrdrdRestrUnknown() {
        priority_ = 0;
    }

    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("name").maxLength(64).notNull(true).notEmpty(true));
        addConstraint(new ConstrainedProperty("restricted").rangeBegin(0));
        addConstraint(new ConstrainedProperty("priority").rangeBegin(0).ordinal(true, "restrictedunknown"));
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

    public OrdrdRestrUnknown priority(int priority) {
        priority_ = priority;
        return this;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public OrdrdRestrUnknown name(String name) {
        name_ = name;
        return this;
    }

    public int getRestricted() {
        return restricted_;
    }

    public void setRestricted(int restricted) {
        restricted_ = restricted;
    }

    public OrdrdRestrUnknown restricted(int restricted) {
        setRestricted(restricted);
        return this;
    }
}
