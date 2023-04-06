/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader;

import rife.cmf.dam.exceptions.ContentManagerException;

import java.util.Set;

/**
 * This is an abstract class that should be implemented by all xhtml content
 * loader back-ends.
 * <p>The {@link #load(Object, boolean, Set) load} method simply checks the
 * type of the data and delegates the handling to typed methods that should be
 * implemented by the back-ends.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class XhtmlContentLoaderBackend implements ContentLoaderBackend<String> {
    /**
     * Loads the data from a string
     *
     * @param data     the raw data that has to be loaded
     * @param fragment {@code true} if the raw data is a fragment; or
     *                 <p>{@code false} if the raw data is a complete document or file
     * @param errors   a set to which possible error messages will be added
     * @return an instance of the {@code LoadedContent} as XHTML as a {@code String}; or
     * <p>{@code null} if the raw data couldn't be loaded
     *
     * @since 1.4
     */
    protected abstract LoadedContent<String> loadFromString(String data, boolean fragment, Set<String> errors)
    throws ContentManagerException;

    public LoadedContent<String> load(Object data, boolean fragment, Set<String> errors)
    throws ContentManagerException {
        if (data instanceof String str) {
            return loadFromString(str, fragment, errors);
        }

        return null;
    }
}
