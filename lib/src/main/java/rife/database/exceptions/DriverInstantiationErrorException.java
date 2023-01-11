/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class DriverInstantiationErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = -8357643089890580253L;

    private final String driver_;

    public DriverInstantiationErrorException(String driver, Throwable cause) {
        super("Couldn't instantiate the JDBC driver '" + driver + "'.", cause);
        driver_ = driver;
    }

    public String getDriver() {
        return driver_;
    }
}
