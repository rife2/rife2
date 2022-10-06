/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class DriverNameRetrievalErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = -2809651374321112986L;

    public DriverNameRetrievalErrorException(Throwable cause) {
        super("Unexpected error while retrieving the driver name of a JDBC connection.", cause);
    }
}
