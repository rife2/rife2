/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class FieldsRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = 5937549014842696343L;

    private String queryName_;

    public FieldsRequiredException(String queryName) {
        super(queryName + " queries require fields.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
