/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.util.Collection;

public class MOSecondBean extends MetaData {
    private Integer identifier_;
    private Collection<MOFirstBean> firstBeans_;
    private String secondString_;

    public MOSecondBean() {
    }

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("identifier").identifier(true));
        addConstraint(new ConstrainedProperty("firstBeans").manyToOneAssociation());
    }

    public void setIdentifier(Integer identifier) {
        identifier_ = identifier;
    }

    public Integer getIdentifier() {
        return identifier_;
    }

    public void setFirstBeans(Collection<MOFirstBean> firstBeans) {
        firstBeans_ = firstBeans;
    }

    public Collection<MOFirstBean> getFirstBeans() {
        return firstBeans_;
    }

    public void setSecondString(String secondString) {
        secondString_ = secondString;
    }

    public String getSecondString() {
        return secondString_;
    }
}

