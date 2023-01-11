/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

public class NoDefaultConstructorBean {
    private int id_ = -1;
    private int linkBean_ = -1;
    private String testString_ = null;

    public NoDefaultConstructorBean(int id) {
        id_ = id;
    }

    public void setLinkBean(int linkBean) {
        linkBean_ = linkBean;
    }

    public int getLinkBean() {
        return linkBean_;
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

    public String toString() {
        return id_ + ";" + linkBean_ + ";" + testString_;
    }
}

