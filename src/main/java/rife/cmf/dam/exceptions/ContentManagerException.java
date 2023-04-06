/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class ContentManagerException extends RuntimeException {
    @Serial private static final long serialVersionUID = 6769318449670036525L;

    public ContentManagerException(String message) {
        super(message, null);
    }

    public ContentManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentManagerException(Throwable cause) {
        super(cause);
    }
}
