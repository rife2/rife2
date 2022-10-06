/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
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
 * @since 1.6
 */
public class LightweightError extends Error {
    @Serial private static final long serialVersionUID = -6077740593752636392L;

    private static boolean sUseFastExceptions = true;

    private Boolean useFastExceptions_;

    public static synchronized void setUseFastExceptions(boolean flag) {
        sUseFastExceptions = flag;
    }

    public static boolean getUseFastExceptions() {
        return sUseFastExceptions;
    }

    private void init() {
        // TODO
//        if (useFastExceptions_ != null) return;
//        try {
//            // detect the presence of RifeConfig and use that if possible, otherwise
//            // use the static flag of this class
//            Class global_class = Class.forName("rife.config.RifeConfig$Global");
//            Method get_use_fast_exceptions = global_class.getDeclaredMethod("getUseFastExceptions", new Class[0]);
//            useFastExceptions_ = ((Boolean) get_use_fast_exceptions.invoke(null, new Object[0])).booleanValue();
//        } catch (Exception e) {
            useFastExceptions_ = sUseFastExceptions;
//        }
    }

    public LightweightError() {
        super();
        init();
    }

    public LightweightError(String message) {
        super(message);
        init();
    }

    public LightweightError(String message, Throwable cause) {
        super(message, cause);
        init();
    }

    public LightweightError(Throwable cause) {
        super(cause);
        init();
    }

    public Throwable fillInStackTrace() {
        init();
        if (useFastExceptions_) {
            return null;
        } else {
            return super.fillInStackTrace();
        }
    }

    public StackTraceElement[] getStackTrace() {
        init();
        if (useFastExceptions_) {
            return new StackTraceElement[0];
        } else {
            return super.getStackTrace();
        }
    }
}
