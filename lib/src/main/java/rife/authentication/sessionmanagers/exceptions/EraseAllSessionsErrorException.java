/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class EraseAllSessionsErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = 5589334271121278190L;

    public EraseAllSessionsErrorException() {
        this(null);
    }

    public EraseAllSessionsErrorException(DatabaseException cause) {
        super("Unable to erase all the sessions.", cause);
    }
}
