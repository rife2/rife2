/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartUnexpectedEndingException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = -9216259920842606514L;

    public MultipartUnexpectedEndingException() {
        super("Premature ending of the form data.");
    }
}
