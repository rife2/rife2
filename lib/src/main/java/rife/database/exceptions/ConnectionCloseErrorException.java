/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class ConnectionCloseErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = 8592248767491353911L;

    private final Datasource datasource_;

    public ConnectionCloseErrorException(Datasource datasource, Throwable cause) {
        super("Couldn't properly close the connection with url '" + datasource.getUrl() + "'.", cause);
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }
}
