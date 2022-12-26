/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;

import java.io.Serial;

public class StoreContentDataErrorException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 2037221789702552879L;

    private final int id_;

    public StoreContentDataErrorException(int id, Throwable cause) {
        super("Unexpected error while storing the content with the id '" + id + "'.", cause);

        id_ = id;
    }

    public int getId() {
        return id_;
    }
}
