/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class InstallSessionsErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -2782754934511672351L;

    public InstallSessionsErrorException() {
        this(null);
    }

    public InstallSessionsErrorException(DatabaseException cause) {
        super("Can't install the session database structure.", cause);
    }
}
