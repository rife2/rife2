/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import java.io.Serial;

public class DateFormatInitializationException extends RuntimeException {
    @Serial private static final long serialVersionUID = 3704524567671436091L;

    public DateFormatInitializationException(Throwable cause) {
        super(cause);
    }

    public DateFormatInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DateFormatInitializationException(String message) {
        super(message);
    }
}
