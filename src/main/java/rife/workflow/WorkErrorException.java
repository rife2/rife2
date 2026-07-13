/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import java.io.Serial;

/**
 * Reports the failure of {@link Work} that was executing in a
 * {@link Workflow}.
 * <p>The message describes which work failed and, when the failure was
 * caused by missing continuations instrumentation, how to resolve that.
 * The original exception that was thrown by the work is available through
 * {@link #getCause()}.
 *
 * @rife.apiNote The workflow engine is still in an ALPHA EXPERIMENTAL STAGE and might change.
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ErrorListener
 * @since 1.10
 */
public class WorkErrorException extends RuntimeException {
    @Serial private static final long serialVersionUID = 5251971526966208626L;

    public WorkErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
