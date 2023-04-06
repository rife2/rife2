/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import rife.database.Datasource;

import java.io.Serial;

public class TransactionBeginErrorException extends TransactionErrorException {
    @Serial private static final long serialVersionUID = -75164107264303943L;

    public TransactionBeginErrorException(Datasource datasource, Throwable cause) {
        super("Error while beginning the transaction.", datasource, cause);
    }
}
