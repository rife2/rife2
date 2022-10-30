/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.DbPreparedStatement;

import java.io.Serial;

public class NoParametersException extends DatabaseException {
    @Serial private static final long serialVersionUID = -2087220322509692913L;

    private final DbPreparedStatement preparedStatement_;

    public NoParametersException(DbPreparedStatement statement) {
        super("The statement with sql '" + statement.getSql() + "' doesn't contain any parameters.");
        preparedStatement_ = statement;
    }

    public DbPreparedStatement getPreparedStatement() {
        return preparedStatement_;
    }
}
