/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.exceptions;

import java.io.Serial;

public class ValidationBuilderException extends RuntimeException {
    @Serial private static final long serialVersionUID = -1875900584540609689L;

    public ValidationBuilderException(String message) {
        super(message);
    }

    public ValidationBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationBuilderException(Throwable cause) {
        super(cause);
    }
}
