/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.engine.Context;

/**
 * The method of a {@code CrudBeforeHook} will be executed right before an
 * administration stores or removes an instance, so that the operation can
 * be refused with a message that the user gets to read.
 * <p>The hook runs after the submission was validated and before anything
 * was stored, so a refusal has nothing to undo. It receives the context of
 * the request that performs the operation, so you can find out who is
 * performing it.
 * <p>A hook is meant to decide, not to change the instance. The instance
 * was validated before this runs and isn't validated again afterwards, so a
 * value that a hook puts on it is stored without anything having checked
 * it. Corrections to the instance belong in the constraints or in the
 * callbacks of the bean instead.
 * <p>Storing something of your own belongs in a
 * {@link CrudAfterHook after hook}, which runs inside the transaction of
 * the operation. Whether the transaction is already open when this runs
 * depends on the entity, so anything a before hook writes isn't tied to the
 * outcome of the operation.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#beforeAdd(CrudBeforeHook)
 * @see CrudEntityOptions#beforeUpdate(CrudBeforeHook)
 * @see CrudEntityOptions#beforeDelete(CrudBeforeHook)
 * @since 1.10
 */
@FunctionalInterface
public interface CrudBeforeHook<T> {
    /**
     * Executed right before an instance is stored or removed.
     *
     * @param context  the context of the request that performs the
     *                 operation
     * @param instance the instance that is about to be stored or removed
     * @return {@code null} when the operation can proceed; or
     * <p>the message that refuses it
     * @since 1.10
     */
    String check(Context context, T instance);
}
