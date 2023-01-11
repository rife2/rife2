/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.transform;

import rife.cmf.dam.exceptions.ContentManagerException;

import java.awt.Image;
import java.util.Map;

/**
 * This interface defines the API that has to be implemented by classes that
 * are capable of transforming {@link java.awt.Image image} content data after
 * it's initially loaded.
 * <p>The content attributes are provided to the {@link
 * #transform(Image data, Map attributes) transform} method and can be used to
 * provide hints for the transformation.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ContentTransformer
 * @since 1.0
 */
public interface ImageContentTransformer extends ContentTransformer<Image> {
    /**
     * Transforms the {@link java.awt.Image image} content data and returns
     * the transformed data as an {@code Image}.
     *
     * @param data       the image that has to be transformed
     * @param attributes a map of content attributes that can be used to
     *                   provide hints or parameters for the transformation
     * @return the transformed image
     * @since 1.0
     */
    Image transform(Image data, Map<String, String> attributes)
    throws ContentManagerException;
}
