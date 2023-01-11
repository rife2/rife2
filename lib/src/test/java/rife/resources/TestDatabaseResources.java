/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.resources.exceptions.ResourceWriterErrorException;
import rife.tools.ExceptionUtils;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseResources {
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

    protected void setup(Datasource datasource) {
        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            resource_finder.install();
            resource_finder.addResource("resources/test.txt", RESOURCE);
            resource_finder.addResource("resources/test-utf8.txt", RESOURCE_UTF8);
        } catch (ResourceWriterErrorException e) {
            throw new RuntimeException(e);
        }
    }

    protected void tearDown(Datasource datasource) {
        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            resource_finder.remove();
        } catch (ResourceWriterErrorException e) {
            // that's ok, it's probably already gone
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiation(Datasource datasource) {
        setup(datasource);
        try {
            ResourceFinder resource_finder1 = DatabaseResourcesFactory.instance(datasource);
            ResourceFinder resource_finder2 = DatabaseResourcesFactory.instance(datasource);
            assertNotNull(resource_finder1);
            assertNotNull(resource_finder2);
            assertSame(resource_finder1, resource_finder2);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstall(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            resource_finder.remove();

            resource_finder.install();
            try {
                resource_finder.install();
                fail();
            } catch (ResourceWriterErrorException e) {
                assertTrue(true);
            }
        } catch (ResourceWriterErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemove(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            resource_finder.remove();

            resource_finder.install();
            resource_finder.remove();
            try {
                resource_finder.remove();
                fail();
            } catch (ResourceWriterErrorException e) {
                assertTrue(true);
            }
        } catch (ResourceWriterErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAddResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            resource_finder.addResource("just/some/resource", "the content of this resource\nyes it's there");
        } catch (ResourceWriterErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetUnknownResource(Datasource datasource) {
        setup(datasource);
        try {
            var resource_finder = DatabaseResourcesFactory.instance(datasource);
            assertNull(resource_finder.getResource("this/resource/doesnt/exist.txt"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetResourceByName(Datasource datasource) {
        setup(datasource);
        try {
            var resource_finder = DatabaseResourcesFactory.instance(datasource);
            assertNotNull(resource_finder.getResource("resources/test.txt"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUpdateResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            var content1 = "the content of this resource\nyes it's there";
            var content2 = "the content of this resource has been modified";

            resource_finder.addResource("resources/test.txt", content1);

            var result1 = resource_finder.getContent("resources/test.txt");
            var time1 = resource_finder.getModificationTime("resources/test.txt");
            assertEquals(content1, result1);

            assertTrue(resource_finder.updateResource("resources/test.txt", content2));

            var result2 = resource_finder.getContent("resources/test.txt");
            var time2 = resource_finder.getModificationTime("resources/test.txt");
            assertEquals(content2, result2);

            assertTrue(time1 != time2);
        } catch (ResourceFinderErrorException | ResourceWriterErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUpdateMissingResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            assertNull(resource_finder.getContent("resources/test_blah.txt"));
            assertFalse(resource_finder.updateResource("resources/test_blah.txt", "blah"));
            assertNull(resource_finder.getContent("resources/test_blah.txt"));
        } catch (ResourceFinderErrorException | ResourceWriterErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemoveResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            assertNotNull(resource_finder.getContent("resources/test.txt"));
            assertTrue(resource_finder.removeResource("resources/test.txt"));
            assertNull(resource_finder.getContent("resources/test.txt"));
            assertFalse(resource_finder.removeResource("resources/test.txt"));
        } catch (ResourceFinderErrorException | ResourceWriterErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetUnknownStreamByName(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
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
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetUnknownStreamByResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
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
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetStreamByName(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
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
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetStreamByResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
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
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetUnknownContentByName(Datasource datasource) {
        setup(datasource);


        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            var content = resource_finder.getContent("this/resource/doesnt/exist.txt");
            assertNull(content);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetUnknownContentByResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            var content = resource_finder.getContent(new URL("file://this/resource/doesnt/exist.txt"));
            assertNull(content);
        } catch (ResourceFinderErrorException | MalformedURLException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentByName(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            var content = resource_finder.getContent("resources/test.txt");
            assertNotNull(content);
            assertEquals(content, RESOURCE);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentByResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        var resource = resource_finder.getResource("resources/test.txt");
        try {
            var content = resource_finder.getContent(resource);
            assertNotNull(content);
            assertEquals(content, RESOURCE);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentByNameAndEncoding(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            var content = resource_finder.getContent("resources/test-utf8.txt", "UTF-8");
            assertNotNull(content);
            assertEquals(content, RESOURCE_UTF8);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentByResourceAndEncoding(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        var resource = resource_finder.getResource("resources/test-utf8.txt");
        try {
            var content = resource_finder.getContent(resource, "UTF-8");
            assertNotNull(content);
            assertEquals(content, RESOURCE_UTF8);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetUnknownModificationTimeByName(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            var time = resource_finder.getModificationTime("this/resource/doesnt/exist.txt");
            assertEquals(-1, time);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetUnknownModificationTimeByResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            var time = resource_finder.getModificationTime(new URL("file://this/resource/doesnt/exist.txt"));
            assertEquals(-1, time);
        } catch (ResourceFinderErrorException | MalformedURLException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetModificationTimeByName(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        try {
            var time = resource_finder.getModificationTime("resources/test.txt");
            assertTrue(time != -1);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetModificationTimeByResource(Datasource datasource) {
        setup(datasource);

        var resource_finder = DatabaseResourcesFactory.instance(datasource);
        var resource = resource_finder.getResource("resources/test.txt");
        try {
            var time = resource_finder.getModificationTime(resource);
            assertTrue(time != -1);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }
}
