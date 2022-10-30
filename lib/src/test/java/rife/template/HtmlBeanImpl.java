/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.validation.Validation;

public class HtmlBeanImpl extends Validation {
    private boolean wantsupdates_ = false;
    private String[] colors_ = null;
    private String firstname_ = null;
    private String lastname_ = null;

    public HtmlBeanImpl() {
    }

    public void setWantsupdates(boolean wantsupdates) {
        wantsupdates_ = wantsupdates;
    }

    public boolean getWantsupdates() {
        return wantsupdates_;
    }

    public void setColors(String[] colors) {
        colors_ = colors;
    }

    public String[] getColors() {
        return colors_;
    }

    public void setFirstname(String firstname) {
        firstname_ = firstname;
    }

    public String getFirstname() {
        return firstname_;
    }

    public void setLastname(String lastname) {
        lastname_ = lastname;
    }

    public String getLastname() {
        return lastname_;
    }
}

