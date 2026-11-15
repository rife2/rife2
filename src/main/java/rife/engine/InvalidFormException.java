/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

// a urlencoded form or query that the site runner answers with 400, like Jetty
class InvalidFormException extends RuntimeException {
    InvalidFormException(String message) {
        super(message);
    }
}
