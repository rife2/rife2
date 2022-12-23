/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.RollbackException;
import rife.tools.ExceptionUtils;
import rife.tools.InnerClassException;

import java.util.logging.Logger;

/**
 * By extending this class it's possible to provide the logic that should be
 * executed by the {@link DbQueryManager#inTransaction(TransactionUser) inTransaction}
 * method in the {@link DbQueryManager} class.
 * <p>This class has both a default constructor and one that can take a data
 * object. This can be handy when using it as an extending anonymous inner
 * class when you need to use variables inside the inner class that are
 * cumbersome to change to <code>final</code> in the enclosing class.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see DbQueryManager#inTransaction(TransactionUser)
 * @since 1.0
 */
public abstract class DbTransactionUser<ResultType, DataType> implements Cloneable, TransactionUser<ResultType> {
    protected DataType data_ = null;

    public DbTransactionUser() {
    }

    public DbTransactionUser(DataType data) {
        data_ = data;
    }

    public DataType getData() {
        return data_;
    }

    /**
     * Should be overridden if the transaction has to be executed in another
     * isolation level.
     *
     * @return <code>-1</code> when the active isolation level should be
     * preserved; or
     * <p>a level constant from {@link java.sql.Connection Connection} if the
     * isolation needs to be changed.
     * @since 1.0
     */
    public int getTransactionIsolation() {
        return -1;
    }

    /**
     * Should be used to roll back ongoing transactions, otherwise enclosing
     * transaction users might not be interrupted and subsequent modification
     * can still happen outside the transaction.
     *
     * @throws RollbackException indicates that a rollback should happen
     *                           and all further transaction logic interrupted.
     * @since 1.0
     */
    public void rollback()
    throws RollbackException {
        throw new RollbackException();
    }

    /**
     * Calling this method makes it possible to throw a checked exception from
     * within this class.
     * <p>To catch it you should surround the {@link
     * DbQueryManager#inTransaction(TransactionUser) inTransaction} with a
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
    public abstract ResultType useTransaction()
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
