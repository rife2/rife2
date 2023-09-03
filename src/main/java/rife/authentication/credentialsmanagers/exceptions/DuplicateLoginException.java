/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;

import java.io.Serial;

public class DuplicateLoginException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -5868340001604117437L;

    private final String login_;

    public DuplicateLoginException(String login) {
        this(login, null);
    }

    public DuplicateLoginException(String login, Throwable e) {
        super("The login '" + login + "' is already present.", e);

        login_ = login;
    }

    public String getLogin() {
        return login_;
    }
}
