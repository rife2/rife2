/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader;

import org.junit.jupiter.api.Test;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestImageContentLoader {
    @Test
    void testLoad()
    throws Exception {
        var loader = new ImageContentLoader();
        Set<String> errors = new HashSet<>();
        var image_resource = ResourceFinderClasspath.instance().getResource("uwyn.png");

        var image_bytes = FileUtils.readBytes(image_resource);
        var image = loader.load(image_bytes, false, errors);

        assertNotNull(image);
        assertEquals(0, errors.size());
    }

    @Test
    void testLoadNull() {
        var loader = new ImageContentLoader();
        Set<String> errors = new HashSet<>();

        var image = loader.load(null, false, errors);

        assertNull(image);
        assertEquals(0, errors.size());
    }

    @Test
    void testGetBackends() {
        var loader = new ImageContentLoader();
        assertTrue(!loader.getBackends().isEmpty());
    }
}
