/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class ExecutionErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = 4317171502649179520L;

    private final String sql_;
    private final Datasource datasource_;

    public ExecutionErrorException(String sql, Datasource datasource, Throwable cause) {
        super("Error while executing the SQL '" + sql + "'.", cause);
        sql_ = sql;
        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }

    public String getSql() {
        return sql_;
    }
}
