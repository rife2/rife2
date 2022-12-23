/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

public class ChildBean extends ParentBean {
    private String childString_ = null;

    public void setChildString(String childString) {
        childString_ = childString;
    }

    public String getChildString() {
        return childString_;
    }
}

