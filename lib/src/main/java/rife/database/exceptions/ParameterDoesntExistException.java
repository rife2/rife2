/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.DbPreparedStatement;

import java.io.Serial;

public class ParameterDoesntExistException extends DatabaseException {
    @Serial private static final long serialVersionUID = -5547694215702755839L;

    private final DbPreparedStatement preparedStatement_;
    private final String parameterName_;

    public ParameterDoesntExistException(DbPreparedStatement statement, String parameterName) {
        super("The statement with sql '" + statement.getSql() + "' doesn't contain the parameter '" + parameterName + "'.");
        preparedStatement_ = statement;
        parameterName_ = parameterName;
    }

    public DbPreparedStatement getPreparedStatement() {
        return preparedStatement_;
    }

    public String getParameterName() {
        return parameterName_;
    }
}
