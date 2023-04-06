/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.exceptions;

import rife.authentication.exceptions.RememberManagerException;

import java.io.Serial;

public class GetRememberedUserIdErrorException extends RememberManagerException {
    @Serial private static final long serialVersionUID = -8795043125113898699L;

    private final String rememberId_;

    public GetRememberedUserIdErrorException(String rememberId) {
        this(rememberId, null);
    }

    public GetRememberedUserIdErrorException(String rememberId, Throwable cause) {
        super("Unable to get the user id for remember id '" + rememberId + "'.", cause);
        rememberId_ = rememberId;
    }

    public String getRememberId() {
        return rememberId_;
    }
}
