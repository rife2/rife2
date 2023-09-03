/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.basic;

import rife.continuations.CallState;
import rife.continuations.exceptions.CallTargetNotFoundException;

/**
 * Retrieves the continuable for a call continuation where the call target is a
 * class.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ClassCallTargetRetriever implements CallTargetRetriever {
    /**
     * Retrieves the continuable for a call continuation.
     *
     * @param target the call target object that will be used to retrieve the
     *               continuable
     * @param state  the call state
     * @return the call continuable; or
     * <p>{@code null} if no continuable should be executed immediately in
     * response to this call
     * @since 1.0
     */
    public Object getCallTarget(Object target, CallState state) {
        try {
            var target_class = (Class) target;
            return target_class.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            throw new CallTargetNotFoundException(target, e);
        }
    }
}
