/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format.exceptions;

import java.io.Serial;

public class FormatException extends Exception {
    @Serial private static final long serialVersionUID = -3292520395011984387L;

    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatException(Throwable cause) {
        super(cause);
    }
}
