/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

/**
 * Thrown when a server-sent events connection is established for a request
 * that already has one, since two connections over the same response would
 * interleave their event writes and corrupt the stream.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class SseConnectionAlreadyEstablishedException extends EngineException {
    @Serial private static final long serialVersionUID = 2265762724471304447L;

    public SseConnectionAlreadyEstablishedException() {
        super("An SSE connection has already been established for this request.");
    }
}
