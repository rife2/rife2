/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * Request parameters with a special meaning for RIFE2.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public final class SpecialParameters {
    /**
     * Not used for anything functional, just used to automatically
     * differentiate a URL for each deployment.
     *
     * @since 1.0
     */
    public static final String RND = "rnd";

    /**
     * The unique ID of a continuation to resume.
     *
     * @since 1.0
     */
    public static final String CONT_ID = "contId";
}
