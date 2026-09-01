/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.engine.Context;

/**
 * The method of a {@code CrudActionHandler} will be executed when a custom
 * action is performed on a single instance of an entity.
 * <p>The full context is provided, so that an action can do anything that
 * an element can do, like streaming a document back instead of returning to
 * the browse page.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudAction
 * @since 1.10
 */
@FunctionalInterface
public interface CrudActionHandler<T> {
    /**
     * Performs the action on an instance.
     * <p>The browser is sent back to the browse page of the entity when this
     * method returns. Producing your own response therefore means printing
     * it and interrupting the request with {@link Context#respond()}, since
     * printing by itself doesn't stop the redirect that follows.
     *
     * @param context  the context of the request that triggered the action
     * @param instance the instance to perform the action on
     * @return the message that confirms the action; or
     * <p>{@code null} when no message should be shown
     * @since 1.10
     */
    String perform(Context context, T instance);
}
