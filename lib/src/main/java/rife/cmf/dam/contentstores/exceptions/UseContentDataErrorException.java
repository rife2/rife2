/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;

import java.io.Serial;

public class UseContentDataErrorException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 5436823294436558781L;

    private final int id_;

    public UseContentDataErrorException(int id, Throwable cause) {
        super("Unexpected error while retrieving content with the id '" + id + "'.", cause);

        id_ = id;
    }

    public int getId() {
        return id_;
    }
}
