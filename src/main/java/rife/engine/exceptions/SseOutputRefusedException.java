/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

/**
 * Thrown when regular response output is used after a server-sent events
 * connection has been established for the request, since that output would
 * corrupt the event stream.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class SseOutputRefusedException extends EngineException {
    @Serial private static final long serialVersionUID = 5424068817963931362L;

    public SseOutputRefusedException() {
        super("Regular response output can't be used after an SSE connection has been established, use the connection to send events.");
    }
}
