/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.exceptions;

import rife.authentication.exceptions.RememberManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class EraseUserRememberIdsErrorException extends RememberManagerException {
    @Serial private static final long serialVersionUID = -759372674181172750L;

    public EraseUserRememberIdsErrorException(long userId) {
        this(userId, null);
    }

    public EraseUserRememberIdsErrorException(long userId, DatabaseException cause) {
        super("Unable to erase all the remember ids for user id " + userId + ".", cause);
    }
}
