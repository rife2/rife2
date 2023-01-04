/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class StartSessionErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = 1599170266402021852L;

    private final long userId_;
    private final String authData_;

    public StartSessionErrorException(long userId, String authData) {
        this(userId, authData, null);
    }

    public StartSessionErrorException(long userId, String authData, DatabaseException cause) {
        super("Unable to start session for userid '" + userId + "' and auth data '" + authData + "'.", cause);
        userId_ = userId;
        authData_ = authData;
    }

    public long getUserId() {
        return userId_;
    }

    public String getAuthData() {
        return authData_;
    }
}
