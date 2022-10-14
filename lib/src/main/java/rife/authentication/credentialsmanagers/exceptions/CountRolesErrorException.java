/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class CountRolesErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -8457306602256031431L;

    public CountRolesErrorException() {
        this(null);
    }

    public CountRolesErrorException(DatabaseException cause) {
        super("Error while counting the roles.", cause);
    }
}
