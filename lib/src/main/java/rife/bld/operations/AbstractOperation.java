/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

/**
 * Provides common features across all operations
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.2
 */
public abstract class AbstractOperation<T extends AbstractOperation<T>> {
    private boolean silent_ = false;

    /**
     * Changes whether the operation should be silent or not.
     * <p>
     * Defaults to not silent.
     *
     * @param silent {@code true} if the operation should be silent;
     *               {@code false} otherwise
     * @return this operation instance
     * @since 1.5.2
     */
    public T silent(boolean silent) {
        silent_ = silent;
        return (T)this;
    }

    /**
     * Indicates whether the operation should be silent or not.
     *
     * @return {@code true} if the operation should be silent;
     * {@code false} otherwise
     * @since 1.5.2
     */
    public boolean silent() {
        return silent_;
    }
}
