/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader.xhtml;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestSAXLoader {
    @Test
    void testIsBackendPresent() {
        var loader = new SAXLoader();
        assertTrue(loader.isBackendPresent());
    }

    @Test
    void testLoadSuccess() {
        var loader = new SAXLoader();
        Set<String> errors = new HashSet<>();

        var xhtml = loader.load("<p>some <b>html</b> here</p>", true, errors);

        assertNotNull(xhtml);
        assertEquals(0, errors.size());
    }

    @Test
    void testLoadUnsupportedType() {
        var loader = new SAXLoader();
        Set<String> errors = new HashSet<>();

        var xhtml = loader.load(new Object(), true, errors);

        assertNull(xhtml);
        assertEquals(0, errors.size());
    }

    @Test
    void testLoadFromStringSuccessFragment() {
        var loader = new SAXLoader();
        Set<String> errors = new HashSet<>();

        var xhtml = loader.loadFromString("<p>some <b>html</b> here</p>", true, errors);

        assertNotNull(xhtml);
        assertEquals(0, errors.size());
    }

    @Test
    void testLoadFromStringSuccessComplete() {
        var loader = new SAXLoader();
        Set<String> errors = new HashSet<>();

        var xhtml = loader.loadFromString("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml"><head><title></title></head><body>
            <p>body</p>
            </body></html>""", false, errors);

        assertNotNull(xhtml);
        assertEquals(0, errors.size());
    }

    @Test
    void testLoadFromStringError() {
        var loader = new SAXLoader();
        Set<String> errors = new HashSet<>();

        var xhtml = loader.loadFromString("<i><b>error</i>", true, errors);

        assertNull(xhtml);
        assertEquals(1, errors.size());
    }

    @Test
    void testLoadFromStringErrorNoList() {
        var loader = new SAXLoader();

        var xhtml = loader.loadFromString("<i><b>error</i>", true, null);

        assertNull(xhtml);
    }
}
