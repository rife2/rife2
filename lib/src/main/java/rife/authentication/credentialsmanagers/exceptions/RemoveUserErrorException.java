/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class RemoveUserErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -272534511065525807L;

    private final String login_;
    private final Long userid_;

    public RemoveUserErrorException(String role) {
        this(role, null);
    }

    public RemoveUserErrorException(String login, DatabaseException cause) {
        super("Error while removing user with login '" + login + "'.", cause);
        login_ = login;
		userid_ = null;
    }

    public RemoveUserErrorException(Long userId, DatabaseException cause) {
        super("Error while removing user with login '" + userId + "'.", cause);
		login_ = null;
        userid_ = userId;
    }

    public String getLogin() {
        return login_;
    }

    public Long getUserId() {
        return userid_;
    }
}
