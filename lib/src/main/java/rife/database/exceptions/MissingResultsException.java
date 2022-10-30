/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class MissingResultsException extends DatabaseException {
    @Serial private static final long serialVersionUID = 8032678779633066395L;

    private final Datasource datasource_;

    public MissingResultsException(Datasource datasource) {
        super("Trying to fetch result from datasource '" + datasource.getUrl() + "' while no results are available.");
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }
}
