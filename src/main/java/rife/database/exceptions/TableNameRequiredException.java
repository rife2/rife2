/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class TableNameRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = 5815362326172483731L;

    private final String queryName_;

    public TableNameRequiredException(String queryName) {
        super(queryName + " queries require a table name.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
