/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format;

import rife.cmf.Content;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.transform.ContentTransformer;

/**
 * Formats raw data according to the information that's provided by a {@link
 * rife.cmf.Content Content} instance. The raw data will be
 * loaded, optionally transformed and eventually returned.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface Formatter<DataType, InternalType> {
    /**
     * Formats raw data and returns it in a <code>DataType</code> that's
     * supported by the back-end stores.
     *
     * @param content     a <code>Content</code> instance that contains the raw
     *                    data with additional information that describes the storage and
     *                    formatting of the processed data
     * @param transformer a transformer that will be used to modify raw data
     *                    after it has been loaded; or
     *                    <p><code>null</code> if the data shouldn't be transformed
     * @return the result of the formatting of the raw data
     * @since 1.0
     */
    DataType format(Content content, ContentTransformer<InternalType> transformer)
    throws FormatException;
}

