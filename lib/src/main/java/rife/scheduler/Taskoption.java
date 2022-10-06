/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

public class Taskoption /*extends Validation*/ implements Cloneable {
    private int mTaskId = -1;
    private String mName = null;
    private String mValue = null;

    public Taskoption() {
    }

    // TODO
//	protected void activateValidation()
//	{
//		addRule(new ValidationRuleRange("taskId", new Integer(0), null));
//		addRule(new ValidationRuleNotNull("name"));
//		addRule(new ValidationRuleNotNull("value"));
//	}

    public void setTaskId(int taskid) {
        mTaskId = taskid;
    }

    public int getTaskId() {
        return mTaskId;
    }

    public void setName(String name) {
        if (null == name && 0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    public Taskoption clone()
    throws CloneNotSupportedException {
        return (Taskoption) super.clone();
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

        Taskoption other_taskoption = (Taskoption) object;

        return other_taskoption.getTaskId() == getTaskId() &&
            other_taskoption.getName().equals(getName()) &&
            other_taskoption.getValue().equals(getValue());
    }
}
