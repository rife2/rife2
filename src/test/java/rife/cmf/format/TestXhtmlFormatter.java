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
import rife.cmf.format.exceptions.UnreadableDataFormatException;
import rife.cmf.loader.XhtmlContentLoader;
import rife.cmf.transform.TextContentTransformer;
import rife.tools.StringUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestXhtmlFormatter {
    @Test
    void testFormatBasic()
    throws Exception {
        var data = """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html><head><title>my title</title></head><body><p>some text\s
            <i>here</i> and <b>there</b></p></body></html>""";
        var content = new Content(MimeType.APPLICATION_XHTML, data);
        var formatter = new XhtmlFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        assertEquals(data, result);
    }

    @Test
    void testFormatInvalidDataType()
    throws Exception {
        var content = new Content(MimeType.APPLICATION_XHTML, new Object());
        var formatter = new XhtmlFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (InvalidContentDataTypeException e) {
            assertSame(String.class, e.getExpectedType());
            assertSame(formatter, e.getFormatter());
            assertSame(MimeType.APPLICATION_XHTML, e.getMimeType());
            assertSame(Object.class, e.getReceivedType());
        }
    }

    @Test
    void testFormatCachedLoadedData()
    throws Exception {
        var data = "<p>some text <i>here</i> and <b>there</b></p>";
        var content = new Content(MimeType.IMAGE_PNG, data);
        var xhtml = new XhtmlContentLoader().load(data, true, null);
        content.setCachedLoadedData(xhtml);

        var formatter = new XhtmlFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        assertEquals(data, result);
    }

    @Test
    void testFormatUnreadableData()
    throws Exception {
        var content = new Content(MimeType.APPLICATION_XHTML, "<p>some text <i>here</b> and <b>there</i></blurp>");
        var formatter = new XhtmlFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (UnreadableDataFormatException e) {
            assertSame(MimeType.APPLICATION_XHTML, e.getMimeType());
            assertFalse(e.getErrors().isEmpty());
        }
    }

    @Test
    void testFormatTransformer()
    throws Exception {
        var data = "<p>some text <i>here</i> and <b>there</b></p>";
        var content = new Content(MimeType.APPLICATION_XHTML, data)
            .fragment(true);
        var formatter = new XhtmlFormatter();
        var result = formatter.format(content, new XhtmlTransformer());

        assertNotNull(result);

        var transformed = "<p>some text <i>here</i> and <b>at home</b></p>";
        assertEquals(transformed, result);
    }

    static class XhtmlTransformer implements TextContentTransformer {
        public String transform(String data, Map<String, String> attributes)
        throws ContentManagerException {
            return StringUtils.replace(data, "there", "at home");
        }
    }
}
