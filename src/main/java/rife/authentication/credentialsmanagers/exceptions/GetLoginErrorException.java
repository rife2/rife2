/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class GetLoginErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -3880034096005166200L;

    private final long userId_;

    public GetLoginErrorException(DatabaseException cause, long userId) {
        super("Error while obtaining the login of user with id '" + userId + "'.", cause);
        userId_ = userId;
    }

    public long getUserId() {
        return userId_;
    }
}
