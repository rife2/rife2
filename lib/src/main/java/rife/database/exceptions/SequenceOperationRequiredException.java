/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class SequenceOperationRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = -4800820909278366194L;

    private final String queryName_;

    public SequenceOperationRequiredException(String queryName) {
        super(queryName + " queries require a sequence operation to be provided.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
