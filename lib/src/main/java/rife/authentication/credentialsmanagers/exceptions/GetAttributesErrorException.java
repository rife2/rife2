/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class GetAttributesErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 8779077667203772941L;

    private final String login_;

    public GetAttributesErrorException(String login) {
        this(login, null);
    }

    public GetAttributesErrorException(String login, DatabaseException cause) {
        super("Error while obtaining the attributes of the user with login '" + login + "'.", cause);
        login_ = login;
    }

    public String getLogin() {
        return login_;
    }
}
