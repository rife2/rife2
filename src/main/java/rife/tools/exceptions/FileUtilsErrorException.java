/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

import java.io.IOException;
import java.io.Serial;

public class FileUtilsErrorException extends IOException {
    @Serial
    private static final long serialVersionUID = -2727442872243338966L;

    public FileUtilsErrorException(String message) {
        super(message);
    }

    public FileUtilsErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
