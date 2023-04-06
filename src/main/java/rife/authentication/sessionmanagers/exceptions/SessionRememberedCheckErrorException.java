/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class SessionRememberedCheckErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -9064926639559046361L;

    private final String authId_;

    public SessionRememberedCheckErrorException(String authId) {
        this(authId, null);
    }

    public SessionRememberedCheckErrorException(String authId, DatabaseException cause) {
        super("Unable to check whether the session with authid '" + authId + "' was created from remembered data.", cause);
        authId_ = authId;
    }

    public String getAuthId() {
        return authId_;
    }
}
