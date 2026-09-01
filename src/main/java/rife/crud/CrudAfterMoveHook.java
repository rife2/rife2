/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.engine.Context;

/**
 * The method of a {@code CrudAfterMoveHook} will be executed right after an
 * administration moved an instance, while the list that it moved in is still
 * being held, so throwing from it takes the move back.
 * <p>A move names the instance by its identifier throughout and never
 * restores it, since a move only works with where the row sits, so this
 * hook is given that identifier rather than an instance.
 * <p>An instance that was already at the end of its list isn't moved and
 * this isn't executed for it.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#afterMove(CrudAfterMoveHook)
 * @since 1.10
 */
@FunctionalInterface
public interface CrudAfterMoveHook {
    /**
     * Executed right after an instance was moved.
     *
     * @param context the context of the request that performed the move
     * @param id      the identifier of the instance that was moved
     * @param upwards {@code true} when the instance moved towards the start
     *                of its list; or
     *                <p>{@code false} when it moved towards the end
     * @since 1.10
     */
    void perform(Context context, int id, boolean upwards);
}
