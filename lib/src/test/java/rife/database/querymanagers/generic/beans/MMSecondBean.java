/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.util.Collection;

public class MMSecondBean extends MetaData {
    private Integer identifier_;
    private Collection<MMFirstBean> firstBeans_;
    private String secondString_;

    public MMSecondBean() {
    }

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("identifier").identifier(true));
        addConstraint(new ConstrainedProperty("firstBeans").manyToManyAssociation());
    }

    public void setIdentifier(Integer identifier) {
        identifier_ = identifier;
    }

    public Integer getIdentifier() {
        return identifier_;
    }

    public void setFirstBeans(Collection<MMFirstBean> firstBeans) {
        firstBeans_ = firstBeans;
    }

    public Collection<MMFirstBean> getFirstBeans() {
        return firstBeans_;
    }

    public void setSecondString(String secondString) {
        secondString_ = secondString;
    }

    public String getSecondString() {
        return secondString_;
    }
}

