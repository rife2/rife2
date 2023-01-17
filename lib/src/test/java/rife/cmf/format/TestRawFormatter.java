/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format;

import org.junit.jupiter.api.Test;
import rife.cmf.Content;
import rife.cmf.MimeType;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.format.exceptions.InvalidContentDataTypeException;
import rife.cmf.transform.RawContentTransformer;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestRawFormatter {
    @Test
    void testFormatBasic()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.RAW, new ByteArrayInputStream(data_image_gif));
        var formatter = new RawFormatter();
        var result = (InputStream) formatter.format(content, null);

        assertNotNull(result);

        assertArrayEquals(data_image_gif, FileUtils.readBytes(result));
    }

    @Test
    void testFormatInvalidDataType()
    throws Exception {
        var content = new Content(MimeType.RAW, new byte[1]);
        var formatter = new RawFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (InvalidContentDataTypeException e) {
            assertSame(InputStream.class, e.getExpectedType());
            assertSame(formatter, e.getFormatter());
            assertSame(MimeType.RAW, e.getMimeType());
            assertSame(byte[].class, e.getReceivedType());
        }
    }

    @Test
    void testFormatTransformer()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.RAW, new ByteArrayInputStream(data_image_gif));
        var formatter = new RawFormatter();
        var result = (InputStream) formatter.format(content, new TransparentRawTransformer());

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, FileUtils.readBytes(result));
    }

    static class TransparentRawTransformer implements RawContentTransformer {
        public InputStream transform(InputStream data, Map<String, String> attributes)
        throws ContentManagerException {
            try {
                var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                var data_image_png = FileUtils.readBytes(image_resource_png);

                return new ByteArrayInputStream(data_image_png);
            } catch (FileUtilsErrorException e) {
                return null;
            }
        }
    }
}
