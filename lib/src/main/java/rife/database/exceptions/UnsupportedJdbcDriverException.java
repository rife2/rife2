/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class UnsupportedJdbcDriverException extends RuntimeException {
    @Serial private static final long serialVersionUID = 664475636733401910L;

    private final String driver_;

    public UnsupportedJdbcDriverException(String driver, Throwable cause) {
        super("The JDBC driver '" + driver + "' isn't supported, certain functionalities will not function correctly.", cause);
        driver_ = driver;
    }

    public String getDriver() {
        return driver_;
    }
}
