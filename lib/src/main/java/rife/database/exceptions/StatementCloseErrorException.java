/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class StatementCloseErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = -4874100206556310884L;

    private final Datasource datasource_;

    public StatementCloseErrorException(Datasource datasource, Throwable cause) {
        super("Couldn't close the statement.", cause);
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }
}
