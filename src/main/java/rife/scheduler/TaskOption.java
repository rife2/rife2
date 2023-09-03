/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.validation.Validation;
import rife.validation.ValidationRuleNotNull;
import rife.validation.ValidationRuleRange;

/**
 * A task option provides a way to configure an existing task.
 * <p>
 * For a task option to be valid, a task ID needs to be associated and
 * both the task option name and value have to be set.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class TaskOption extends Validation implements Cloneable {
    private int taskId_ = -1;
    private String name_ = null;
    private String value_ = null;

    /**
     * Create a new task option instance that is not associated with any task.
     *
     * @since 1.0
     */
    public TaskOption() {
    }

    protected void activateValidation() {
        addRule(new ValidationRuleRange("taskId", 0, null));
        addRule(new ValidationRuleNotNull("name"));
        addRule(new ValidationRuleNotNull("value"));
    }

    /**
     * Sets the task ID that this task option is associated with.
     *
     * @param taskId the associated task ID
     * @since 1.0
     */
    public void setTaskId(int taskId) {
        taskId_ = taskId;
    }

    /**
     * Sets the task ID that this task option is associated with.
     *
     * @param taskId the associated task ID
     * @return this task option instance
     * @since 1.0
     */
    public TaskOption taskId(int taskId) {
        setTaskId(taskId);
        return this;
    }

    /**
     * Retrieves the task ID this task option is associated with.
     *
     * @return the associated task ID; or
     * {@code -1} when no task has been associated
     */
    public int getTaskId() {
        return taskId_;
    }

    /**
     * Sets the name of this task option.
     *
     * @param name this task option's name
     * @since 1.0
     */
    public void setName(String name) {
        if (null == name || name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");

        name_ = name;
    }

    /**
     * Sets the name of this task option.
     *
     * @param name this task option's name
     * @return this task option instance
     * @since 1.0
     */
    public TaskOption name(String name) {
        setName(name);
        return this;
    }

    /**
     * Retrieves the name of this task option.
     *
     * @return this task option's name; or
     * {@code null} if the name hasn't been set yet
     */
    public String getName() {
        return name_;
    }

    /**
     * Sets the value of this task option.
     *
     * @param value this task option's value
     * @since 1.0
     */
    public void setValue(String value) {
        value_ = value;
    }

    /**
     * Sets the value of this task option.
     *
     * @param value this task option's value
     * @return this task option instance
     * @since 1.0
     */
    public TaskOption value(String value) {
        setValue(value);
        return this;
    }

    /**
     * Retrieves the value of this task option.
     *
     * @return this task option's value; or
     * {@code null} if the value hasn't been set yet
     * @since 1.0
     */
    public String getValue() {
        return value_;
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
