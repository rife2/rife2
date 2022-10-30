/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.exceptions;

import rife.authentication.exceptions.RememberManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class EraseAllRememberIdsErrorException extends RememberManagerException {
    @Serial private static final long serialVersionUID = -8055510005187772158L;

    public EraseAllRememberIdsErrorException() {
        this(null);
    }

    public EraseAllRememberIdsErrorException(DatabaseException cause) {
        super("Unable to erase all the remember ids.", cause);
    }
}
