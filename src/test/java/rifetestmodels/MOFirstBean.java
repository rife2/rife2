/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class MOFirstBean extends MetaData {
    private Integer identifier_;
    private MOSecondBean secondBean_ = null;
    private MOSecondBean secondBean2_ = null;
    private MOThirdBean thirdBean_ = null;
    private String firstString_ = null;

    public MOFirstBean() {
    }

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("identifier").identifier(true));
        addConstraint(new ConstrainedProperty("secondBean").manyToOne());
        addConstraint(new ConstrainedProperty("secondBean2").manyToOne(MOSecondBean.class, "identifier"));
        addConstraint(new ConstrainedProperty("thirdBean").manyToOne(MOThirdBean.class));
    }

    public void setIdentifier(Integer identifier) {
        identifier_ = identifier;
    }

    public Integer getIdentifier() {
        return identifier_;
    }

    public void setSecondBean(MOSecondBean secondBean) {
        secondBean_ = secondBean;
    }

    public MOSecondBean getSecondBean() {
        return secondBean_;
    }

    public void setSecondBean2(MOSecondBean secondBean) {
        secondBean2_ = secondBean;
    }

    public MOSecondBean getSecondBean2() {
        return secondBean2_;
    }

    public void setThirdBean(MOThirdBean thirdBean) {
        thirdBean_ = thirdBean;
    }

    public MOThirdBean getThirdBean() {
        return thirdBean_;
    }

    public void setFirstString(String firstString) {
        firstString_ = firstString;
    }

    public String getFirstString() {
        return firstString_;
    }

    public static String getStaticFirstString() {
        return "test";
    }
}
