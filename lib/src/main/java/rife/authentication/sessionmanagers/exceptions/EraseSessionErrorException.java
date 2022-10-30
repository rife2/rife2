/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class EraseSessionErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = 7163686412279212060L;

    private final String authId_;

    public EraseSessionErrorException(String authId) {
        this(authId, null);
    }

    public EraseSessionErrorException(String authId, DatabaseException cause) {
        super("Unable to erase the session with authid '" + authId + "'.", cause);
        authId_ = authId;
    }

    public String getAuthId() {
        return authId_;
    }
}
