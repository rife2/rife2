/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class TransactionTimedOutException extends TransactionErrorException {
    @Serial private static final long serialVersionUID = 6277363843403636905L;

    public TransactionTimedOutException(Datasource datasource) {
        super("The transaction timed out.", datasource, null);
    }
}
