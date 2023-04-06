/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

import java.io.Serial;

public abstract class SerializationUtilsErrorException extends Exception {
    @Serial private static final long serialVersionUID = 5207703400979008703L;

    public SerializationUtilsErrorException(Throwable cause) {
        super(cause);
    }

    public SerializationUtilsErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationUtilsErrorException(String message) {
        super(message);
    }
}
