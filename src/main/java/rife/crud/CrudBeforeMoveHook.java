/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.engine.Context;

/**
 * The method of a {@code CrudBeforeMoveHook} will be executed right before
 * an administration moves an instance, so that the move can be refused with
 * a message that the user gets to read.
 * <p>A move names the instance by its identifier throughout and never
 * restores it, since a move only works with where the row sits, so this
 * hook is given that identifier rather than an instance. Restore it
 * yourself when the decision needs more than that.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#beforeMove(CrudBeforeMoveHook)
 * @since 1.10
 */
@FunctionalInterface
public interface CrudBeforeMoveHook {
    /**
     * Executed right before an instance is moved.
     *
     * @param context the context of the request that performs the move
     * @param id      the identifier of the instance that is about to be
     *                moved
     * @param upwards {@code true} when the instance moves towards the start
     *                of its list; or
     *                <p>{@code false} when it moves towards the end
     * @return {@code null} when the move can proceed; or
     * <p>the message that refuses it
     * @since 1.10
     */
    String check(Context context, int id, boolean upwards);
}
