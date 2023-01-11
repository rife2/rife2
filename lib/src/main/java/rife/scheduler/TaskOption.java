/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.validation.Validation;
import rife.validation.ValidationRuleNotNull;
import rife.validation.ValidationRuleRange;

public class TaskOption extends Validation implements Cloneable {
    private int taskId_ = -1;
    private String name_ = null;
    private String value_ = null;

    public TaskOption() {
    }

    protected void activateValidation() {
        addRule(new ValidationRuleRange("taskId", 0, null));
        addRule(new ValidationRuleNotNull("name"));
        addRule(new ValidationRuleNotNull("value"));
    }

    public void setTaskId(int taskId) {
        taskId_ = taskId;
    }

    public TaskOption taskId(int taskId) {
        setTaskId(taskId);
        return this;
    }

    public int getTaskId() {
        return taskId_;
    }

    public void setName(String name) {
        if (null == name && 0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        name_ = name;
    }

    public TaskOption name(String name) {
        setName(name);
        return this;
    }

    public String getName() {
        return name_;
    }

    public void setValue(String value) {
        value_ = value;
    }

    public String getValue() {
        return value_;
    }

    public TaskOption value(String value) {
        setValue(value);
        return this;
    }

    public TaskOption clone()
    throws CloneNotSupportedException {
        return (TaskOption) super.clone();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }

        TaskOption other_taskoption = (TaskOption) object;

        return other_taskoption.getTaskId() == getTaskId() &&
            other_taskoption.getName().equals(getName()) &&
            other_taskoption.getValue().equals(getValue());
    }
}
