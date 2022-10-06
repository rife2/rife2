/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

/**
 * By extending this class it's possible to provide the logic that should be
 * executed by the {@link DbQueryManager#reserveConnection(DbConnectionUser)
 * reserveConnection} method in the {@link DbQueryManager} class.
 * <p>This class has both a default constructor and one that can take a data
 * object. This can be handy when using it as an extending anonymous inner
 * class when you need to use variables inside the inner class that are
 * cumbersome to change to <code>final</code> in the enclosing class.
 *
 * @author JR Boyens (jboyens[remove] at uwyn dot com)
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @see DbConnection
 * @see DbQueryManager#reserveConnection(DbConnectionUser)
 * @since 1.0
 */

import rife.tools.ExceptionUtils;
import rife.tools.InnerClassException;

import java.util.logging.Logger;

public abstract class DbConnectionUser<ResultType, DataType> implements Cloneable {
    protected DataType data_ = null;

    /**
     * Create a new DbConnectionUser.
     *
     * @since 1.0
     */
    public DbConnectionUser() {
    }

    /**
     * Create a new DbConnectionUser with a data object.
     *
     * @param data a user data object to be stored locally
     * @since 1.0
     */
    public DbConnectionUser(DataType data) {
        data_ = data;
    }

    /**
     * Retrieve the data object that was provided to the constructor.
     *
     * @return the provided data object; or
     * <p><code>null</code> if no data object was provided
     * @since 1.0
     */
    public DataType getData() {
        return data_;
    }

    /**
     * Calling this method makes it possible to throw a checked exception
     * from within this class.
     * <p>To catch it you should surround the {@link
     * DbQueryManager#reserveConnection(DbConnectionUser)
     * reserveConnection} with a <code>try-catch</code> block that catches
     * <code>InnerClassException</code>. The original exception is then
     * available through <code>getCause()</code> and can for example be
     * rethrown.
     *
     * @param exception the exception to be thrown
     * @exception InnerClassException when a checked exception needs to be
     * thrown from within this class and caught outside the caller.
     * @since 1.0
     */
    public void throwException(Exception exception)
    throws InnerClassException {
        throw new InnerClassException(exception);
    }

    /**
     * Should be implemented by all extending classes.
     *
     * @param connection the connection that should be used
     * @since 1.0
     */
    public abstract ResultType useConnection(DbConnection connection)
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

