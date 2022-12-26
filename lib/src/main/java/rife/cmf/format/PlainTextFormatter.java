/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format;

import rife.cmf.Content;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.format.exceptions.InvalidContentDataTypeException;
import rife.cmf.transform.ContentTransformer;

/**
 * Formats plain test <code>Content</code> data.
 * <p>This merely executes the provided transformer on the data.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Formatter
 * @since 1.0
 */
public class PlainTextFormatter implements Formatter<String, String> {
    public String format(Content content, ContentTransformer<String> transformer)
    throws FormatException {
        if (!(content.getData() instanceof String data)) {
            throw new InvalidContentDataTypeException(this, content.getMimeType(), String.class, content.getData().getClass());
        }

        // transform the content, if needed
        if (transformer != null) {
            data = transformer.transform(data, content.getAttributes());
        }

        return data;
    }
}

