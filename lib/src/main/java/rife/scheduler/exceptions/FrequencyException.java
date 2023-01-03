/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class FrequencyException extends RuntimeException {
    @Serial private static final long serialVersionUID = -3013172833353569656L;

    public FrequencyException(String message) {
        super(message);
    }

    public FrequencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrequencyException(Throwable cause) {
        super(cause);
    }
}
