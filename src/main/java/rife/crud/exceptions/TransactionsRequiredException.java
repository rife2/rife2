/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

/**
 * Reports a datasource that an administration can't administer with, which is
 * one whose driver has no transactions.
 * <p>An operation and the hooks that follow it share a single transaction, so
 * that a hook which throws takes the whole operation back. A driver without
 * transactions has nothing to take it back with, which would leave a refused
 * operation half performed.
 * <p>An entity that is ordered manually is refused by the ordinal sequence
 * that hands out its places instead, which holds the whole list of one inside
 * a transaction and reports {@link
 * rife.cmf.dam.exceptions.TransactionsRequiredException} of its own.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class TransactionsRequiredException extends CrudException {
    @Serial private static final long serialVersionUID = 4611686018427387905L;

    private final String driver_;

    public TransactionsRequiredException(String driver) {
        super("The driver '" + driver + "' doesn't support transactions, which is what an operation and the hooks that follow it are performed in.");

        driver_ = driver;
    }

    public String getDriver() {
        return driver_;
    }
}
