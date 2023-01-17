/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class ContinueSessionErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -3671791851196693420L;

    private final String authId_;

    public ContinueSessionErrorException(String authId) {
        this(authId, null);
    }

    public ContinueSessionErrorException(String authId, DatabaseException cause) {
        super("Unable to continue the session with authid '" + authId + "'.", cause);
        authId_ = authId;
    }

    public String getAuthId() {
        return authId_;
    }
}
