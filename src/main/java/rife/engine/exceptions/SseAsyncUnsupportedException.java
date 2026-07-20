/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

/**
 * Thrown when a detached server-sent events connection can't be established
 * because the request doesn't support asynchronous processing.
 * <p>The embedded {@code Server} and {@code TomcatServer} enable this
 * automatically. When deploying as a war archive, asynchronous support has
 * to be declared explicitly in {@code web.xml}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class SseAsyncUnsupportedException extends EngineException {
    @Serial
    private static final long serialVersionUID = 1149877773340279418L;

    public SseAsyncUnsupportedException(Throwable cause) {
        super("""
            Couldn't establish a detached server-sent events connection because \
            asynchronous request processing isn't supported for this request. \
            When deploying as a war archive, add <async-supported>true</async-supported> \
            to the RIFE2 filter declaration in web.xml (as well as to any other \
            filters and the servlet in the request's chain). The embedded Server \
            and TomcatServer enable this automatically.""", cause);
    }
}
