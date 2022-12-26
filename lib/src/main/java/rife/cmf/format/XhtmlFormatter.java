/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format;

import rife.cmf.Content;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.format.exceptions.InvalidContentDataTypeException;
import rife.cmf.format.exceptions.UnreadableDataFormatException;
import rife.cmf.loader.XhtmlContentLoader;
import rife.cmf.transform.ContentTransformer;
import rife.tools.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Formats raw <code>Content</code> data as valid Xhtml.
 * <p>No content attributes are supported:
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Formatter
 * @since 1.0
 */
public class XhtmlFormatter implements Formatter<String, String> {
    public String format(Content content, ContentTransformer<String> transformer)
    throws FormatException {
        if (!(content.getData() instanceof String)) {
            throw new InvalidContentDataTypeException(this, content.getMimeType(), String.class, content.getData().getClass());
        }

        String data = null;

        // check if the content contains a cached value of the loaded data
        if (content.hasCachedLoadedData()) {
            data = (String) content.getCachedLoadedData();
        }

        if (null == data) {
            // get an image
            Set<String> errors = new HashSet<>();
            data = new XhtmlContentLoader().load(content.getData(), content.isFragment(), errors);
            if (null == data) {
                throw new UnreadableDataFormatException(content.getMimeType(), errors);
            }
        }

        // ensure that as much as possible entities are encoded
        data = StringUtils.encodeHtmlDefensive(data);

        // transform the content, if needed
        if (transformer != null) {
            data = transformer.transform(data, content.getAttributes());
        }

        return data;
    }
}

