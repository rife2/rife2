/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader;

import java.util.Set;

/**
 * This is an interface that should be implemented by all content loader
 * back-ends.
 * <p>All content loader back-ends that are fronted by the same {@link
 * ContentLoader} should handle the same {@code InternalType}, which is
 * returned by the {@link #load(Object, boolean, Set) load} method.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface ContentLoaderBackend<InternalType> {
    /**
     * Indicates whether the back-end is present.
     * <p>This can be important for optional libraries that should only
     * actually try to load the data when the required classes are available
     * in the classpath.
     *
     * @return {@code true} if the back-end is present; or
     * <p>{@code false} if this is not the case
     * @since 1.0
     */
    boolean isBackendPresent();

    /**
     * Loads any kind of raw data and tries to accommodate as much as possible
     * to return an instance of {@code InternalType} after successful
     * loading and handling.
     * <p>Should any errors occur, then they will be added as text messages to
     * the {@code errors} collection.
     *
     * @param data     the raw data that has to be loaded
     * @param fragment {@code true} if the raw data is a fragment; or
     *                 <p>{@code false} if the raw data is a complete document or file
     * @param errors   a set to which possible error messages will be added
     * @return an instance of the {@code LoadedContent} with the {@code InternalType}; or
     * <p>{@code null} if the raw data couldn't be loaded
     * @since 1.4
     */
    LoadedContent<InternalType> load(Object data, boolean fragment, Set<String> errors);
}
