/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader;

import rife.cmf.dam.exceptions.ContentManagerException;

import java.util.List;
import java.util.Set;

/**
 * This is an abstract class that needs to be extended by all the classes that
 * are able to load raw data and converted it to a common internal type.
 * <p>Each content loader has a collection of back-ends that are able to
 * interpret the raw data. All that should be done by an extending class, is
 * implement the {@link #getBackends() getBackends} method and return a
 * {@code List} of supported loader back-ends.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ContentLoaderBackend
 * @since 1.0
 */
public abstract class ContentLoader<InternalType> {
    /**
     * Loads raw data and returns the internal type after successful loading
     * and handling.
     * <p>Should any errors occur in the back-ends, then they will be added as
     * text messages to the {@code errors} collection.
     *
     * @param data     the raw data that has to be loaded
     * @param fragment {@code true} if the raw data is a fragment; or
     *                 <p>{@code false} if the raw data is a complete document or file
     * @param errors   a set to which possible error messages will be added
     * @return an instance of the internal type that is common for all loaders
     * for the handled content type, for instance {@code java.awt.Image}
     * for loaders that handle images; or
     * <p>{@code null} if the raw data couldn't be loaded
     * @since 1.0
     */
    public InternalType load(Object data, boolean fragment, Set<String> errors)
    throws ContentManagerException {
        if (null == data) {
            return null;
        }

        InternalType result = null;
        for (ContentLoaderBackend<InternalType> loader : getBackends()) {
            if (loader.isBackendPresent()) {
                result = loader.load(data, fragment, errors);
                if (result != null) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Returns a list of support content loading back-ends.
     * <p>This method should be implemented by all concrete content loaders.
     *
     * @return the list of content loader back-ends
     * @since 1.0
     */
    public abstract List<ContentLoaderBackend<InternalType>> getBackends();
}

