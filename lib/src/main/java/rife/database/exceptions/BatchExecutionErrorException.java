/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class BatchExecutionErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = 7946011449481688333L;

    private final Datasource datasource_;

    public BatchExecutionErrorException(Datasource datasource, Throwable cause) {
        super("Error while executing the batch sql commands.", cause);
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }
}
