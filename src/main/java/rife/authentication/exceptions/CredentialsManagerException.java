/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.exceptions;

import java.io.Serial;

public class CredentialsManagerException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1084302907754518421L;

    public CredentialsManagerException(String message) {
        super(message);
    }

    public CredentialsManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CredentialsManagerException(Throwable cause) {
        super(cause);
    }
}
