/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.exceptions;

import rife.authentication.exceptions.SessionManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class IsSessionValidErrorException extends SessionManagerException {
    @Serial private static final long serialVersionUID = -4255610298209171384L;

    private final String authId_;
    private final String hostIp_;

    public IsSessionValidErrorException(String authId, String hostIp) {
        this(authId, hostIp, null);
    }

    public IsSessionValidErrorException(String authId, String hostIp, DatabaseException cause) {
        super("Unable to check the validity of the session with authid '" + authId + "' for hostip '" + hostIp + "'.", cause);
        authId_ = authId;
        hostIp_ = hostIp;
    }

    public String getAuthId() {
        return authId_;
    }

    public String getHostIp() {
        return hostIp_;
    }
}
