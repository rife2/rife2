/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.engine.Context;

/**
 * The method of a {@code CrudAfterHook} will be executed right after an
 * administration stored or removed an instance, inside the transaction of
 * that operation.
 * <p>The hook runs after the administration verified what actually reached
 * the database, so throwing from it takes the whole operation back. That
 * ordering can't be had with the query manager's own callbacks, which run
 * before that verification.
 * <p>The hooks only run for operations that come through the
 * administration, and they receive the context of the request that
 * performed one, which makes them the place to record who changed what.
 * <p>A hook can't send the request somewhere else. A redirect would commit the transaction and send the browser on
 * without the operation ever reporting its own outcome or verifying what
 * reached the database, so it's refused and reported as a
 * {@link rife.crud.exceptions.HookDivertedRequestException}, which takes the
 * operation back. A
 * {@link CrudBeforeHook before hook} runs before anything was stored and
 * can divert the request.
 * <p>The identifier, the ordinal and the property that the ordinal is
 * restricted to can't be changed by a hook, exactly like they can't be
 * changed by a
 * {@link rife.database.querymanagers.generic.Callbacks callback}. An instance that is stored again by a hook and ends up in another
 * list than the one that is being held reports a
 * {@link rife.crud.exceptions.PlacementChangedException} and the operation
 * is taken back, since the ordinals of the list it went to were never
 * locked for it.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#afterAdd(CrudAfterHook)
 * @see CrudEntityOptions#afterUpdate(CrudAfterHook)
 * @see CrudEntityOptions#afterDelete(CrudAfterHook)
 * @since 1.10
 */
@FunctionalInterface
public interface CrudAfterHook<T> {
    /**
     * Executed right after an instance was stored or removed.
     *
     * @param context  the context of the request that performed the
     *                 operation
     * @param instance the instance that was stored or removed
     * @since 1.10
     */
    void perform(Context context, T instance);
}
