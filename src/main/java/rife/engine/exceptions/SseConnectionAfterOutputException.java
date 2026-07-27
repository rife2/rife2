/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

/**
 * Thrown when a server-sent events connection is established for a request
 * whose response output has already been produced or handed out, since that
 * content would precede the event stream and corrupt it.
 * <p>You should establish the connection before anything is written to the
 * response of the request.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SseOutputRefusedException
 * @see SseConnectionAlreadyEstablishedException
 * @since 1.10
 */
public class SseConnectionAfterOutputException extends EngineException {
    @Serial private static final long serialVersionUID = 7085768638333810051L;

    public SseConnectionAfterOutputException() {
        super("An SSE connection can't be established after response content has been produced.");
    }
}
