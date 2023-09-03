/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import org.junit.jupiter.api.Test;
import rife.tools.InnerClassException;

import static org.junit.jupiter.api.Assertions.*;

public class TestContentDataUser {
    @Test
    void testInstantiation() {
        var user = new ContentDataUser<>() {
            public Object useContentData(Object contentData)
            throws InnerClassException {
                return null;
            }
        };

        assertNotNull(user);
    }

    @Test
    void testData() {
        var user = new ContentDataUser<String>() {
            public String useContentData(Object contentData)
            throws InnerClassException {
                return contentData + " some string";
            }
        };

        assertEquals("the data some string", user.useContentData("the data"));
    }

    @Test
    void testException() {
        var user = new ContentDataUser<>() {
            public Object useContentData(Object contentData)
            throws InnerClassException {
                throwException(new Exception(contentData.toString()));
                return null;
            }
        };

        try {
            user.useContentData("some exception");

            fail();
        } catch (InnerClassException e) {
            assertEquals("some exception", e.getCause().getMessage());
        }
    }
}
