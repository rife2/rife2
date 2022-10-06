/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class TableNameOrFieldsRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = -1252775241150915434L;

    private final String queryName_;

    public TableNameOrFieldsRequiredException(String queryName) {
        super(queryName + " queries require a table name or fields.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
