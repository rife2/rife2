/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class TransactionRollbackErrorException extends TransactionErrorException {
    public TransactionRollbackErrorException(String action, Datasource datasource, Throwable cause) {
        super(action, datasource, cause);
    }

    @Serial private static final long serialVersionUID = 2809082434948067632L;

    public TransactionRollbackErrorException(Datasource datasource, Throwable cause) {
        super("Error while rolling back the transaction.", datasource, cause);
    }
}
