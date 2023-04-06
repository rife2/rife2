/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

public class ParentBean {
    protected int id_ = -1;
    protected String parentString_ = null;

    public void setParentString(String parentString) {
        parentString_ = parentString;
    }

    public String getParentString() {
        return parentString_;
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }
}

