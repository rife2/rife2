/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

/**
 * Default values for {@link ContinuationConfigRuntime}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface ContinuationConfigRuntimeDefaults {
    /**
     * The default duration is 20 minutes.
     *
     * @since 1.0
     */
    long DEFAULT_CONTINUATION_DURATION = 1200000;

    /**
     * The default frequency is every 20 times out of the scale, with the
     * default scale of 1000 this means, 1/50th of the time.
     *
     * @since 1.0
     */
    int DEFAULT_CONTINUATION_PURGE_FREQUENCY = 20;

    /**
     * The default purge scale is 1000.
     *
     * @since 1.0
     */
    int DEFAULT_CONTINUATION_PURGE_SCALE = 1000;
}