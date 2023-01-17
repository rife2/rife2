/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.*;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class TestMemoryResources {
    private final String RESOURCE = """
        This just contains some text to
        verify if

        resources can be found

        and
        read

        correctly.
        """;
    private final String RESOURCE_UTF8 = """
        This just contains some text to
        verify if

        resources can be found

        and
        read

        correctly.
        Here are some encoding-specific chars : ¡¢£¥§¨©ª«¬®.
        """;

    MemoryResources resource_finder;

    @BeforeEach
    protected void setup() {
        resource_finder = new MemoryResources();
        resource_finder.addResource("resources/test.txt", RESOURCE);
        resource_finder.addResource("resources/test-utf8.txt", RESOURCE_UTF8);
    }

    @Test
    void testAddResource() {
        resource_finder.addResource("just/some/resource", "the content of this resource\nyes it's there");
        assertNotNull(resource_finder.getResource("just/some/resource"));
    }

    @Test
    void testGetUnknownResource() {
        assertNull(resource_finder.getResource("this/resource/doesnt/exist.txt"));
    }

    @Test
    void testGetResourceByName() {
        assertNotNull(resource_finder.getResource("resources/test.txt"));
    }

    @Test
    void testUpdateResource() {
        try {
            var content1 = "the content of this resource\nyes it's there";
            var content2 = "the content of this resource has been modified";

            resource_finder.addResource("resources/test.txt", content1);

            var result1 = resource_finder.getContent("resources/test.txt");
            var time1 = resource_finder.getModificationTime("resources/test.txt");
            assertEquals(content1, result1);

            Thread.sleep(100);

            assertTrue(resource_finder.updateResource("resources/test.txt", content2));

            var result2 = resource_finder.getContent("resources/test.txt");
            var time2 = resource_finder.getModificationTime("resources/test.txt");
            assertEquals(content2, result2);

            assertTrue(time1 != time2);
        } catch (ResourceFinderErrorException | InterruptedException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testUpdateMissingResource() {
        try {
            assertNull(resource_finder.getContent("resources/test_blah.txt"));
            assertFalse(resource_finder.updateResource("resources/test_blah.txt", "blah"));
            assertNull(resource_finder.getContent("resources/test_blah.txt"));
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testRemoveResource() {
        try {
            assertNotNull(resource_finder.getContent("resources/test.txt"));
            assertTrue(resource_finder.removeResource("resources/test.txt"));
            assertNull(resource_finder.getContent("resources/test.txt"));
            assertFalse(resource_finder.removeResource("resources/test.txt"));
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownStreamByName() {
        try {
            resource_finder.useStream("this/resource/doesnt/exist.txt", new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNull(stream);

                    return null;
                }
            });
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownStreamByResource() {
        try {
            resource_finder.useStream(new URL("file://this/resource/doesnt/exist.txt"), new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNull(stream);

                    return null;
                }
            });
        } catch (ResourceFinderErrorException | MalformedURLException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetStreamByName() {
        try {
            resource_finder.useStream("resources/test.txt", new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals(RESOURCE, FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
                    }
                    return null;
                }
            });
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetStreamByResource() {
        var resource = resource_finder.getResource("resources/test.txt");
        try {
            resource_finder.useStream(resource, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals(RESOURCE, FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
                    }
                    return null;
                }
            });
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownContentByName() {
        try {
            var content = resource_finder.getContent("this/resource/doesnt/exist.txt");
            assertNull(content);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownContentByResource() {
        try {
            var content = resource_finder.getContent(new URL("file://this/resource/doesnt/exist.txt"));
            assertNull(content);
        } catch (ResourceFinderErrorException | MalformedURLException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetContentByName() {
        try {
            var content = resource_finder.getContent("resources/test.txt");
            assertNotNull(content);
            assertEquals(content, RESOURCE);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetContentByResource() {
        var resource = resource_finder.getResource("resources/test.txt");
        try {
            var content = resource_finder.getContent(resource);
            assertNotNull(content);
            assertEquals(content, RESOURCE);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetContentByNameAndEncoding() {
        try {
            var content = resource_finder.getContent("resources/test-utf8.txt", "UTF-8");
            assertNotNull(content);
            assertEquals(content, RESOURCE_UTF8);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetContentByResourceAndEncoding() {
        var resource = resource_finder.getResource("resources/test-utf8.txt");
        var content = resource_finder.getContent(resource, "UTF-8");
        assertNotNull(content);
        assertEquals(content, RESOURCE_UTF8);
    }

    @Test
    void testGetUnknownModificationTimeByName() {
        try {
            var time = resource_finder.getModificationTime("this/resource/doesnt/exist.txt");
            assertEquals(-1, time);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownModificationTimeByResource() {
        try {
            var time = resource_finder.getModificationTime(new URL("file://this/resource/doesnt/exist.txt"));
            assertEquals(-1, time);
        } catch (MalformedURLException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetModificationTimeByName() {
        try {
            var time = resource_finder.getModificationTime("resources/test.txt");
            assertTrue(time != -1);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetModificationTimeByResource() {
        var resource = resource_finder.getResource("resources/test.txt");
        var time = resource_finder.getModificationTime(resource);
        assertTrue(time != -1);
    }
}
