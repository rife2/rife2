/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class RemoveCredentialsErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 8496417459907476229L;

    public RemoveCredentialsErrorException() {
        this(null);
    }

    public RemoveCredentialsErrorException(DatabaseException cause) {
        super("Can't remove the credentials database structure.", cause);
    }
}
