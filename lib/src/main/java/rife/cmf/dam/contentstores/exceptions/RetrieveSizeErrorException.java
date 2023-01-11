/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class RetrieveSizeErrorException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -5567272822506458691L;

    private final int id_;

    public RetrieveSizeErrorException(int id, DatabaseException cause) {
        super("Unexpected error while retrieving the size of the content with the id '" + id + "'.", cause);

        id_ = id;
    }

    public int getId() {
        return id_;
    }
}
