/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators.exceptions;

import rife.authentication.exceptions.SessionValidatorException;

import java.io.Serial;

public class RoleCheckErrorException extends SessionValidatorException {
    @Serial private static final long serialVersionUID = -849240411412778497L;

    private final String authId_;
    private final String hostIp_;
    private final String role_;

    public RoleCheckErrorException(String authId, String hostIp, String role) {
        this(authId, hostIp, role, null);
    }

    public RoleCheckErrorException(String authId, String hostIp, String role, Throwable cause) {
        super("Unable to check the role '" + role + "' for the session with authid '" + authId + "' and hostip '" + hostIp + "'.", cause);
        authId_ = authId;
        hostIp_ = hostIp;
        role_ = role;
    }

    public String getAuthId() {
        return authId_;
    }

    public String getHostIp() {
        return hostIp_;
    }

    public String getRole() {
        return role_;
    }
}
