/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestXhtmlContentLoader {
    @Test
    void testLoad() {
        var loader = new XhtmlContentLoader();
        Set<String> errors = new HashSet<>();

        var xhtml = loader.load("<p>some <b>html</b> here</p>", true, errors);

        assertNotNull(xhtml);
        assertEquals(0, errors.size());
    }

    @Test
    void testLoadNull() {
        var loader = new XhtmlContentLoader();
        Set<String> errors = new HashSet<>();

        var xhtml = loader.load(null, false, errors);

        assertNull(xhtml);
        assertEquals(0, errors.size());
    }

    public void getBackends() {
        var loader = new XhtmlContentLoader();
        assertTrue(loader.getBackends().size() > 0);
    }
}
