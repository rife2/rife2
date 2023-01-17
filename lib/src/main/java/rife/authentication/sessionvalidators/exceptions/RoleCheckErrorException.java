/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators.exceptions;

import rife.authentication.exceptions.SessionValidatorException;

import java.io.Serial;

public class RoleCheckErrorException extends SessionValidatorException {
    @Serial private static final long serialVersionUID = -849240411412778497L;

    private final String authId_;
    private final String authData_;
    private final String role_;

    public RoleCheckErrorException(String authId, String authData, String role) {
        this(authId, authData, role, null);
    }

    public RoleCheckErrorException(String authId, String authData, String role, Throwable cause) {
        super("Unable to check the role '" + role + "' for the session with authid '" + authId + "' and auth data '" + authData + "'.", cause);
        authId_ = authId;
        authData_ = authData;
        role_ = role;
    }

    public String getAuthId() {
        return authId_;
    }

    public String getAuthData() {
        return authData_;
    }

    public String getRole() {
        return role_;
    }
}
