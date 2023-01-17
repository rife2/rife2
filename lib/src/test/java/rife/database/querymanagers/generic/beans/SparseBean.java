/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class SparseBean extends MetaData {
    private int id_ = -1;
    private String testString_ = null;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).sparse(true));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setTestString(String testString) {
        this.testString_ = testString;
    }

    public String getTestString() {
        return testString_;
    }
}

