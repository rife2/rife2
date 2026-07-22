/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

/**
 * Base class for the exceptions that a CSRF token verification raises, so
 * that they can be caught and handled together.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CsrfTokenMissingException
 * @see CsrfTokenInvalidException
 * @since 1.10
 */
public abstract class CsrfTokenException extends EngineException {
    @Serial private static final long serialVersionUID = 4116566267291557179L;

    protected CsrfTokenException(String message) {
        super(message);
    }
}
