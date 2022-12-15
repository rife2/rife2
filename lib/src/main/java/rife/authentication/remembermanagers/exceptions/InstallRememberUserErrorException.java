/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.exceptions;

import rife.authentication.exceptions.RememberManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class InstallRememberUserErrorException extends RememberManagerException {
    @Serial private static final long serialVersionUID = 4507208218418987745L;

    public InstallRememberUserErrorException() {
        this(null);
    }

    public InstallRememberUserErrorException(DatabaseException cause) {
        super("Can't install the remember user database structure.", cause);
    }
}
