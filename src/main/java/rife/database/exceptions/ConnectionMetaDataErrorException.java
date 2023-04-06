/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class ConnectionMetaDataErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = 8314476636892309174L;

    private final Datasource datasource_;

    public ConnectionMetaDataErrorException(Datasource datasource, Throwable cause) {
        super("Error while obtaining the metadata of the connection with url '" + datasource.getUrl() + "'.", cause);
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }
}
