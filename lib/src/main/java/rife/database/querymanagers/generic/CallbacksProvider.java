/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

/**
 * Callbacks can either be implemented directly by implementing the {@link
 * rife.database.querymanagers.generic.Callbacks} interface, or they
 * can be provided by implementing this interface.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.database.querymanagers.generic.Callbacks
 * @since 1.0
 */
public interface CallbacksProvider<BeanType> {
    /**
     * Returns an implementation of the {@link
     * rife.database.querymanagers.generic.Callbacks} interface.
     *
     * @return a callbacks instance
     * @since 1.0
     */
    public Callbacks<BeanType> getCallbacks();
}
