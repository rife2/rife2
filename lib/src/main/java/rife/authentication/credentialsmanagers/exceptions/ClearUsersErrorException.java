/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class ClearUsersErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 1471057731695379528L;

    public ClearUsersErrorException() {
        this(null);
    }

    public ClearUsersErrorException(DatabaseException cause) {
        super("Error while clearing all the users.", cause);
    }
}
