/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.exceptions;

import rife.authentication.exceptions.RememberManagerException;

import java.io.Serial;

public class CreateRememberIdErrorException extends RememberManagerException {
    @Serial private static final long serialVersionUID = 5174821054624717542L;

    private final long userId_;

    public CreateRememberIdErrorException(long userId) {
        this(userId, null);
    }

    public CreateRememberIdErrorException(long userId, Throwable cause) {
        super("Unable to create a remember id for userid '" + userId + "'.", cause);

        userId_ = userId;
    }

    public long getUserId() {
        return userId_;
    }
}
