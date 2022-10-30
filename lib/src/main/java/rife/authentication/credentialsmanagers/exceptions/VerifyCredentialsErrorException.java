/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.Credentials;
import rife.authentication.exceptions.CredentialsManagerException;

import java.io.Serial;

public class VerifyCredentialsErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -8816867018874678942L;

    private final Credentials credentials_;

    public VerifyCredentialsErrorException(Credentials credentials) {
        this(credentials, null);
    }

    public VerifyCredentialsErrorException(Credentials credentials, Throwable cause) {
        super("Error while verifying the credentials.", cause);
        credentials_ = credentials;
    }

    public Credentials getCredentials() {
        return credentials_;
    }
}
