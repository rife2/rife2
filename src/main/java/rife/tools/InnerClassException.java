/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.io.Serial;

public class InnerClassException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2692374153192760509L;

    public InnerClassException(String message, Exception cause) {
        super(message, cause);
    }

    public InnerClassException(Exception cause) {
        super(cause);
    }

    public Exception getCause() {
        return (Exception) super.getCause();
    }
}
