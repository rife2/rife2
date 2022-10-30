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
    private final String hostIp_;

    public StartSessionErrorException(long userId, String hostIp) {
        this(userId, hostIp, null);
    }

    public StartSessionErrorException(long userId, String hostIp, DatabaseException cause) {
        super("Unable to start session for userid '" + userId + "' and hostip '" + hostIp + "'.", cause);
        userId_ = userId;
        hostIp_ = hostIp;
    }

    public long getUserId() {
        return userId_;
    }

    public String getHostIp() {
        return hostIp_;
    }
}
