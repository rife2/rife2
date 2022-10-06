/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.DbPreparedStatement;

import java.io.Serial;

public class UndefinedVirtualParameterException extends DatabaseException {
    @Serial private static final long serialVersionUID = -7004752430133818652L;

    private final DbPreparedStatement preparedStatement_;
    private final String parameterName_;
    private final int parameterIndex_;

    public UndefinedVirtualParameterException(DbPreparedStatement statement, String parameterName) {
        super("The statement with sql '" + statement.getSql() + "' requires the definition of a value for the virtual parameter with name '" + parameterName + "'.");
        preparedStatement_ = statement;
        parameterName_ = parameterName;
        parameterIndex_ = -1;
    }

    public UndefinedVirtualParameterException(DbPreparedStatement statement, int parameterIndex) {
        super("The statement with sql '" + statement.getSql() + "' requires the definition of a value for the virtual parameter with index '" + parameterIndex + "'.");
        preparedStatement_ = statement;
        parameterName_ = null;
        parameterIndex_ = parameterIndex;
    }

    public DbPreparedStatement getPreparedStatement() {
        return preparedStatement_;
    }

    public String getParameterName() {
        return parameterName_;
    }

    public int getParameterIndex() {
        return parameterIndex_;
    }
}
