/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class InstallContentStoreErrorException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 6607203778338095289L;

    public InstallContentStoreErrorException(DatabaseException cause) {
        super("Can't install the content store database structure.", cause);
    }
}
