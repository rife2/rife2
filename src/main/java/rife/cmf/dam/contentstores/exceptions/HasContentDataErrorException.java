/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class HasContentDataErrorException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 3463539434963036313L;

    private final int id_;

    public HasContentDataErrorException(int id, DatabaseException cause) {
        super("Unexpected error the presence of content with the id '" + id + "'.", cause);

        id_ = id;
    }

    public int getId() {
        return id_;
    }
}
