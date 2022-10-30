/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * By extending this class it's possible to provide the logic that should be
 * executed by methods that allow interaction with an <code>InputStream</code>.
 * <p>This class has both a default constructor and one that can take a data
 * object. This can be handy when using it as an extending anonymous inner
 * class when you need to use variables inside the inner class that are
 * cumbersome to change to <code>final</code> in the enclosing class.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class InputStreamUser<ResultType, DataType> implements Cloneable {
    protected DataType data_ = null;

    public InputStreamUser() {
    }

    public InputStreamUser(DataType data) {
        data_ = data;
    }

    public DataType getData() {
        return data_;
    }

    /**
     * Calling this method makes it possible to throw a checked exception from
     * within this class.
     * <p>To catch it you should surround the using method with a
     * <code>try-catch</code> block that catching
     * <code>InnerClassException</code>. The original exception is then
     * available through <code>getCause()</code> and can for example be
     * rethrown.
     *
     * @throws InnerClassException when a checked exception needs to be
     *                             thrown from within this class and caught outside the caller.
     * @since 1.0
     */
    public void throwException(Exception exception)
    throws InnerClassException {
        throw new InnerClassException(exception);
    }

    /**
     * Should be implemented by all extending classes.
     *
     * @since 1.0
     */
    public abstract ResultType useInputStream(InputStream stream)
    throws InnerClassException;

    /**
     * Simply clones the instance with the default clone method since this
     * class contains no member variables.
     *
     * @since 1.0
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.database").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }
}

