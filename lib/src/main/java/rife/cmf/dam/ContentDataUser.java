/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.tools.InnerClassException;

/**
 * By implementing this interface it's possible to provide the logic that should be
 * executed by methods that allow interaction with content data.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
@FunctionalInterface
public interface ContentDataUser<ResultType> {
    /**
     * Should be implemented by all implementations.
     *
     * @since 1.0
     */
    ResultType useContentData(Object contentData)
    throws InnerClassException;

    /**
     * Calling this method makes it possible to throw a checked exception from
     * within this class.
     * <p>To catch it you should surround the using method with a
     * {@code try-catch} block that catching
     * {@code InnerClassException}. The original exception is then
     * available through {@code getCause()} and can for example be
     * rethrown.
     *
     * @throws InnerClassException when a checked exception needs to be
     *                             thrown from within this class and caught outside the caller.
     * @since 1.0
     */
    default void throwException(Exception exception)
    throws InnerClassException {
        throw new InnerClassException(exception);
    }
}

