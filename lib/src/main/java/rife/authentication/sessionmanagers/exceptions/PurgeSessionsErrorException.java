/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class PurgeSessionsErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -4809957308355490972L;

    public PurgeSessionsErrorException() {
        this(null);
    }

    public PurgeSessionsErrorException(DatabaseException cause) {
        super("Unable to purge the expired sessions.", cause);
    }
}
