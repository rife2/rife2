/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class ListRolesErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 5960821338157810627L;

    public ListRolesErrorException(DatabaseException cause) {
        super("Error while listing all the roles.", cause);
    }
}
