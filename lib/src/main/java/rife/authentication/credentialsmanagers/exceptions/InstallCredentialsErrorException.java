/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class InstallCredentialsErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 5098527539817113840L;

    public InstallCredentialsErrorException() {
        this(null);
    }

    public InstallCredentialsErrorException(DatabaseException cause) {
        super("Can't install the credentials database structure.", cause);
    }
}
