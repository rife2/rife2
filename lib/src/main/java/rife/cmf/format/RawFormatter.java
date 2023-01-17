/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format;

import rife.cmf.Content;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.format.exceptions.InvalidContentDataTypeException;
import rife.cmf.transform.ContentTransformer;

import java.io.InputStream;

/**
 * Formats raw {@code Content} data.
 * <p>This merely executes the provided transformer on the data.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Formatter
 * @since 1.0
 */
public class RawFormatter implements Formatter<InputStream, InputStream> {
    public InputStream format(Content content, ContentTransformer<InputStream> transformer)
    throws FormatException {
        if (!(content.getData() instanceof InputStream data)) {
            throw new InvalidContentDataTypeException(this, content.getMimeType(), InputStream.class, content.getData().getClass());
        }

        // transform the content, if needed
        if (transformer != null) {
            data = transformer.transform(data, content.getAttributes());
        }

        return data;
    }
}

