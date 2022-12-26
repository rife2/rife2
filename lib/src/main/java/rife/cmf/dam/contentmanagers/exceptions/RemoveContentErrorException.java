/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class RemoveContentErrorException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -2762295250254019078L;

    public RemoveContentErrorException(DatabaseException cause) {
        super("Can't remove the content database structure.", cause);
    }
}
