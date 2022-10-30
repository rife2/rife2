/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class ContainsUserErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -8188124830768131352L;

    private final String login_;

    public ContainsUserErrorException(String login) {
        this(login, null);
    }

    public ContainsUserErrorException(String login, DatabaseException cause) {
        super("Error while checking if the user with login '" + login + "' is present.", cause);
        login_ = login;
    }

    public String getLogin() {
        return login_;
    }
}
