/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.util.Collection;

public class MMFirstBean extends MetaData {
    private Integer identifier_;
    private Collection<MMSecondBean> secondBeans_;
    private String firstString_;

    public MMFirstBean() {
    }

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("identifier").identifier(true));
        addConstraint(new ConstrainedProperty("secondBeans").manyToMany());
    }

    public void setIdentifier(Integer identifier) {
        identifier_ = identifier;
    }

    public Integer getIdentifier() {
        return identifier_;
    }

    public void setSecondBeans(Collection<MMSecondBean> secondBeans) {
        secondBeans_ = secondBeans;
    }

    public Collection<MMSecondBean> getSecondBeans() {
        return secondBeans_;
    }

    public void setFirstString(String firstString) {
        firstString_ = firstString;
    }

    public String getFirstString() {
        return firstString_;
    }
}

