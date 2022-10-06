/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartRequestException extends EngineException {
    @Serial
    private static final long serialVersionUID = 2383362830269664082L;

    public MultipartRequestException() {
        super();
    }

    public MultipartRequestException(String message) {
        super(message);
    }

    public MultipartRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipartRequestException(Throwable cause) {
        super(cause);
    }
}
