/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class CountSessionsErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -1711545850000533498L;

    public CountSessionsErrorException() {
        this(null);
    }

    public CountSessionsErrorException(DatabaseException cause) {
        super("Unable to count the sessions.", cause);
    }
}
