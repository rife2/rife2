/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;
import rife.cmf.MimeType;
import rife.cmf.dam.contentmanagers.DatabaseContentInfo;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseContentInfo {
    @Test
    public void testInstantiation() {
        var content_info = new DatabaseContentInfo();
        assertNotNull(content_info);

        assertEquals(-1, content_info.getContentId());
        assertNull(content_info.getPath());
        assertEquals(-1, content_info.getVersion());
        assertNull(content_info.getCreated());
        assertNull(content_info.getMimeType());
        assertFalse(content_info.isFragment());
        assertFalse(content_info.hasName());
        assertNull(content_info.getName());
        assertNull(content_info.getAttributes());
        assertFalse(content_info.hasAttributes());
        assertFalse(content_info.hasAttribute("attr1"));
        assertNull(content_info.getAttribute("attr1"));
        assertEquals(-1, content_info.getSize());
        assertFalse(content_info.hasProperties());
        assertNull(content_info.getProperties());
        assertFalse(content_info.hasProperty("some prop"));
        assertNull(content_info.getProperty("some prop"));
    }

    @Test
    public void testContentId() {
        var content_info = new DatabaseContentInfo();
        content_info.setContentId(12);
        assertEquals(12, content_info.getContentId());
    }


    @Test
    public void testValidation() {
        var content_info = new DatabaseContentInfo();

        content_info.resetValidation();
        assertFalse(content_info.validate());
        assertFalse(content_info.isSubjectValid("contentId"));
        assertFalse(content_info.isSubjectValid("path"));
        assertFalse(content_info.isSubjectValid("mimeType"));
        assertFalse(content_info.isSubjectValid("version"));
        assertFalse(content_info.isSubjectValid("created"));

        content_info.resetValidation();
        content_info.setPath("/some/other/path");
        content_info.setMimeType(MimeType.APPLICATION_XHTML.toString());
        content_info.setVersion(5);
        content_info.setCreated(new Timestamp(System.currentTimeMillis()));

        content_info.resetValidation();
        content_info.setContentId(87);
        assertTrue(content_info.validate());
        assertTrue(content_info.isSubjectValid("contentId"));

        content_info.resetValidation();
        assertTrue(content_info.validate());
        assertTrue(content_info.isSubjectValid("contentId"));
        assertTrue(content_info.isSubjectValid("path"));
        assertTrue(content_info.isSubjectValid("mimeType"));
        assertTrue(content_info.isSubjectValid("version"));
        assertTrue(content_info.isSubjectValid("created"));
    }
}


