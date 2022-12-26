/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestContentInfo {
    @Test
    public void testInstantiation() {
        var contentinfo = new ContentInfo();
        assertNotNull(contentinfo);

        assertNull(contentinfo.getPath());
        assertEquals(-1, contentinfo.getVersion());
        assertNull(contentinfo.getCreated());
        assertNull(contentinfo.getMimeType());
        assertFalse(contentinfo.isFragment());
        assertFalse(contentinfo.hasName());
        assertNull(contentinfo.getName());
        assertNull(contentinfo.getAttributes());
        assertFalse(contentinfo.hasAttributes());
        assertFalse(contentinfo.hasAttribute("attr1"));
        assertNull(contentinfo.getAttribute("attr1"));
        assertEquals(-1, contentinfo.getSize());
        assertFalse(contentinfo.hasProperties());
        assertNull(contentinfo.getProperties());
        assertFalse(contentinfo.hasProperty("some prop"));
        assertNull(contentinfo.getProperty("some prop"));
    }

    @Test
    public void testPath() {
        var contentinfo = new ContentInfo();
        contentinfo.setPath("/my/path");
        assertEquals("/my/path", contentinfo.getPath());
    }

    @Test
    public void testVersion() {
        var contentinfo = new ContentInfo();
        contentinfo.setVersion(5);
        assertEquals(5, contentinfo.getVersion());
    }

    @Test
    public void testMimeType() {
        var contentinfo = new ContentInfo();
        contentinfo.setMimeType(MimeType.IMAGE_GIF.toString());
        assertEquals(MimeType.IMAGE_GIF.toString(), contentinfo.getMimeType());
    }

    @Test
    public void testFragment() {
        var contentinfo = new ContentInfo();
        contentinfo.setFragment(true);
        assertTrue(contentinfo.isFragment());
        contentinfo.setFragment(false);
        assertFalse(contentinfo.isFragment());
    }

    @Test
    public void testCreated() {
        var contentinfo = new ContentInfo();
        var ts = new Timestamp(System.currentTimeMillis());
        contentinfo.setCreated(ts);
        assertEquals(ts, contentinfo.getCreated());
    }

    @Test
    public void testName() {
        var contentinfo = new ContentInfo();
        contentinfo.setName("myname.gif");
        assertTrue(contentinfo.hasName());
        assertEquals("myname.gif", contentinfo.getName());
        contentinfo.setName(null);
        assertFalse(contentinfo.hasName());
        assertNull(contentinfo.getName());
    }

    @Test
    public void testOptimalPath() {
        var contentinfo = new ContentInfo();
        contentinfo.setPath("/some/path");
        assertEquals("/some/path", contentinfo.getOptimalPath());
        contentinfo.setName("myname.gif");
        assertEquals("/some/path/myname.gif", contentinfo.getOptimalPath());
        contentinfo.setPath(null);
        assertNull(contentinfo.getOptimalPath());
    }

    @Test
    public void testSetAttributes() {
        var contentinfo = new ContentInfo();
        var attrs = new HashMap<String, String>();
        attrs.put("attr1", "val1");
        attrs.put("attr2", "val2");
        attrs.put("attr3", "val3");

        contentinfo.setAttributes(attrs);
        assertTrue(contentinfo.hasAttributes());
        assertSame(contentinfo.getAttributes(), attrs);
        assertEquals(contentinfo.getAttributes().size(), attrs.size());
        assertTrue(contentinfo.getAttributes().containsKey("attr1"));
        assertTrue(contentinfo.getAttributes().containsKey("attr2"));
        assertTrue(contentinfo.getAttributes().containsKey("attr3"));
        assertTrue(contentinfo.hasAttribute("attr1"));
        assertTrue(contentinfo.hasAttribute("attr2"));
        assertTrue(contentinfo.hasAttribute("attr3"));
        assertEquals("val1", contentinfo.getAttribute("attr1"));
        assertEquals("val2", contentinfo.getAttribute("attr2"));
        assertEquals("val3", contentinfo.getAttribute("attr3"));

        attrs.remove("attr2");

        contentinfo.setAttributes(attrs);
        assertTrue(contentinfo.hasAttributes());
        assertSame(contentinfo.getAttributes(), attrs);
        assertEquals(contentinfo.getAttributes().size(), 2);
        assertTrue(contentinfo.getAttributes().containsKey("attr1"));
        assertFalse(contentinfo.getAttributes().containsKey("attr2"));
        assertTrue(contentinfo.getAttributes().containsKey("attr3"));
        assertTrue(contentinfo.hasAttribute("attr1"));
        assertFalse(contentinfo.hasAttribute("attr2"));
        assertTrue(contentinfo.hasAttribute("attr3"));
        assertEquals("val1", contentinfo.getAttribute("attr1"));
        assertNull(contentinfo.getAttribute("attr2"));
        assertEquals("val3", contentinfo.getAttribute("attr3"));

        contentinfo.setAttributes(null);
        assertFalse(contentinfo.hasAttributes());
        assertNull(contentinfo.getAttributes());
    }

    @Test
    public void testSize() {
        var contentinfo = new ContentInfo();
        contentinfo.setSize(2434);
        assertEquals(2434, contentinfo.getSize());
    }

    @Test
    public void testFormattedSize() {
        var contentinfo = new ContentInfo();
        contentinfo.setSize(90);
        assertEquals("0.09KB", contentinfo.getFormattedSize());
        contentinfo.setSize((int) (12.78 * 1024));
        assertEquals("12.78KB", contentinfo.getFormattedSize());
        contentinfo.setSize((int) (101.23 * 1024));
        assertEquals("101KB", contentinfo.getFormattedSize());
        contentinfo.setSize((int) (278.15 * 1024 * 1024));
        assertEquals("278.15MB", contentinfo.getFormattedSize());
        contentinfo.setSize((int) (1.76 * 1024 * 1024 * 1024));
        assertEquals("1.76GB", contentinfo.getFormattedSize());
    }

    @Test
    public void testProperties() {
        var contentinfo = new ContentInfo();
        var props = new HashMap<String, String>();
        props.put("prop1", "val1");
        props.put("prop2", "val2");
        props.put("prop3", "val3");

        contentinfo.setProperties(props);
        assertTrue(contentinfo.hasProperties());
        assertSame(contentinfo.getProperties(), props);
        assertEquals(contentinfo.getProperties().size(), props.size());
        assertTrue(contentinfo.getProperties().containsKey("prop1"));
        assertTrue(contentinfo.getProperties().containsKey("prop2"));
        assertTrue(contentinfo.getProperties().containsKey("prop3"));
        assertTrue(contentinfo.hasProperty("prop1"));
        assertTrue(contentinfo.hasProperty("prop2"));
        assertTrue(contentinfo.hasProperty("prop3"));
        assertEquals("val1", contentinfo.getProperty("prop1"));
        assertEquals("val2", contentinfo.getProperty("prop2"));
        assertEquals("val3", contentinfo.getProperty("prop3"));

        props.remove("prop2");

        contentinfo.setProperties(props);
        assertTrue(contentinfo.hasProperties());
        assertSame(contentinfo.getProperties(), props);
        assertEquals(contentinfo.getProperties().size(), 2);
        assertTrue(contentinfo.getProperties().containsKey("prop1"));
        assertFalse(contentinfo.getProperties().containsKey("prop2"));
        assertTrue(contentinfo.getProperties().containsKey("prop3"));
        assertTrue(contentinfo.hasProperty("prop1"));
        assertFalse(contentinfo.hasProperty("prop2"));
        assertTrue(contentinfo.hasProperty("prop3"));
        assertEquals("val1", contentinfo.getProperty("prop1"));
        assertNull(contentinfo.getProperty("prop2"));
        assertEquals("val3", contentinfo.getProperty("prop3"));

        contentinfo.setProperties(null);
        assertFalse(contentinfo.hasProperties());
        assertNull(contentinfo.getProperties());
    }

    @Test
    public void testValidation() {
        var contentinfo = new ContentInfo();

        contentinfo.resetValidation();
        assertFalse(contentinfo.validate());
        assertFalse(contentinfo.isSubjectValid("path"));
        assertFalse(contentinfo.isSubjectValid("mimeType"));
        assertFalse(contentinfo.isSubjectValid("version"));
        assertFalse(contentinfo.isSubjectValid("created"));

        contentinfo.resetValidation();
        contentinfo.setPath("/some/other/path");
        assertFalse(contentinfo.validate());
        assertTrue(contentinfo.isSubjectValid("path"));

        contentinfo.resetValidation();
        contentinfo.setMimeType("");
        assertFalse(contentinfo.validate());
        assertFalse(contentinfo.isSubjectValid("mimeType"));
        contentinfo.resetValidation();
        contentinfo.setMimeType("01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        assertFalse(contentinfo.validate());
        assertFalse(contentinfo.isSubjectValid("mimeType"));
        contentinfo.resetValidation();
        contentinfo.setMimeType(MimeType.APPLICATION_XHTML.toString());
        assertFalse(contentinfo.validate());
        assertTrue(contentinfo.isSubjectValid("mimeType"));

        contentinfo.resetValidation();
        contentinfo.setVersion(5);
        assertFalse(contentinfo.validate());
        assertTrue(contentinfo.isSubjectValid("version"));

        contentinfo.resetValidation();
        contentinfo.setCreated(new Timestamp(System.currentTimeMillis()));
        assertTrue(contentinfo.validate());
        assertTrue(contentinfo.isSubjectValid("created"));

        contentinfo.resetValidation();
        assertTrue(contentinfo.validate());
        assertTrue(contentinfo.isSubjectValid("path"));
        assertTrue(contentinfo.isSubjectValid("mimeType"));
        assertTrue(contentinfo.isSubjectValid("version"));
        assertTrue(contentinfo.isSubjectValid("created"));
    }
}


