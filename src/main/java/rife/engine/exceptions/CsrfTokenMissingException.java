/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

/**
 * Thrown when a request that changes state doesn't provide a CSRF token,
 * either because the browser sent no token cookie, or because the request
 * carries neither the token parameter nor the token header.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CsrfTokenMissingException extends CsrfTokenException {
    @Serial private static final long serialVersionUID = 3306508374106421310L;

    public CsrfTokenMissingException(String detail) {
        super("The CSRF token is missing: " + detail + ".");
    }
}
