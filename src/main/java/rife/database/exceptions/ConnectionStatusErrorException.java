/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class ConnectionStatusErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = -6733548295573208721L;

    private final Datasource datasource_;

    public ConnectionStatusErrorException(Datasource datasource, Throwable cause) {
        super("Error while checking the status of the connection with url '" + datasource.getUrl() + "'.", cause);
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }
}
