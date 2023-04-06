/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class InstallContentErrorException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -1241554250147142868L;

    public InstallContentErrorException(DatabaseException cause) {
        super("Can't install the content database structure.", cause);
    }
}
