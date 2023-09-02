/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.transform;

import java.util.Map;

/**
 * This interface defines the API that has to be implemented by classes that
 * are capable of transforming {@link java.lang.String text} content data
 * after it's initially loaded.
 * <p>The content attributes are provided to the {@link
 * #transform(String data, Map attributes) transform} method and can be used
 * to provide hints for the transformation.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ContentTransformer
 * @since 1.0
 */
public interface TextContentTransformer extends ContentTransformer<String> {
    /**
     * Transforms the {@link java.lang.String text} content data and returns
     * the transformed data as text.
     *
     * @param data       the text that has to be transformed
     * @param attributes a map of content attributes that can be used to
     *                   provide hints or parameters for the transformation
     * @return the transformed text
     * @since 1.0
     */
    @Override
    String transform(String data, Map<String, String> attributes);
}
