/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class RemoveSessionsErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -5835213695054118894L;

    public RemoveSessionsErrorException() {
        this(null);
    }

    public RemoveSessionsErrorException(DatabaseException cause) {
        super("Can't remove the session database structure.", cause);
    }
}
