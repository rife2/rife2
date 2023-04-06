/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.DbPreparedStatement;

import java.io.Serial;

public class UnsupportedVirtualParameterTypeException extends DatabaseException {
    @Serial private static final long serialVersionUID = -4366883446335774838L;

    private final DbPreparedStatement preparedStatement_;
    private final int parameterIndex_;
    private final String valueType_;

    public UnsupportedVirtualParameterTypeException(DbPreparedStatement statement, int parameterIndex, String valueType) {
        super("The statement with sql '" + statement.getSql() + "' doesn't support the value type '" + valueType + "' for the virtual parameter with index '" + parameterIndex + "'.");
        preparedStatement_ = statement;
        parameterIndex_ = parameterIndex;
        valueType_ = valueType;
    }

    public DbPreparedStatement getPreparedStatement() {
        return preparedStatement_;
    }

    public int getParameterIndex() {
        return parameterIndex_;
    }

    public String getValueType() {
        return valueType_;
    }
}
