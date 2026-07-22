/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

/**
 * Thrown when a request that changes state provides a CSRF token that
 * doesn't correspond to the token of the browser that established the
 * session.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CsrfTokenInvalidException extends CsrfTokenException {
    @Serial private static final long serialVersionUID = 8878932880988712611L;

    public CsrfTokenInvalidException() {
        super("The submitted CSRF token doesn't correspond to the token of this browser.");
    }
}
