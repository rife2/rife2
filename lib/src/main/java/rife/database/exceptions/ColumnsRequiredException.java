/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class ColumnsRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = 6643369478401322040L;

    private final String queryName_;

    public ColumnsRequiredException(String queryName) {
        super(queryName + " queries require columns.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
