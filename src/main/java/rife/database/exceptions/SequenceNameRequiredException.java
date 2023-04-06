/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class SequenceNameRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = -1117694732120142775L;

    private String queryName_;

    public SequenceNameRequiredException(String queryName) {
        super(queryName + " queries require a sequence name.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
