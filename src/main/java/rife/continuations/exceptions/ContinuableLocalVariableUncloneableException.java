/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.exceptions;

import java.io.Serial;

/**
 * Thrown when a local variable in a {@link rife.continuations.ContinuationStack}
 * couldn't be cloned when a continuation is resumed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ContinuableLocalVariableUncloneableException extends CloneNotSupportedException {
    @Serial private static final long serialVersionUID = -6226277931198774505L;

    private final Class continuableClass_;
    private final String localVarType_;

    /**
     * Instantiates a new exception.
     *
     * @param continuableClass the class of the continuable that contains an
     *                         unclonable local variable
     * @param localVarType     the type of the local variable
     * @param cause            the cause of the retrieval failure; or
     *                         <p>{@code null} if there was no exception cause
     * @since 1.0
     */
    public ContinuableLocalVariableUncloneableException(Class continuableClass, String localVarType, Throwable cause) {
        super("The continuable with class name '" + continuableClass.getName() + "' uses a local method variable of type '" + localVarType + "' which is not cloneable.");

        initCause(cause);

        continuableClass_ = continuableClass;
        localVarType_ = localVarType;
    }

    /**
     * Retrieves the class of the continuable that contains an unclonable
     * local variable.
     *
     * @return the class of the continuable
     * @since 1.0
     */
    public Class getContinuableClass() {
        return continuableClass_;
    }

    /**
     * The type of the local variable that can't be cloned.
     *
     * @return the type of the local variable
     * @since 1.0
     */
    public String getLocalVarType() {
        return localVarType_;
    }
}
