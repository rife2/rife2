/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

import java.io.Serial;
import java.lang.reflect.Method;

/**
 * An error that is intended to be as lightweight as possible.
 * <p>
 * Typically, this is used for {@link ControlFlowRuntimeException} exceptions so
 * that as little overhead as possible is imposed when these exceptions are
 * thrown. This is achieved by enforcing the stack traces to be empty, causing
 * them to not be captured.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class LightweightError extends Error {
    @Serial private static final long serialVersionUID = -6077740593752636392L;

    public LightweightError() {
        super();
    }

    public LightweightError(String message) {
        super(message);
    }

    public LightweightError(String message, Throwable cause) {
        super(message, cause);
    }

    public LightweightError(Throwable cause) {
        super(cause);
    }

    public Throwable fillInStackTrace() {
        return null;
    }

    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }
}
