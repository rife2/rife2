/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class GetUserIdErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -7151563685570831238L;

    private final String login_;

    public GetUserIdErrorException(DatabaseException cause, String login) {
        super("Error while obtaining the user id of user with login '" + login + "'.", cause);
        login_ = login;
    }

    public String getLogin() {
        return login_;
    }
}
