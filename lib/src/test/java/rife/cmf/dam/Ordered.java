/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

public class Ordered extends Validation {
    private int id_ = -1;
    private String name_ = null;
    private int priority_ = -1;

    public Ordered() {
        priority_ = 0;
    }

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

    public void setPriority(int priority) {
        priority_ = priority;
    }

    public int getPriority() {
        return priority_;
    }

    public Ordered priority(int priority) {
        priority_ = priority;
        return this;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public Ordered name(String name) {
        name_ = name;
        return this;
    }
}
