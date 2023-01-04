/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class IsSessionValidErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -4255610298209171384L;

    private final String authId_;
    private final String authData_;

    public IsSessionValidErrorException(String authId, String authData) {
        this(authId, authData, null);
    }

    public IsSessionValidErrorException(String authId, String authData, DatabaseException cause) {
        super("Unable to check the validity of the session with auth ID '" + authId + "' for auth data '" + authData + "'.", cause);
        authId_ = authId;
        authData_ = authData;
    }

    public String getAuthId() {
        return authId_;
    }

    public String getAuthData() {
        return authData_;
    }
}
