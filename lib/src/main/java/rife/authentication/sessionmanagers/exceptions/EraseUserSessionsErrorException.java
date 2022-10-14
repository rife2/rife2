/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class EraseUserSessionsErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -322527253962142510L;

    public EraseUserSessionsErrorException(long userId) {
        this(userId, null);
    }

    public EraseUserSessionsErrorException(long userId, DatabaseException cause) {
        super("Unable to erase all the sessions for user id " + userId + ".", cause);
    }
}
