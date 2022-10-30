/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class TransactionSupportCheckErrorException extends TransactionErrorException {
    @Serial private static final long serialVersionUID = 2834697164959844045L;

    public TransactionSupportCheckErrorException(Datasource datasource, Throwable cause) {
        super("Error while checking the transaction support.", datasource, cause);
    }
}
