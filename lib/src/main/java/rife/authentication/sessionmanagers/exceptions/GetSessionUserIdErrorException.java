/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class GetSessionUserIdErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -4781581912583842776L;

    private final String authId_;

    public GetSessionUserIdErrorException(String authId) {
        this(authId, null);
    }

    public GetSessionUserIdErrorException(String authId, DatabaseException cause) {
        super("Unable to get the session user id with authid '" + authId + "'.", cause);
        authId_ = authId;
    }

    public String getAuthId() {
        return authId_;
    }
}
