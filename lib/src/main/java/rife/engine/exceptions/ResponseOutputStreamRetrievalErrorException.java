/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class ResponseOutputStreamRetrievalErrorException extends EngineException {
    @Serial
    private static final long serialVersionUID = -8974926963726906900L;

    public ResponseOutputStreamRetrievalErrorException(Throwable e) {
        super("An error occurred during the retrieval of the response output stream.", e);
    }
}
