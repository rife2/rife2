/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.transform;

import rife.cmf.dam.exceptions.ContentManagerException;

import java.io.InputStream;
import java.util.Map;

/**
 * This interface defines the API that has to be implemented by classes that
 * are capable of transforming raw content data after it's initially loaded.
 * <p>The content attributes are provided to the {@link
 * #transform(InputStream data, Map attributes) transform} method and can be
 * used to provide hints for the transformation.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ContentTransformer
 * @since 1.0
 */
public interface RawContentTransformer extends ContentTransformer<InputStream> {
    /**
     * Transforms the raw content data and returns the transformed data as an
     * array of bytes.
     *
     * @param data       the raw data that has to be transformed
     * @param attributes a map of content attributes that can be used to
     *                   provide hints or parameters for the transformation
     * @return the transformed data
     * @since 1.0
     */
    InputStream transform(InputStream data, Map<String, String> attributes)
    throws ContentManagerException;
}
