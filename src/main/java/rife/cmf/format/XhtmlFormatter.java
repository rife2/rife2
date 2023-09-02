/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format;

import rife.cmf.Content;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.format.exceptions.InvalidContentDataTypeException;
import rife.cmf.format.exceptions.UnreadableDataFormatException;
import rife.cmf.loader.LoadedContent;
import rife.cmf.loader.XhtmlContentLoader;
import rife.cmf.transform.ContentTransformer;
import rife.tools.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Formats raw {@code Content} data as valid Xhtml.
 * <p>No content attributes are supported:
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Formatter
 * @since 1.0
 */
public class XhtmlFormatter implements Formatter<String, String> {
    @Override
    public String format(Content content, ContentTransformer<String> transformer)
    throws FormatException {
        if (!(content.getData() instanceof String)) {
            throw new InvalidContentDataTypeException(this, content.getMimeType(), String.class, content.getData().getClass());
        }

        LoadedContent<String> loaded = null;
        String data = null;

        // check if the content contains a cached value of the loaded data
        if (content.hasCachedLoadedData()) {
            var cached = content.getCachedLoadedData();
            if (cached instanceof LoadedContent<?> cached_loaded) {
                loaded = (LoadedContent<String>) cached_loaded;
                data = loaded.data();
            } else {
                data = (String) cached;
            }
        }

        if (null == data) {
            // get an image
            Set<String> errors = new HashSet<>();
            loaded = new XhtmlContentLoader().load(content.getData(), content.isFragment(), errors);
            if (null == loaded) {
                throw new UnreadableDataFormatException(content.getMimeType(), errors);
            }

            data = loaded.data();
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

