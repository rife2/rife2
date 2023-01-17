/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestContent {
    @Test
    void testInstantiation() {
        var content = new Content(MimeType.APPLICATION_XHTML, "<html></html>");
        assertNotNull(content);

        assertSame(content.getMimeType(), MimeType.APPLICATION_XHTML);
        assertEquals(content.getData(), "<html></html>");
        assertFalse(content.isFragment());
        assertNull(content.getAttributes());
        assertFalse(content.hasName());
        assertNull(content.getName());
        assertFalse(content.hasAttributes());
        assertFalse(content.hasAttribute("attr1"));
        assertNull(content.getAttribute("attr1"));
        assertFalse(content.hasCachedLoadedData());
        assertNull(content.getCachedLoadedData());
        assertFalse(content.hasProperties());
        assertNull(content.getProperties());
        assertFalse(content.hasProperty("some prop"));
        assertNull(content.getProperty("some prop"));
    }

    @Test
    void testInstantiationNullData() {
        var content = new Content(MimeType.APPLICATION_XHTML, null);
        assertNotNull(content);

        assertSame(content.getMimeType(), MimeType.APPLICATION_XHTML);
        assertNull(content.getData());
        assertFalse(content.isFragment());
        assertFalse(content.hasName());
        assertNull(content.getName());
        assertNull(content.getAttributes());
        assertFalse(content.hasAttributes());
        assertFalse(content.hasAttribute("attr1"));
        assertNull(content.getAttribute("attr1"));
        assertFalse(content.hasCachedLoadedData());
        assertNull(content.getCachedLoadedData());
        assertFalse(content.hasProperties());
        assertNull(content.getProperties());
        assertFalse(content.hasProperty("some prop"));
        assertNull(content.getProperty("some prop"));
    }

    @Test
    void testInvalidMimeType() {
        Content content = null;
        try {
            content = new Content(null, "<html></html>");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().indexOf("mimeType") != -1);
        }

        assertNull(content);
    }

    @Test
    void testFragment() {
        var content = new Content(MimeType.APPLICATION_XHTML, "<html></html>");
        content.setFragment(true);
        assertTrue(content.isFragment());
        assertSame(content, content.fragment(false));
        assertFalse(content.isFragment());
    }

    @Test
    void testName() {
        var content = new Content(MimeType.APPLICATION_XHTML, "<html></html>");
        content.setName("myname.gif");
        assertTrue(content.hasName());
        assertEquals("myname.gif", content.getName());
        assertSame(content, content.name("anothername.png"));
        assertTrue(content.hasName());
        assertEquals("anothername.png", content.getName());
        content.name(null);
        assertFalse(content.hasName());
        assertNull(content.getName());
    }

    @Test
    void testSetAttributes() {
        var content = new Content(MimeType.APPLICATION_XHTML, "<html></html>");
        var attrs = new HashMap<String, String>();
        attrs.put("attr1", "val1");
        attrs.put("attr2", "val2");
        attrs.put("attr3", "val3");

        content.setAttributes(attrs);
        assertTrue(content.hasAttributes());
        assertNotSame(content.getAttributes(), attrs);
        assertEquals(content.getAttributes().size(), attrs.size());
        assertTrue(content.getAttributes().containsKey("attr1"));
        assertTrue(content.getAttributes().containsKey("attr2"));
        assertTrue(content.getAttributes().containsKey("attr3"));
        assertTrue(content.hasAttribute("attr1"));
        assertTrue(content.hasAttribute("attr2"));
        assertTrue(content.hasAttribute("attr3"));
        assertEquals("val1", content.getAttribute("attr1"));
        assertEquals("val2", content.getAttribute("attr2"));
        assertEquals("val3", content.getAttribute("attr3"));

        attrs.remove("attr2");

        content.attributes(attrs);
        assertTrue(content.hasAttributes());
        assertNotSame(content.getAttributes(), attrs);
        assertEquals(content.getAttributes().size(), 2);
        assertTrue(content.getAttributes().containsKey("attr1"));
        assertFalse(content.getAttributes().containsKey("attr2"));
        assertTrue(content.getAttributes().containsKey("attr3"));
        assertTrue(content.hasAttribute("attr1"));
        assertFalse(content.hasAttribute("attr2"));
        assertTrue(content.hasAttribute("attr3"));
        assertEquals("val1", content.getAttribute("attr1"));
        assertNull(content.getAttribute("attr2"));
        assertEquals("val3", content.getAttribute("attr3"));

        content.setAttributes(null);
        assertFalse(content.hasAttributes());
        assertNull(content.getAttributes());
    }

    @Test
    void testSetAttribute() {
        var content = new Content(MimeType.APPLICATION_XHTML, "<html></html>");

        assertSame(content, content
            .attribute("attr1", false)
            .attribute("attr2", 'O')
            .attribute("attr3", (byte) 89)
            .attribute("attr4", (short) 120)
            .attribute("attr5", 19843)
            .attribute("attr6", 3847934L)
            .attribute("attr7", 343.9f)
            .attribute("attr8", 3487.343d)
            .attribute("attr9", "value"));

        assertTrue(content.hasAttributes());
        assertEquals(content.getAttributes().size(), 9);
        assertTrue(content.hasAttribute("attr1"));
        assertTrue(content.hasAttribute("attr2"));
        assertTrue(content.hasAttribute("attr3"));
        assertTrue(content.hasAttribute("attr4"));
        assertTrue(content.hasAttribute("attr5"));
        assertTrue(content.hasAttribute("attr6"));
        assertTrue(content.hasAttribute("attr7"));
        assertTrue(content.hasAttribute("attr8"));
        assertTrue(content.hasAttribute("attr9"));
        assertEquals("false", content.getAttribute("attr1"));
        assertEquals("O", content.getAttribute("attr2"));
        assertEquals("89", content.getAttribute("attr3"));
        assertEquals("120", content.getAttribute("attr4"));
        assertEquals("19843", content.getAttribute("attr5"));
        assertEquals("3847934", content.getAttribute("attr6"));
        assertEquals("343.9", content.getAttribute("attr7"));
        assertEquals("3487.343", content.getAttribute("attr8"));
        assertEquals("value", content.getAttribute("attr9"));
    }

    @Test
    void testLoadedDataCache() {
        var content = new Content(MimeType.APPLICATION_XHTML, "<html></html>");
        var object = new Object();
        content.setCachedLoadedData(object);
        assertTrue(content.hasCachedLoadedData());
        assertSame(object, content.getCachedLoadedData());
        content.cachedLoadedData(null);
        assertFalse(content.hasCachedLoadedData());
        assertNull(content.getCachedLoadedData());
    }

    @Test
    void testSetProperties() {
        var content = new Content(MimeType.APPLICATION_XHTML, "<html></html>");
        var props = new HashMap<String, String>();
        props.put("prop1", "val1");
        props.put("prop2", "val2");
        props.put("prop3", "val3");

        content.setProperties(props);
        assertTrue(content.hasProperties());
        assertNotSame(content.getProperties(), props);
        assertEquals(content.getProperties().size(), props.size());
        assertTrue(content.getProperties().containsKey("prop1"));
        assertTrue(content.getProperties().containsKey("prop2"));
        assertTrue(content.getProperties().containsKey("prop3"));
        assertTrue(content.hasProperty("prop1"));
        assertTrue(content.hasProperty("prop2"));
        assertTrue(content.hasProperty("prop3"));
        assertEquals("val1", content.getProperty("prop1"));
        assertEquals("val2", content.getProperty("prop2"));
        assertEquals("val3", content.getProperty("prop3"));

        props.remove("prop2");

        content.properties(props);
        assertTrue(content.hasProperties());
        assertNotSame(content.getProperties(), props);
        assertEquals(content.getProperties().size(), 2);
        assertTrue(content.getProperties().containsKey("prop1"));
        assertFalse(content.getProperties().containsKey("prop2"));
        assertTrue(content.getProperties().containsKey("prop3"));
        assertTrue(content.hasProperty("prop1"));
        assertFalse(content.hasProperty("prop2"));
        assertTrue(content.hasProperty("prop3"));
        assertEquals("val1", content.getProperty("prop1"));
        assertNull(content.getProperty("prop2"));
        assertEquals("val3", content.getProperty("prop3"));

        content.setProperties(null);
        assertFalse(content.hasProperties());
        assertNull(content.getProperties());
    }

    @Test
    void testSetProperty() {
        var content = new Content(MimeType.APPLICATION_XHTML, "<html></html>");

        assertSame(content, content
            .property("prop1", false)
            .property("prop2", 'O')
            .property("prop3", (byte) 89)
            .property("prop4", (short) 120)
            .property("prop5", 19843)
            .property("prop6", 3847934L)
            .property("prop7", 343.9f)
            .property("prop8", 3487.343d)
            .property("prop9", "value"));

        assertTrue(content.hasProperties());
        assertEquals(content.getProperties().size(), 9);
        assertTrue(content.hasProperty("prop1"));
        assertTrue(content.hasProperty("prop2"));
        assertTrue(content.hasProperty("prop3"));
        assertTrue(content.hasProperty("prop4"));
        assertTrue(content.hasProperty("prop5"));
        assertTrue(content.hasProperty("prop6"));
        assertTrue(content.hasProperty("prop7"));
        assertTrue(content.hasProperty("prop8"));
        assertTrue(content.hasProperty("prop9"));
        assertEquals("false", content.getProperty("prop1"));
        assertEquals("O", content.getProperty("prop2"));
        assertEquals("89", content.getProperty("prop3"));
        assertEquals("120", content.getProperty("prop4"));
        assertEquals("19843", content.getProperty("prop5"));
        assertEquals("3847934", content.getProperty("prop6"));
        assertEquals("343.9", content.getProperty("prop7"));
        assertEquals("3487.343", content.getProperty("prop8"));
        assertEquals("value", content.getProperty("prop9"));
    }
}
