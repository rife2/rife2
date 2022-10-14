/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.exceptions;

import rife.authentication.exceptions.RememberManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class EraseRememberIdErrorException extends RememberManagerException {
    @Serial private static final long serialVersionUID = 300560529136885181L;

    private final String rememberId_;

    public EraseRememberIdErrorException(String rememberId) {
        this(rememberId, null);
    }

    public EraseRememberIdErrorException(String rememberId, DatabaseException cause) {
        super("Unable to erase the remember id '" + rememberId + "'.", cause);
        rememberId_ = rememberId;
    }

    public String getRememberId() {
        return rememberId_;
    }
}
