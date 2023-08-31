/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.cmf.dam.contentstores.*;

import rife.cmf.Content;
import rife.cmf.ContentRepository;
import rife.cmf.MimeType;
import rife.cmf.dam.contentmanagers.DatabaseContentFactory;
import rife.cmf.dam.contentmanagers.exceptions.InstallContentErrorException;
import rife.cmf.dam.contentmanagers.exceptions.UnknownContentRepositoryException;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class TestContentManager {
    public void setup(Datasource datasource) {
        DatabaseContentFactory.instance(datasource).install();
    }

    public void tearDown(Datasource datasource) {
        try {
            DatabaseContentFactory.instance(datasource).remove();
        } catch (Throwable e) {
            // discard errors
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstallError(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.install();
                fail();
            } catch (InstallContentErrorException e) {
                assertNotNull(e.getCause());
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemoveError(Datasource datasource) {
        setup(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();

            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.remove();
                fail();
            } catch (ContentManagerException e) {
                assertNotNull(e.getCause());
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentXhtml(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // text data
            final var data_text = """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html><head><title>my title</title></head><body></body></html>""";
            var content = new Content(MimeType.APPLICATION_XHTML, data_text);
            assertTrue(manager.storeContent("/textcontent", content, null));

            manager.useContentData("/textcontent", contentData -> assertEquals(data_text, contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentGif(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // image data
            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);
            final var data_image_png = FileUtils.readBytes(image_resource_png);

            var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
            assertTrue(manager.storeContent("/imagegif", content, null));

            manager.useContentData("/imagegif", contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentTif(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // image data
            var image_resource_tif = ResourceFinderClasspath.instance().getResource("uwyn.tif");
            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn-noalpha.png");
            var data_image_tif = FileUtils.readBytes(image_resource_tif);
            final var data_image_png = FileUtils.readBytes(image_resource_png);

            var content = new Content(MimeType.IMAGE_PNG, data_image_tif);
            assertTrue(manager.storeContent("/imagetif", content, null));

            manager.useContentData("/imagetif", contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentGifResized(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // image data
            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn_resized-width_20.png");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);
            final var data_image_png = FileUtils.readBytes(image_resource_png);

            var content_image = new Content(MimeType.IMAGE_PNG, data_image_gif);
            content_image.attribute("width", 20);
            assertTrue(manager.storeContent("/imagegif", content_image, null));

            manager.useContentData("/imagegif", contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentTifResized(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // image data
            var image_resource_tif = ResourceFinderClasspath.instance().getResource("uwyn.tif");
            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn_resized-width_20-noalpha.png");
            var data_image_tif = FileUtils.readBytes(image_resource_tif);
            final var data_image_png = FileUtils.readBytes(image_resource_png);

            var content = new Content(MimeType.IMAGE_PNG, data_image_tif);
            content.attribute("width", 20);
            assertTrue(manager.storeContent("/imagetif", content, null));

            manager.useContentData("/imagetif", contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentRaw(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var size = 1024 * 1024 * 4; // 4Mb
            final var raw = new byte[size];
            for (var i = 0; i < size; i++) {
                raw[i] = (byte) (i % 255);
            }

            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
            assertTrue(manager.storeContent("/rawdata", content, null));

            manager.useContentData("/rawdata", new ContentDataUserWithoutResult() {
                public void useContentData(Object contentData)
                throws InnerClassException {
                    try {
                        assertArrayEquals(raw, FileUtils.readBytes((InputStream) contentData));
                    } catch (FileUtilsErrorException e) {
                        throwException(e);
                    }
                }
            });
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentRepository(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            manager.createRepository("mynewrep");

            // text data
            final var data_text = """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html><head><title>my title</title></head><body></body></html>""";
            var content = new Content(MimeType.APPLICATION_XHTML, data_text);
            assertTrue(manager.storeContent("mynewrep:/textcontent", content, null));

            manager.useContentData("mynewrep:/textcontent", contentData ->
                assertEquals(data_text, contentData));
            manager.useContentData("/textcontent", Assertions::assertNull);
            manager.useContentData(ContentRepository.DEFAULT + ":/textcontent", Assertions::assertNull);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreUnknownContentRepository(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // text data
            final var data_text = """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html><head><title>my title</title></head><body></body></html>""";
            var content = new Content(MimeType.APPLICATION_XHTML, data_text);
            try {
                manager.storeContent("mynewrep:/textcontent", content, null);
                fail();
            } catch (UnknownContentRepositoryException e) {
                assertEquals(e.getRepositoryName(), "mynewrep");
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testCreateRepositoryIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.createRepository(null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.createRepository("");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testContainsContentRepository(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            assertTrue(manager.containsRepository(""));
            assertTrue(manager.containsRepository(ContentRepository.DEFAULT));
            assertFalse(manager.containsRepository("mynewrep"));

            manager.createRepository("mynewrep");
            assertTrue(manager.containsRepository("mynewrep"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testContainsRepositoryIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.containsRepository(null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // text data
            var data_text = """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html><head><title>my title</title></head><body></body></html>""";
            var content_text = new Content(MimeType.APPLICATION_XHTML, data_text);

            try {
                manager.storeContent(null, content_text, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.storeContent("", content_text, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.storeContent("notabsolute", content_text, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.storeContent("default:", content_text, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.storeContent("default:notabsolute", content_text, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.storeContent("/nocontent", null, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUseContent(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // image data
            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);
            final var data_image_png = FileUtils.readBytes(image_resource_png);

            manager.storeContent("/the/logo/of", new Content(MimeType.IMAGE_PNG, data_image_gif), null);

            manager.useContentData("/the/logo/of", contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));

            manager.useContentData("/the/logo/of/uwyn.png", contentData -> fail());

            assertNull(manager.useContentDataResult("/the/logo/of/wrongname.png", contentData -> {
                fail();
                return null;
            }));

            assertNull(manager.useContentDataResult("/the/wrong/path/uwyn.png", contentData -> {
                fail();
                return null;
            }));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUseContentName(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            // image data
            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);
            final var data_image_png = FileUtils.readBytes(image_resource_png);

            manager.storeContent("/the/logo/of", new Content(MimeType.IMAGE_PNG, data_image_gif).name("uwyn.png"), null);

            manager.useContentData("/the/logo/of", contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));

            manager.useContentData("/the/logo/of/uwyn.png", contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));

            manager.useContentData("/the/logo/of/wrongname.png", contentData -> fail());

            assertNull(manager.useContentDataResult("/the/wrong/path/uwyn.png", contentData -> {
                fail();
                return null;
            }));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUseContentDataIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.useContentData(null, contentData -> {
                });
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.useContentData("", contentData -> {
                });
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.useContentData("notabsolute", contentData -> {
                });
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.useContentData("/url", null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.useContentDataResult(null, contentData -> null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.useContentDataResult("", contentData -> null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.useContentDataResult("notabsolute", contentData -> null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.useContentDataResult("/url", null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUseContentDataUnknown(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            manager.useContentData("/unknown", Assertions::assertNull);
            manager.useContentDataResult("/unknown", contentData -> {
                assertNull(contentData);
                return null;
            });
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContentData(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            assertFalse(manager.hasContentData("/textcontent"));

            var content = new Content(MimeType.APPLICATION_XHTML, "<p>some text</p>").fragment(true);
            manager.storeContent("/textcontent", content, null);

            assertTrue(manager.hasContentData("/textcontent"));
            assertFalse(manager.hasContentData("/textcontent/mytext.xhtml"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContentDataName(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            assertFalse(manager.hasContentData("/textcontent"));

            var content = new Content(MimeType.APPLICATION_XHTML, "<p>some text</p>").name("mytext.xhtml").fragment(true);
            manager.storeContent("/textcontent", content, null);

            assertTrue(manager.hasContentData("/textcontent"));
            assertTrue(manager.hasContentData("/textcontent/mytext.xhtml"));
            assertFalse(manager.hasContentData("/textcontent/unknowntext.xhtml"));
            assertFalse(manager.hasContentData("/unknowncontent/mytext.xhtml"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContentDataIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.hasContentData(null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.hasContentData("");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.hasContentData("notabsolute");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentImage(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var manager = DatabaseContentFactory.instance(datasource);

            assertFalse(manager.hasContentData("/imagecontent"));

            var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
            manager.storeContent("/imagecontent", content, null);

            var store = DatabaseImageStoreFactory.instance(datasource);
            var info = manager.getContentInfo("/imagecontent");
            assertNotNull(info);
            manager.useContentData("/imagecontent", Assertions::assertNotNull);
            assertTrue(manager.hasContentData("/imagecontent"));
            assertTrue(store.hasContentData(info.getContentId()));
            assertTrue(manager.deleteContent("/imagecontent"));

            assertNull(manager.getContentInfo("/imagecontent"));
            manager.useContentData("/imagecontent", Assertions::assertNull);
            assertFalse(manager.hasContentData("/imagecontent"));
            assertFalse(store.hasContentData(info.getContentId()));
            assertFalse(manager.deleteContent("/imagecontent"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentText(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            assertFalse(manager.hasContentData("/textcontent"));

            var content = new Content(MimeType.APPLICATION_XHTML, "<p>some text</p>").fragment(true);
            manager.storeContent("/textcontent", content, null);

            var store = DatabaseTextStoreFactory.instance(datasource);
            var info = manager.getContentInfo("/textcontent");
            assertNotNull(info);
            manager.useContentData("/textcontent", Assertions::assertNotNull);
            assertTrue(manager.hasContentData("/textcontent"));
            assertTrue(store.hasContentData(info.getContentId()));
            assertTrue(manager.deleteContent("/textcontent"));

            assertNull(manager.getContentInfo("/textcontent"));
            manager.useContentData("/textcontent", Assertions::assertNull);
            assertFalse(manager.hasContentData("/textcontent"));
            assertFalse(store.hasContentData(info.getContentId()));
            assertFalse(manager.deleteContent("/textcontent"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentRaw(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var size = 1024 * 1024 * 4; // 4Mb
            final var raw = new byte[size];
            for (var i = 0; i < size; i++) {
                raw[i] = (byte) (i % 255);
            }

            assertFalse(manager.hasContentData("/rawcontent"));

            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
            manager.storeContent("/rawcontent", content, null);

            var store = DatabaseRawStoreFactory.instance(datasource);
            var info = manager.getContentInfo("/rawcontent");
            assertNotNull(info);
            manager.useContentData("/rawcontent", Assertions::assertNotNull);
            assertTrue(manager.hasContentData("/rawcontent"));
            assertTrue(store.hasContentData(info.getContentId()));
            assertTrue(manager.deleteContent("/rawcontent"));

            assertNull(manager.getContentInfo("/rawcontent"));
            manager.useContentData("/rawcontent", Assertions::assertNull);
            assertFalse(manager.hasContentData("/rawcontent"));
            assertFalse(store.hasContentData(info.getContentId()));
            assertFalse(manager.deleteContent("/rawcontent"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.deleteContent(null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.deleteContent("");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.deleteContent("notabsolute");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentInfo(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            assertNull(manager.getContentInfo("/textcontent"));

            var content_text1 = new Content(MimeType.APPLICATION_XHTML, "<p>some text</p>")
                .fragment(true)
                .attribute("attr1", "value1")
                .attribute("attr2", "value2");
            manager.storeContent("/textcontent", content_text1, null);

            var info1a = manager.getContentInfo("/textcontent");
            assertNotNull(info1a);
            assertTrue(info1a.getContentId() >= 0);
            assertNotNull(info1a.getCreated());
            assertTrue(info1a.getCreated().getTime() <= System.currentTimeMillis());
            assertEquals("/textcontent", info1a.getPath());
            assertEquals(MimeType.APPLICATION_XHTML.toString(), info1a.getMimeType());
            assertEquals(0, info1a.getVersion());
            assertNotNull(info1a.getAttributes());
            assertEquals(2, info1a.getAttributes().size());
            assertEquals("value1", info1a.getAttributes().get("attr1"));
            assertEquals("value2", info1a.getAttributes().get("attr2"));

            var info1b = manager.getContentInfo("/textcontent/mytext.html");
            assertNull(info1b);

            var content_text2 = new Content(MimeType.APPLICATION_XHTML, "<p>some other text</p>")
                .fragment(true);
            manager.storeContent("/textcontent", content_text2, null);

            var info2a = manager.getContentInfo("/textcontent");
            assertNotNull(info2a);
            assertEquals(info1a.getContentId() + 1, info2a.getContentId());
            assertNotNull(info2a.getCreated());
            assertTrue(info2a.getCreated().getTime() <= System.currentTimeMillis());
            assertEquals("/textcontent", info2a.getPath());
            assertEquals(MimeType.APPLICATION_XHTML.toString(), info2a.getMimeType());
            assertEquals(1, info2a.getVersion());
            assertNull(info2a.getAttributes());

            var info2b = manager.getContentInfo("/textcontent/mytext.html");
            assertNull(info2b);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentInfoName(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            assertNull(manager.getContentInfo("/textcontent"));

            var content_text1 = new Content(MimeType.APPLICATION_XHTML, "<p>some text</p>")
                .fragment(true)
                .attribute("attr1", "value1")
                .attribute("attr2", "value2")
                .name("mytext.html");
            manager.storeContent("/textcontent", content_text1, null);

            var info1a = manager.getContentInfo("/textcontent");
            assertNotNull(info1a);
            assertTrue(info1a.getContentId() >= 0);
            assertNotNull(info1a.getCreated());
            assertTrue(info1a.getCreated().getTime() <= System.currentTimeMillis());
            assertEquals("/textcontent", info1a.getPath());
            assertEquals(MimeType.APPLICATION_XHTML.toString(), info1a.getMimeType());
            assertEquals(0, info1a.getVersion());
            assertNotNull(info1a.getAttributes());
            assertEquals(2, info1a.getAttributes().size());
            assertEquals("value1", info1a.getAttributes().get("attr1"));
            assertEquals("value2", info1a.getAttributes().get("attr2"));

            var info1b = manager.getContentInfo("/textcontent/mytext.html");
            assertNotNull(info1b);
            assertTrue(info1b.getContentId() >= 0);
            assertNotNull(info1b.getCreated());
            assertTrue(info1b.getCreated().getTime() <= System.currentTimeMillis());
            assertEquals("/textcontent", info1b.getPath());
            assertEquals(MimeType.APPLICATION_XHTML.toString(), info1b.getMimeType());
            assertEquals(0, info1b.getVersion());
            assertNotNull(info1b.getAttributes());
            assertEquals(2, info1b.getAttributes().size());
            assertEquals("value1", info1b.getAttributes().get("attr1"));
            assertEquals("value2", info1b.getAttributes().get("attr2"));

            var info1c = manager.getContentInfo("/textcontent/unknown.html");
            assertNull(info1c);

            var info1d = manager.getContentInfo("/unknowncontent/mytext.html");
            assertNull(info1d);

            var content_text2 = new Content(MimeType.APPLICATION_XHTML, "<p>some other text</p>")
                .fragment(true);
            manager.storeContent("/textcontent", content_text2, null);
            var info2 = manager.getContentInfo("/textcontent");
            assertNotNull(info2);
            assertEquals(info1a.getContentId() + 1, info2.getContentId());
            assertNotNull(info2.getCreated());
            assertTrue(info2.getCreated().getTime() <= System.currentTimeMillis());
            assertEquals("/textcontent", info2.getPath());
            assertEquals(MimeType.APPLICATION_XHTML.toString(), info2.getMimeType());
            assertEquals(1, info2.getVersion());
            assertNull(info2.getAttributes());

            var info2b = manager.getContentInfo("/textcontent/mytext.html");
            assertNotNull(info2b);

            var info2c = manager.getContentInfo("/textcontent/unknown.html");
            assertNull(info2c);

            var info2d = manager.getContentInfo("/unknowncontent/mytext.html");
            assertNull(info2d);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentInfoIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.getContentInfo(null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.getContentInfo("");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                manager.getContentInfo("notabsolute");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentIllegalArgument(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            try {
                manager.serveContentData(null, "/apath");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentForHtmlInvalidPath(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            assertEquals("", manager.getContentForHtml(null, null, null));
            assertEquals("", manager.getContentForHtml("", null, null));
            assertEquals("", manager.getContentForHtml("notabsolute", null, null));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentForHtmlUnknownPath(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            assertEquals("", manager.getContentForHtml("/unknown", null, null));
        } finally {
            tearDown(datasource);
        }
    }
}

