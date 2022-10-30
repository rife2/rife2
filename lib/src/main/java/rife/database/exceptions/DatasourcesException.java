/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class DatasourcesException extends DatabaseException {
    @Serial private static final long serialVersionUID = 6060312635494918174L;

    public DatasourcesException(String message) {
        super(message);
    }

    public DatasourcesException(Throwable cause) {
        super(cause);
    }

    public DatasourcesException(String message, Throwable cause) {
        super(message, cause);
    }
}
