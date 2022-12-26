/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class RemoveContentStoreErrorException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 5377418013372733953L;

    public RemoveContentStoreErrorException(DatabaseException cause) {
        super("Can't remove the content store database structure.", cause);
    }
}
