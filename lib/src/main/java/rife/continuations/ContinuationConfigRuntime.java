/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import rife.continuations.exceptions.MissingActiveContinuationConfigRuntimeException;

/**
 * Configures the runtime behavior of the continuations engine.
 * <p>The active runtime configuration always has to be available through
 * {@link #getActiveConfigRuntime()} when a continuable object is
 * executed. Therefore, it's best to always call
 * {@link #setActiveConfigRuntime} before the execution. The
 * {@link rife.continuations.basic.BasicContinuableRunner} does
 * this by default. If you create your own runner, you have to ensure that
 * this is respected.
 * <p>By default the lifetime duration and purging of continuable object
 * instances is set to a sensible default, so this only needs tuning in
 * specific case.
 * <p>This class has to be extended though to provide information that suits
 * your continuations usage and to indicate whether continuations should be
 * cloned when they are resumed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class ContinuationConfigRuntime implements ContinuationConfigRuntimeDefaults {
    private static final ThreadLocal<ContinuationConfigRuntime> ACTIVE_CONFIG_RUNTIME = new ThreadLocal<>();

    /**
     * Sets the active runtime configuration for the executing thread.
     *
     * @param config the active runtime configuration for the executing thread
     * @since 1.0
     */
    public static void setActiveConfigRuntime(ContinuationConfigRuntime config) {
        ACTIVE_CONFIG_RUNTIME.set(config);
    }

    /**
     * Retrieves the active runtime configuration for the executing thread.
     *
     * @return the active runtime configuration
     * @throws MissingActiveContinuationConfigRuntimeException when the active
     *                                                         runtime configuration isn't set
     * @since 1.0
     */
    public static ContinuationConfigRuntime getActiveConfigRuntime()
    throws MissingActiveContinuationConfigRuntimeException {
        var config = ACTIVE_CONFIG_RUNTIME.get();
        if (null == config) {
            throw new MissingActiveContinuationConfigRuntimeException();
        }
        return config;
    }

    /**
     * The duration, in milliseconds, by which a continuation stays valid.
     * <p>When this period is exceeded, a continuation can not be retrieved
     * anymore, and it will be removed from the manager during the next purge.
     *
     * @return the validity duration of a continuation in milliseconds
     * @since 1.0
     */
    public long getContinuationDuration() {
        return DEFAULT_CONTINUATION_DURATION;
    }

    /**
     * The frequency by which the continuations purging will run in the
     * {@link ContinuationManager}.
     * <p>This works together with the scale that is configured through
     * {@link #getContinuationPurgeScale}. The frequency divided by the scale
     * makes how often the purging will happen. For instance, a frequency of 20
     * and a scale of 1000 means that purging will happen 1/50th of the time.
     *
     * @return the continuation purge frequency
     * @see #getContinuationPurgeScale
     * @since 1.0
     */
    public int getContinuationPurgeFrequency() {
        return DEFAULT_CONTINUATION_PURGE_FREQUENCY;
    }

    /**
     * The scale that will be used to determine how often continuations purging
     * will run in the {@link ContinuationManager}.
     * <p>See {@link #getContinuationPurgeFrequency} for more info.
     *
     * @return the continuation purge scale
     * @see #getContinuationPurgeFrequency
     * @since 1.0
     */
    public int getContinuationPurgeScale() {
        return DEFAULT_CONTINUATION_PURGE_SCALE;
    }

    /**
     * Retrieves the manager that is responsible for the
     * continuable object that is currently executing.
     *
     * @param executingInstance the currently executing object instance
     * @return the corresponding manager
     * @since 1.0
     */
    public abstract ContinuationManager getContinuationManager(Object executingInstance);

    /**
     * Indicates whether a continuable should be cloned before resuming the
     * execution.
     *
     * @param executingContinuable the currently executing continuable
     * @return {@code true} is the continuation should be cloned; or
     * <p>{@code false} otherwise
     * @since 1.0
     */
    public abstract boolean cloneContinuations(Object executingContinuable);
}

