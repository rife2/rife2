/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.IOException;
import java.io.Serial;

public class MultipartInputErrorException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = 4019071444351340575L;

    public MultipartInputErrorException(IOException e) {
        super("Unexpected error during the input of the multipart request content.", e);
    }
}
