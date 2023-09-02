/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.cmf.MimeType;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import java.io.InputStream;

public class ContentRaw extends Validation {
    private int id_ = -1;
    private String name_ = null;
    private InputStream raw_ = null;

    @Override
    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("name")
            .maxLength(64)
            .notNull(true)
            .notEmpty(true));
        addConstraint(new ConstrainedProperty("raw")
            .notNull(true)
            .mimeType(MimeType.RAW));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public ContentRaw name(String name) {
        name_ = name;
        return this;
    }

    public InputStream getRaw() {
        return raw_;
    }

    public void setRaw(InputStream image) {
        raw_ = image;
    }

    public ContentRaw raw(InputStream raw) {
        raw_ = raw;
        return this;
    }
}
