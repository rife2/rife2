/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators.exceptions;

import rife.authentication.exceptions.SessionValidatorException;

import java.io.Serial;

public class SessionValidityCheckErrorException extends SessionValidatorException {
    @Serial private static final long serialVersionUID = 4277837804430634653L;

    private final String authId_;
    private final String authData_;

    public SessionValidityCheckErrorException(String authId, String authData) {
        this(authId, authData, null);
    }

    public SessionValidityCheckErrorException(String authId, String authData, Throwable cause) {
        super("Unable to check the validity of the session with authid '" + authId + "' for auth data '" + authData + "'.", cause);
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
