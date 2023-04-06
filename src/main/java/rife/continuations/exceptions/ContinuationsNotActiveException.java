/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.exceptions;

import rife.tools.exceptions.ControlFlowRuntimeException;

import java.io.Serial;

/**
 * Thrown when a continuations method has not been instrumented.
 * <p>This is typically the sole method body of the methods that are present
 * in a continuable support object.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ContinuationsNotActiveException extends RuntimeException implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = -7358516398081778097L;

    /**
     * Instantiates a new exception.
     *
     * @since 1.0
     */
    public ContinuationsNotActiveException() {
        super("Continuations are not active for this class method. This class hasn't been instrumented or is has been reloaded through hot-swap.");
    }
}
