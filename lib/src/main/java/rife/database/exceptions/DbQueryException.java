/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class DbQueryException extends RuntimeException {
    @Serial private static final long serialVersionUID = -2143066136860048063L;

    public DbQueryException(String message) {
        super(message);
    }

    public DbQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
