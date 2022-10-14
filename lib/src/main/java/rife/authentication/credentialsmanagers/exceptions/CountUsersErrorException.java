/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class CountUsersErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -7282912807960339831L;

    public CountUsersErrorException() {
        this(null);
    }

    public CountUsersErrorException(DatabaseException cause) {
        super("Error while counting the users.", cause);
    }
}
