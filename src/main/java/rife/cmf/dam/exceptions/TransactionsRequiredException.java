/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class TransactionsRequiredException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 5518430927265384176L;

    private final String driver_;

    public TransactionsRequiredException(String driver) {
        super("The driver '" + driver + "' doesn't support transactions, which is what the ordinals of a scope are kept apart with.");

        driver_ = driver;
    }

    public String getDriver() {
        return driver_;
    }
}
