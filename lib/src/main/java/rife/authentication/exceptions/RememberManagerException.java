/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.exceptions;

import java.io.Serial;

public class RememberManagerException extends Exception {
    @Serial private static final long serialVersionUID = 3486027286626106335L;

    public RememberManagerException(String message) {
        super(message);
    }

    public RememberManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RememberManagerException(Throwable cause) {
        super(cause);
    }
}
