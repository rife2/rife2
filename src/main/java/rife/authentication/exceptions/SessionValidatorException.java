/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.exceptions;

import java.io.Serial;

public class SessionValidatorException extends RuntimeException {
    @Serial private static final long serialVersionUID = -6033104555814346647L;

    public SessionValidatorException(String message) {
        super(message);
    }

    public SessionValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionValidatorException(Throwable cause) {
        super(cause);
    }
}
