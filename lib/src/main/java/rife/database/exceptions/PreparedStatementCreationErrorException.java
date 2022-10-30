/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class PreparedStatementCreationErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = 527710892636948049L;

    private final Datasource datasource_;

    public PreparedStatementCreationErrorException(Datasource datasource, Throwable cause) {
        super("Couldn't create a new prepared statement.", cause);
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }
}
