/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader;

import rife.cmf.dam.exceptions.ContentManagerException;

import java.awt.*;
import java.util.Set;

/**
 * This is an abstract class that should be implemented by all image content
 * loader back-ends.
 * <p>The {@link #load(Object, boolean, Set) load} method simply checks the
 * type of the data and delegates the handling to typed methods that should be
 * implemented by the back-ends.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class ImageContentLoaderBackend implements ContentLoaderBackend<Image> {
    /**
     * Loads the data from a byte array.
     *
     * @param data   the raw data that has to be loaded
     * @param errors a set to which possible error messages will be added
     * @return an instance of the {@code LoadedContent} with {@code Image} data; or
     * <p>{@code null} if the raw data couldn't be loaded
     */
    protected abstract LoadedContent<Image> loadFromBytes(byte[] data, Set<String> errors)
    throws ContentManagerException;

    @Override
    public LoadedContent<Image> load(Object data, boolean fragment, Set<String> errors)
    throws ContentManagerException {
        if (data instanceof byte[] bytes) {
            return loadFromBytes(bytes, errors);
        }

        return null;
    }
}
