/*
 * Copyright 2001-2008 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.tools;

import org.junit.jupiter.api.Test;
import rife.resources.ResourceFinderClasspath;
import rife.resources.exceptions.ResourceFinderErrorException;

import static org.junit.jupiter.api.Assertions.*;

public class TestResourceFinderClasspath {
    @Test public void testInstantiation() {
        ResourceFinderClasspath rf = ResourceFinderClasspath.instance();
        assertNotNull(rf);
    }

    @Test
    public void testSingleton() {
        ResourceFinderClasspath rf1 = ResourceFinderClasspath.instance();
        assertNotNull(rf1);
        ResourceFinderClasspath rf2 = ResourceFinderClasspath.instance();
        assertNotNull(rf2);
        assertSame(rf1, rf2);
    }

    @Test public void testModificationTime() {
        ResourceFinderClasspath rf = ResourceFinderClasspath.instance();
        try {
            assertTrue(rf.getModificationTime("java/lang/Class.class") > 0);
        } catch (ResourceFinderErrorException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
