/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.cmf.MimeType;
import rife.validation.ConstrainedProperty;

public class ContentImageAutoRetrRep extends ContentImage {
    private int id_ = -1;
    private String name_ = null;
    private byte[] image_ = null;

    @Override
    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("name")
            .maxLength(64)
            .notNull(true)
            .notEmpty(true));
        addConstraint(new ConstrainedProperty("image")
            .notNull(true)
            .mimeType(MimeType.IMAGE_PNG)
            .autoRetrieved(true)
            .repository("testrep"));
    }

    @Override
    public void setId(int id) {
        id_ = id;
    }

    @Override
    public int getId() {
        return id_;
    }

    @Override
    public void setName(String name) {
        name_ = name;
    }

    @Override
    public String getName() {
        return name_;
    }

    @Override
    public ContentImageAutoRetrRep name(String name) {
        name_ = name;
        return this;
    }

    @Override
    public byte[] getImage() {
        return image_;
    }

    @Override
    public void setImage(byte[] image) {
        image_ = image;
    }

    @Override
    public ContentImageAutoRetrRep image(byte[] image) {
        image_ = image;
        return this;
    }
}
