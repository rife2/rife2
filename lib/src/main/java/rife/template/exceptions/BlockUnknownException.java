/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class BlockUnknownException extends ProcessingException {
    @Serial private static final long serialVersionUID = -4086947245991150379L;

    private String id_ = null;

    public BlockUnknownException(String id) {
        super("The template doesn't contain a block with id " + (null == id ? "null" : "'" + id + "'") + ".");
        id_ = id;
    }

    public String getId() {
        return id_;
    }
}
