/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators.exceptions;

import rife.authentication.exceptions.SessionValidatorException;

import java.io.Serial;

public class SessionValidityCheckErrorException extends SessionValidatorException {
    @Serial private static final long serialVersionUID = 4277837804430634653L;

    private final String authId_;
    private final String hostIp_;

    public SessionValidityCheckErrorException(String authId, String hostIp) {
        this(authId, hostIp, null);
    }

    public SessionValidityCheckErrorException(String authId, String hostIp, Throwable cause) {
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
