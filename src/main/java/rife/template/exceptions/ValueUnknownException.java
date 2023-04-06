/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class ValueUnknownException extends ProcessingException {
    @Serial private static final long serialVersionUID = -236961439795474690L;

    private String id_ = null;

    public ValueUnknownException(String id) {
        super("The template doesn't contain a value with id " + (null == id ? "null" : "'" + id + "'") + ".");
        id_ = id;
    }

    public String getId() {
        return id_;
    }
}
