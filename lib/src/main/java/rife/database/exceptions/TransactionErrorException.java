/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class TransactionErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = -5022112556565975376L;

    private Datasource datasource_;

    TransactionErrorException(String action, Datasource datasource, Throwable cause) {
        super(action, cause);
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }
}
