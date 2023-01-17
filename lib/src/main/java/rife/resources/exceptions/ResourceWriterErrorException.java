/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class ResourceWriterErrorException extends Exception {
    @Serial private static final long serialVersionUID = -3012453669174294722L;

    public ResourceWriterErrorException(String message) {
        super(message);
    }

    public ResourceWriterErrorException(String message, Throwable e) {
        super(message, e);
    }
}
