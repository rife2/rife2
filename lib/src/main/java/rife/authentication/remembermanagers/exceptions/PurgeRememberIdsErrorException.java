/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.exceptions;

import rife.authentication.exceptions.RememberManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class PurgeRememberIdsErrorException extends RememberManagerException {
    @Serial private static final long serialVersionUID = -309361946618471471L;

    public PurgeRememberIdsErrorException() {
        this(null);
    }

    public PurgeRememberIdsErrorException(DatabaseException cause) {
        super("Unable to purge the expired remember IDs.", cause);
    }
}
