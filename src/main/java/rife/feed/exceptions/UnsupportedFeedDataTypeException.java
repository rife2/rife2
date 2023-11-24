/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed.exceptions;

import java.io.Serial;

public class UnsupportedFeedDataTypeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8910041916874032181L;

    public UnsupportedFeedDataTypeException() {
        super();
    }

    public UnsupportedFeedDataTypeException(String message) {
        super(message);
    }

    public UnsupportedFeedDataTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedFeedDataTypeException(Throwable cause) {
        super(cause);
    }
}
