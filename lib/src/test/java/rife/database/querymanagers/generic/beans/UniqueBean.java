/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

public class UniqueBean extends Validation {
    private int identifier_ = -1;
    private String testString_ = null;
    private String anotherString_ = null;
    private String thirdString_ = null;

    public UniqueBean() {
    }

    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("identifier").identifier(true));
        addGroup("group1")
            .addConstraint(new ConstrainedProperty("testString").unique(true).maxLength(20).notNull(true))
            .addConstraint(new ConstrainedProperty("anotherString").maxLength(20).notNull(true));
        addGroup("group2")
            .addConstraint(new ConstrainedProperty("thirdString").maxLength(20));

        addConstraint(new rife.validation.ConstrainedBean().unique("anotherString", "thirdString"));
    }

    public void setIdentifier(int identifier) {
        identifier_ = identifier;
    }

    public int getIdentifier() {
        return identifier_;
    }

    public void setTestString(String testString) {
        this.testString_ = testString;
    }

    public String getTestString() {
        return testString_;
    }

    public void setAnotherString(String anotherString) {
        anotherString_ = anotherString;
    }

    public String getAnotherString() {
        return anotherString_;
    }

    public void setThirdString(String thirdString) {
        thirdString_ = thirdString;
    }

    public String getThirdString() {
        return thirdString_;
    }
}

