/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

/**
 * Thrown when a urlencoded form body or query string can't be parsed, or
 * goes over the form limits. RIFE2 answers the request with {@code 400}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11.0
 */
public class InvalidFormException extends EngineException {
    @Serial
    private static final long serialVersionUID = 4581276330981347612L;

    public InvalidFormException(String message) {
        super(message);
    }
}
