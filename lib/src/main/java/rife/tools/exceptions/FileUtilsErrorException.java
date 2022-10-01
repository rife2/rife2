/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

import java.io.Serial;

public class FileUtilsErrorException extends Exception {
    @Serial
    private static final long serialVersionUID = 5563842867757961501L;

    public FileUtilsErrorException(String message) {
        super(message);
    }

    public FileUtilsErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
