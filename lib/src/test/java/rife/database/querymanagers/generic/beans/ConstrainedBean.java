/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class ConstrainedBean extends MetaData {
    private int identifier_ = -1;
    private Integer linkBean_ = null;
    private String testString_ = null;

    public ConstrainedBean() {
    }

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("identifier").identifier(true));
        addConstraint(new ConstrainedProperty("linkBean").manyToOne(LinkBean.class, "id"));
    }

    public void setIdentifier(int identifier) {
        identifier_ = identifier;
    }

    public int getIdentifier() {
        return identifier_;
    }

    public void setLinkBean(Integer linkBean) {
        linkBean_ = linkBean;
    }

    public Integer getLinkBean() {
        return linkBean_;
    }

    public void setTestString(String testString) {
        this.testString_ = testString;
    }

    public String getTestString() {
        return testString_;
    }
}

