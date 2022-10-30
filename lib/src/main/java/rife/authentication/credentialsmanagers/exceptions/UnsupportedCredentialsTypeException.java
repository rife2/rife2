/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.Credentials;
import rife.authentication.exceptions.CredentialsManagerException;

import java.io.Serial;

public class UnsupportedCredentialsTypeException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 6083937124194075522L;

    private final Credentials credentials_;

    public UnsupportedCredentialsTypeException(Credentials credentials) {
        super("The credentials with type '" + credentials.getClass().getName() + "' aren't supported.");
        credentials_ = credentials;
    }

    public Credentials getCredentials() {
        return credentials_;
    }
}
