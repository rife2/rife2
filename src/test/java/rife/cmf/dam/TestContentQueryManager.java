/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.cmf.MimeType;
import rife.cmf.dam.contentmanagers.DatabaseContentFactory;
import rife.cmf.dam.exceptions.*;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.TestDatasources;
import rife.database.queries.Select;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class TestContentQueryManager {
    public void setup(Datasource datasource) {
        DatabaseContentFactory.instance(datasource).install();
    }

    public void tearDown(Datasource datasource) {
        DatabaseContentFactory.instance(datasource).remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiation(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            assertNotNull(manager);
            assertNotNull(manager.getContentManager());
            assertTrue(manager.getContentManager() instanceof ContentManager);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBuildCmfPathBean(Datasource datasource) {
        setup(datasource);
        try {
            var content = new ContentImage()
                .name("the content name");
            content
                .setId(3);

            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            assertEquals("/contentimage/3/image", manager.buildCmfPath(content, "image"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBuildCmfPathId(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            assertEquals("/contentimage/4/image", manager.buildCmfPath(4, "image"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBuildCmfPathBeanRepository(Datasource datasource) {
        setup(datasource);
        try {
            var content = new ContentImageRepository()
                .name("the content name");
            content
                .setId(3);

            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            assertEquals("testrep:/contentimagerepository/3/image", manager.buildCmfPath(content, "image"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBuildCmfPathIdRepository(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            assertEquals("testrep:/contentimagerepository/4/image", manager.buildCmfPath(4, "image"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBuildServeContentPathBean(Datasource datasource) {
        setup(datasource);
        try {
            var content = new ContentImage()
                .name("the content name");
            content
                .setId(3);

            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            assertEquals("/contentimage/3/image", manager.buildServeContentPath(content, "image"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBuildServeContentPathId(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            assertEquals("/contentimage/4/image", manager.buildServeContentPath(4, "image"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBuildServeContentPathBeanRepository(Datasource datasource) {
        setup(datasource);
        try {
            var content = new ContentImageRepository()
                .name("the content name");
            content
                .setId(3);

            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            assertEquals("/contentimagerepository/3/image", manager.buildServeContentPath(content, "image"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBuildServeContentPathIdRepository(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            assertEquals("/contentimagerepository/4/image", manager.buildServeContentPath(4, "image"));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveContent(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                var content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImage.class);
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());

                var info = content_manager.getContentInfo(path);
                assertEquals(id, info.getContentId());
                assertEquals(MimeType.IMAGE_PNG.toString(), info.getMimeType());
                assertEquals("myimage.png", info.getName());

                var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                final var data_image_png = FileUtils.readBytes(image_resource_png);

                content_manager.useContentData(path, contentData ->
                    assertArrayEquals(data_image_png, (byte[]) contentData));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveContentOtherTable(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class, "othercontentimage");
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                var content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");
                assertTrue(path.startsWith("/contentimage/"));

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImage.class, "othercontentimage");
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());
                assertEquals(1, new DbQueryManager(datasource)
                    .executeGetFirstInt(new Select(datasource)
                        .field("count(*)")
                        .from("othercontentimage")));

                var info = content_manager.getContentInfo(path);
                assertEquals(id, info.getContentId());
                assertEquals(MimeType.IMAGE_PNG.toString(), info.getMimeType());
                assertEquals("myimage.png", info.getName());

                var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                final var data_image_png = FileUtils.readBytes(image_resource_png);

                content_manager.useContentData(path, contentData ->
                    assertArrayEquals(data_image_png, (byte[]) contentData));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    private void checkContentRepository(Datasource datasource, int id, String repository) {
        assertEquals(repository, new DbQueryManager(datasource).executeGetFirstString(new Select(datasource)
            .from(RifeConfig.cmf().getTableContentInfo() + " i")
            .join(RifeConfig.cmf().getTableContentRepository() + " r")
            .field("r.name")
            .where("contentId", "=", id)
            .whereAnd("i.repositoryId = r.repositoryId")));
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveContentRepository(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            manager.install();
            manager.getContentManager().createRepository("testrep");
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImageRepository()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                checkContentRepository(datasource, id, "testrep");

                var content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");
                assertTrue(path.startsWith("testrep:"));

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImageRepository.class);
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());

                var info = content_manager.getContentInfo(path);
                assertEquals(id, info.getContentId());
                assertEquals(MimeType.IMAGE_PNG.toString(), info.getMimeType());
                assertEquals("myimage.png", info.getName());

                var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                final var data_image_png = FileUtils.readBytes(image_resource_png);

                content_manager.useContentData(path, contentData ->
                    assertArrayEquals(data_image_png, (byte[]) contentData));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveContentRaw(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentRaw.class);
            manager.install();
            try {
                var size = 1024 * 1024 * 4; // 4Mb
                final var raw = new byte[size];
                for (var i = 0; i < size; i++) {
                    raw[i] = (byte) (i % 255);
                }

                var content = new ContentRaw()
                    .name("the content name")
                    .raw(new ByteArrayInputStream(raw));

                var id = manager.save(content);
                assertTrue(id >= 0);

                var content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "raw");

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentRaw.class);
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getRaw());

                var info = content_manager.getContentInfo(path);
                assertEquals(id, info.getContentId());
                assertEquals(MimeType.RAW.toString(), info.getMimeType());

                content_manager.useContentData(path, new ContentDataUserWithoutResult() {
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
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveContentUpdate(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);

                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                var rife_resource_tif = ResourceFinderClasspath.instance().getResource("rife-logo_small.tif");
                var rife_image_tif = FileUtils.readBytes(rife_resource_tif);
                content
                    .name("updated content name")
                    .image(rife_image_tif);

                manager.save(content);

                var content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImage.class);
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());

                var info = content_manager.getContentInfo(path);
                assertEquals(id + 1, info.getContentId());
                assertEquals(MimeType.IMAGE_PNG.toString(), info.getMimeType());

                var rife_resource_png = ResourceFinderClasspath.instance().getResource("rife-logo_small.png");
                final var rife_image_png = FileUtils.readBytes(rife_resource_png);

                content_manager.useContentData(path, contentData ->
                    assertArrayEquals(rife_image_png, (byte[]) contentData));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveContentUpdateRepository(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            manager.install();
            manager.getContentManager().createRepository("testrep");
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);

                var content = new ContentImageRepository()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                checkContentRepository(datasource, id, "testrep");

                var rife_resource_tif = ResourceFinderClasspath.instance().getResource("rife-logo_small.tif");
                var rife_image_tif = FileUtils.readBytes(rife_resource_tif);
                content
                    .name("updated content name")
                    .image(rife_image_tif);

                manager.save(content);

                var content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");
                assertTrue(path.startsWith("testrep:"));

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImageRepository.class);
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());

                var info = content_manager.getContentInfo(path);
                assertEquals(id + 1, info.getContentId());
                assertEquals(MimeType.IMAGE_PNG.toString(), info.getMimeType());

                checkContentRepository(datasource, info.getContentId(), "testrep");

                var rife_resource_png = ResourceFinderClasspath.instance().getResource("rife-logo_small.png");
                final var rife_image_png = FileUtils.readBytes(rife_resource_png);

                content_manager.useContentData(path, contentData ->
                    assertArrayEquals(rife_image_png, (byte[]) contentData));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSavePojo(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, RegularPojo.class);
            manager.install();
            try {
                var content = new RegularPojo()
                    .name("the regular pojo name");

                var id = manager.save(content);
                assertTrue(id >= 0);

                var gqm = GenericQueryManagerFactory.instance(datasource, RegularPojo.class);
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreEmptyContent(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                var content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");

                assertTrue(content_manager.hasContentData(path));
                content_manager.useContentData(path, Assertions::assertNotNull);

                assertTrue(manager.storeEmptyContent(content, "image"));

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImage.class);
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());

                var info = content_manager.getContentInfo(path);
                assertEquals(id + 1, info.getContentId());
                assertEquals(MimeType.IMAGE_PNG.toString(), info.getMimeType());

                assertFalse(content_manager.hasContentData(path));
                content_manager.useContentData(path, Assertions::assertNull);
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreEmptyContentRepository(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            manager.install();
            manager.getContentManager().createRepository("testrep");
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImageRepository()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                checkContentRepository(datasource, id, "testrep");

                var content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");
                assertTrue(path.startsWith("testrep:"));

                assertTrue(content_manager.hasContentData(path));
                content_manager.useContentData(path, Assertions::assertNotNull);

                assertTrue(manager.storeEmptyContent(content, "image"));

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImageRepository.class);
                var restored = gqm.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());

                var info = content_manager.getContentInfo(path);
                assertEquals(id + 1, info.getContentId());
                assertEquals(MimeType.IMAGE_PNG.toString(), info.getMimeType());

                assertFalse(content_manager.hasContentData(path));
                content_manager.useContentData(path, Assertions::assertNull);
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreEmptyContentIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                try {
                    manager.storeEmptyContent(null, "image");
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }

                try {
                    manager.storeEmptyContent(new ContentImage(), null);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }

                try {
                    manager.storeEmptyContent(new ContentImage(), "");
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreEmptyContentPojo(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, RegularPojo.class);
            manager.install();
            try {
                var content = new RegularPojo()
                    .name("the regular pojo name");

                assertFalse(manager.storeEmptyContent(content, "name"));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreEmptyContentMissingIdentifier(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                try {
                    manager.storeEmptyContent(new ContentImage(), "image");
                    fail();
                } catch (MissingIdentifierValueException e) {
                    assertSame(ContentImage.class, e.getBeanClass());
                    assertEquals("id", e.getIdentifierName());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreEmptyContentUnknownProperty(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                manager.save(content);

                try {
                    manager.storeEmptyContent(content, "imageunknown");
                    fail();
                } catch (UnknownConstrainedPropertyException e) {
                    assertSame(ContentImage.class, e.getBeanClass());
                    assertEquals("imageunknown", e.getProperty());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreEmptyContentMimeTypeExpected(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                manager.save(content);

                try {
                    manager.storeEmptyContent(content, "name");
                    fail();
                } catch (ExpectedMimeTypeConstraintException e) {
                    assertSame(ContentImage.class, e.getBeanClass());
                    assertEquals("name", e.getProperty());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContent(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                assertTrue(manager.delete(id));

                ContentManager content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImage.class);
                assertNull(gqm.restore(id));
                assertNull(content_manager.getContentInfo(path));
                assertFalse(content_manager.hasContentData(path));
                content_manager.useContentData(path, Assertions::assertNull);
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentRepository(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            manager.install();
            manager.getContentManager().createRepository("testrep");
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImageRepository()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                checkContentRepository(datasource, id, "testrep");

                assertTrue(manager.delete(id));

                ContentManager content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");
                assertTrue(path.startsWith("testrep:"));

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImageRepository.class);
                assertNull(gqm.restore(id));
                assertNull(content_manager.getContentInfo(path));
                assertFalse(content_manager.hasContentData(path));
                content_manager.useContentData(path, Assertions::assertNull);
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentNonCmfProperty(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageNonCmfProps.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImageNonCmfProps()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);
                assertTrue(id >= 0);

                assertTrue(manager.delete(id));

                ContentManager content_manager = DatabaseContentFactory.instance(datasource);
                var path = manager.buildCmfPath(content, "image");

                var gqm = GenericQueryManagerFactory.instance(datasource, ContentImageNonCmfProps.class);
                assertNull(gqm.restore(id));
                assertNull(content_manager.getContentInfo(path));
                assertFalse(content_manager.hasContentData(path));
                content_manager.useContentData(path, Assertions::assertNull);
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeletePojo(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, RegularPojo.class);
            manager.install();
            try {
                var content = new RegularPojo()
                    .name("the regular pojo name");

                var id = manager.save(content);
                assertTrue(id >= 0);

                assertTrue(manager.delete(id));

                var gqm = GenericQueryManagerFactory.instance(datasource, RegularPojo.class);
                assertNull(gqm.restore(id));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteOrdinal(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, Ordered.class);
            manager.install();
            try {
                var content1 = new Ordered()
                    .name("the content name");

                var id1 = manager.save(content1);
                assertTrue(id1 >= 0);

                var content2 = new Ordered()
                    .name("another content name");

                var id2 = manager.save(content2);
                assertTrue(id2 > id1);

                var content3 = new Ordered()
                    .name("one more content name");

                var id3 = manager.save(content3);
                assertTrue(id3 > id2);

                assertTrue(manager.delete(id2));

                var gqm = GenericQueryManagerFactory.instance(datasource, Ordered.class);
                content1 = gqm.restore(id1);
                assertEquals(0, content1.getPriority());
                assertNull(gqm.restore(id2));
                content3 = gqm.restore(id3);
                assertEquals(1, content3.getPriority());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteOrdinalRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrderedRestricted.class);
            manager.install();
            try {
                var content1 = new OrderedRestricted()
                    .name("the content name")
                    .restricted(3);

                var id1 = manager.save(content1);
                assertTrue(id1 >= 0);

                var content2 = new OrderedRestricted()
                    .name("another content name")
                    .restricted(5);

                var id2 = manager.save(content2);
                assertTrue(id2 > id1);

                var content3 = new OrderedRestricted()
                    .name("some other content name")
                    .restricted(3);

                var id3 = manager.save(content3);
                assertTrue(id3 > id2);

                var content4 = new OrderedRestricted()
                    .name("yet one more content name")
                    .restricted(3);

                var id4 = manager.save(content4);
                assertTrue(id4 > id3);

                var content5 = new OrderedRestricted()
                    .name("the last content name")
                    .restricted(5);

                var id5 = manager.save(content5);
                assertTrue(id5 > id4);

                assertTrue(manager.delete(id3));

                var gqm = GenericQueryManagerFactory.instance(datasource, OrderedRestricted.class);
                content1 = gqm.restore(id1);
                assertEquals(0, content1.getPriority());
                content2 = gqm.restore(id2);
                assertEquals(0, content2.getPriority());
                assertNull(gqm.restore(id3));
                content4 = gqm.restore(id4);
                assertEquals(1, content4.getPriority());
                content5 = gqm.restore(id5);
                assertEquals(1, content5.getPriority());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteNotPresent(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, RegularPojo.class);
            manager.install();
            try {
                assertFalse(manager.delete(3));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentUnknownId(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                assertFalse(manager.delete(3));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveOrdinal(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, Ordered.class);
            manager.install();
            try {
                var content1 = new Ordered()
                    .name("the content name");

                var id1 = manager.save(content1);
                assertTrue(id1 >= 0);

                var content2 = new Ordered()
                    .name("another content name");

                var id2 = manager.save(content2);
                assertTrue(id2 > id1);

                var content3 = new Ordered()
                    .name("one more content name");

                var id3 = manager.save(content3);
                assertTrue(id3 > id2);

                var gqm = GenericQueryManagerFactory.instance(datasource, Ordered.class);
                content1 = gqm.restore(id1);
                assertEquals(0, content1.getPriority());
                content2 = gqm.restore(id2);
                assertEquals(1, content2.getPriority());
                content3 = gqm.restore(id3);
                assertEquals(2, content3.getPriority());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveOrdinalRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrderedRestricted.class);
            manager.install();
            try {
                var content1 = new OrderedRestricted()
                    .name("the content name")
                    .restricted(3);

                var id1 = manager.save(content1);
                assertTrue(id1 >= 0);

                var content2 = new OrderedRestricted()
                    .name("another content name")
                    .restricted(5);

                var id2 = manager.save(content2);
                assertTrue(id2 > id1);

                var content3 = new OrderedRestricted()
                    .name("some other content name")
                    .restricted(3);

                var id3 = manager.save(content3);
                assertTrue(id3 > id2);

                var content4 = new OrderedRestricted()
                    .name("yet one more content name")
                    .restricted(3);

                var id4 = manager.save(content4);
                assertTrue(id4 > id3);

                var content5 = new OrderedRestricted()
                    .name("the last content name")
                    .restricted(5);

                var id5 = manager.save(content5);
                assertTrue(id5 > id4);


                var gqm = GenericQueryManagerFactory.instance(datasource, OrderedRestricted.class);
                content1 = gqm.restore(id1);
                assertEquals(0, content1.getPriority());
                content2 = gqm.restore(id2);
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(id3);
                assertEquals(1, content3.getPriority());
                content4 = gqm.restore(id4);
                assertEquals(2, content4.getPriority());
                content5 = gqm.restore(id5);
                assertEquals(1, content5.getPriority());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveOrdinal(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, Ordered.class);
            manager.install();
            try {
                var content1 = new Ordered()
                    .name("the content name");
                var content2 = new Ordered()
                    .name("another content name");
                var content3 = new Ordered()
                    .name("one more content name");

                manager.save(content1);
                manager.save(content2);
                manager.save(content3);

                var gqm = GenericQueryManagerFactory.instance(datasource, Ordered.class);
                content1 = gqm.restore(content1.getId());
                assertEquals(0, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(1, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(2, content3.getPriority());

                assertTrue(manager.move(content1, "priority", OrdinalManager.DOWN));

                content1 = gqm.restore(content1.getId());
                assertEquals(1, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(2, content3.getPriority());

                assertTrue(manager.move(content3, "priority", OrdinalManager.UP));

                content1 = gqm.restore(content1.getId());
                assertEquals(2, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(1, content3.getPriority());

                manager.up(content2, "priority");

                content1 = gqm.restore(content1.getId());
                assertEquals(2, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(1, content3.getPriority());

                manager.down(content1, "priority");

                content1 = gqm.restore(content1.getId());
                assertEquals(2, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(1, content3.getPriority());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, Ordered.class);
            manager.install();
            try {
                try {
                    manager.move(null, "priority", OrdinalManager.UP);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }

                try {
                    manager.move(new Ordered(), null, OrdinalManager.UP);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }

                try {
                    manager.move(new Ordered(), "", OrdinalManager.UP);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveUnknownProperty(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, Ordered.class);
            manager.install();
            try {
                try {
                    manager.move(new Ordered(), "priorityunknown", OrdinalManager.UP);
                    fail();
                } catch (UnknownConstrainedPropertyException e) {
                    assertSame(Ordered.class, e.getBeanClass());
                    assertEquals("priorityunknown", e.getProperty());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveNotOrdinalConstraint(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                try {
                    manager.move(new ContentImage(), "name", OrdinalManager.UP);
                    fail();
                } catch (ExpectedOrdinalConstraintException e) {
                    assertSame(ContentImage.class, e.getBeanClass());
                    assertEquals("name", e.getProperty());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveNotOrdinalInvalidOrdinalType(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrderedInvalidType.class);
            manager.install();
            try {
                try {
                    manager.move(new OrderedInvalidType(), "priority", OrdinalManager.UP);
                    fail();
                } catch (InvalidOrdinalTypeException e) {
                    assertSame(OrderedInvalidType.class, e.getBeanClass());
                    assertEquals("priority", e.getProperty());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveOrdinalRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrderedRestricted.class);
            manager.install();
            try {
                var content1 = new OrderedRestricted()
                    .name("the content name")
                    .restricted(3);

                var content2 = new OrderedRestricted()
                    .name("another content name")
                    .restricted(5);

                var content3 = new OrderedRestricted()
                    .name("some other content name")
                    .restricted(3);

                var content4 = new OrderedRestricted()
                    .name("yet one more content name")
                    .restricted(3);

                var content5 = new OrderedRestricted()
                    .name("the last content name")
                    .restricted(5);

                manager.save(content1);
                manager.save(content2);
                manager.save(content3);
                manager.save(content4);
                manager.save(content5);

                var gqm = GenericQueryManagerFactory.instance(datasource, OrderedRestricted.class);
                content1 = gqm.restore(content1.getId());
                assertEquals(0, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(1, content3.getPriority());
                content4 = gqm.restore(content4.getId());
                assertEquals(2, content4.getPriority());
                content5 = gqm.restore(content5.getId());
                assertEquals(1, content5.getPriority());

                assertTrue(manager.move(content1, "priority", OrdinalManager.DOWN));

                content1 = gqm.restore(content1.getId());
                assertEquals(1, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(0, content3.getPriority());
                content4 = gqm.restore(content4.getId());
                assertEquals(2, content4.getPriority());
                content5 = gqm.restore(content5.getId());
                assertEquals(1, content5.getPriority());

                assertTrue(manager.move(content4, "priority", OrdinalManager.UP));

                content1 = gqm.restore(content1.getId());
                assertEquals(2, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(0, content3.getPriority());
                content4 = gqm.restore(content4.getId());
                assertEquals(1, content4.getPriority());
                content5 = gqm.restore(content5.getId());
                assertEquals(1, content5.getPriority());

                assertTrue(manager.up(content5, "priority"));

                content1 = gqm.restore(content1.getId());
                assertEquals(2, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(1, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(0, content3.getPriority());
                content4 = gqm.restore(content4.getId());
                assertEquals(1, content4.getPriority());
                content5 = gqm.restore(content5.getId());
                assertEquals(0, content5.getPriority());

                assertTrue(manager.down(content5, "priority"));

                content1 = gqm.restore(content1.getId());
                assertEquals(2, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(0, content3.getPriority());
                content4 = gqm.restore(content4.getId());
                assertEquals(1, content4.getPriority());
                content5 = gqm.restore(content5.getId());
                assertEquals(1, content5.getPriority());

                manager.up(content2, "priority");

                content1 = gqm.restore(content1.getId());
                assertEquals(2, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(0, content3.getPriority());
                content4 = gqm.restore(content4.getId());
                assertEquals(1, content4.getPriority());
                content5 = gqm.restore(content5.getId());
                assertEquals(1, content5.getPriority());

                manager.down(content1, "priority");

                content1 = gqm.restore(content1.getId());
                assertEquals(2, content1.getPriority());
                content2 = gqm.restore(content2.getId());
                assertEquals(0, content2.getPriority());
                content3 = gqm.restore(content3.getId());
                assertEquals(0, content3.getPriority());
                content4 = gqm.restore(content4.getId());
                assertEquals(1, content4.getPriority());
                content5 = gqm.restore(content5.getId());
                assertEquals(1, content5.getPriority());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveOrdinalRestrictedInvalidType(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrdrdRestrInvalidType.class);
            manager.install();
            try {
                var content = new OrdrdRestrInvalidType()
                    .name("the content name")
                    .restricted("3");

                try {
                    manager.save(content);
                    fail();
                } catch (InvalidOrdinalRestrictionTypeException e) {
                    assertSame(OrdrdRestrInvalidType.class, e.getBeanClass());
                    assertEquals("priority", e.getProperty());
                    assertEquals("restricted", e.getRestriction());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveUnknownOrdinal(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrdrdUnknown.class);
            manager.install();
            try {
                try {
                    manager.move(new OrdrdUnknown(), "unknown", OrdinalManager.UP);
                    fail();
                } catch (UnknownOrdinalException e) {
                    assertSame(OrdrdUnknown.class, e.getBeanClass());
                    assertEquals("unknown", e.getProperty());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveUnknownRestriction(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrdrdRestrUnknown.class);
            manager.install();
            try {
                try {
                    manager.move(new OrdrdRestrUnknown(), "priority", OrdinalManager.UP);
                    fail();
                } catch (UnknownOrdinalRestrictionException e) {
                    assertSame(OrdrdRestrUnknown.class, e.getBeanClass());
                    assertEquals("priority", e.getProperty());
                    assertEquals("restrictedunknown", e.getRestriction());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveRestrictionInvalidType(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrdrdRestrInvalidType.class);
            manager.install();
            try {
                try {
                    manager.move(new OrdrdRestrInvalidType().restricted("restricted"), "priority", OrdinalManager.UP);
                    fail();
                } catch (InvalidOrdinalRestrictionTypeException e) {
                    assertSame(OrdrdRestrInvalidType.class, e.getBeanClass());
                    assertEquals("priority", e.getProperty());
                    assertEquals("restricted", e.getRestriction());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveRestrictionNull(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrdrdRestrInvalidType.class);
            manager.install();
            try {
                try {
                    manager.move(new OrdrdRestrInvalidType(), "priority", OrdinalManager.UP);
                    fail();
                } catch (OrdinalRestrictionCantBeNullException e) {
                    assertSame(OrdrdRestrInvalidType.class, e.getBeanClass());
                    assertEquals("priority", e.getProperty());
                    assertEquals("restricted", e.getRestriction());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveOrdinalRestrictedUnknownRestriction(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, OrdrdRestrUnknown.class);
            manager.install();
            try {
                var content = new OrdrdRestrUnknown()
                    .name("the content name")
                    .restricted(3);

                try {
                    manager.save(content);
                    fail();
                } catch (UnknownOrdinalRestrictionException e) {
                    assertSame(OrdrdRestrUnknown.class, e.getBeanClass());
                    assertEquals("priority", e.getProperty());
                    assertEquals("restrictedunknown", e.getRestriction());
                }
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContent(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);

                assertTrue(manager.hasContent(id, "image"));
                assertFalse(manager.hasContent(id, "unknown"));
                assertFalse(manager.hasContent(34, "image"));

                assertTrue(manager.hasContent(content, "image"));
                assertFalse(manager.hasContent(content, "unknown"));
                content.setId(334);
                assertFalse(manager.hasContent(content, "image"));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContentRepository(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageRepository.class);
            manager.install();
            manager.getContentManager().createRepository("testrep");
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImageRepository()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);

                checkContentRepository(datasource, id, "testrep");

                assertTrue(manager.hasContent(id, "image"));
                assertFalse(manager.hasContent(id, "unknown"));
                assertFalse(manager.hasContent(34, "image"));

                assertTrue(manager.hasContent(content, "image"));
                assertFalse(manager.hasContent(content, "unknown"));
                content.setId(334);
                assertFalse(manager.hasContent(content, "image"));
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreById(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImage.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImage()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);

                var restored = manager.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreByIdAutoRetrieved(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageAutoRetrieved.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImageAutoRetrieved()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);

                var restored = manager.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                var data_image_png = FileUtils.readBytes(image_resource_png);
                assertArrayEquals(data_image_png, restored.getImage());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreByIdPojo(Datasource datasource) {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, RegularPojo.class);
            manager.install();
            try {
                var content = new RegularPojo()
                    .name("the regular pojo name");

                var id = manager.save(content);
                assertTrue(id >= 0);

                var restored = manager.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreByIdNonCmfProperties(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageNonCmfProps.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);
                var content = new ContentImageNonCmfProps()
                    .name("the content name")
                    .image(data_image_gif);

                var id = manager.save(content);

                var restored = manager.restore(id);
                assertEquals(content.getId(), restored.getId());
                assertEquals(content.getName(), restored.getName());
                assertNull(restored.getImage());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreFirst(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageAutoRetrieved.class);
            manager.install();
            try {
                var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var data_image_gif = FileUtils.readBytes(image_resource_gif);

                var content1 = new ContentImageAutoRetrieved()
                    .name("the content name")
                    .image(data_image_gif);
                manager.save(content1);

                var image_resource_tif = ResourceFinderClasspath.instance().getResource("uwyn.tif");
                var data_image_tif = FileUtils.readBytes(image_resource_tif);

                var content2 = new ContentImageAutoRetrieved()
                    .name("another content name")
                    .image(data_image_tif);
                manager.save(content2);

                var restored = manager.restoreFirst(manager.getRestoreQuery().orderBy("id", Select.DESC));
                assertEquals(content2.getId(), restored.getId());
                assertEquals(content2.getName(), restored.getName());
                var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn-noalpha.png");
                var data_image_png = FileUtils.readBytes(image_resource_png);
                assertArrayEquals(data_image_png, restored.getImage());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestore(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageAutoRetrieved.class);
            manager.install();
            try {
                var uwyn_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var uwyn_image_gif = FileUtils.readBytes(uwyn_resource_gif);

                var content1 = new ContentImageAutoRetrieved()
                    .name("the content name")
                    .image(uwyn_image_gif);
                manager.save(content1);

                var rife_resource_tif = ResourceFinderClasspath.instance().getResource("rife-logo_small.tif");
                var rife_image_tif = FileUtils.readBytes(rife_resource_tif);

                var content2 = new ContentImageAutoRetrieved()
                    .name("another content name")
                    .image(rife_image_tif);
                manager.save(content2);

                var uwyn_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                var uwyn_image_png = FileUtils.readBytes(uwyn_resource_png);

                var rife_resource_png = ResourceFinderClasspath.instance().getResource("rife-logo_small.png");
                var rife_image_png = FileUtils.readBytes(rife_resource_png);

                var restored_list = manager.restore();
                ContentImageAutoRetrieved restored = null;
                var restored_it = restored_list.iterator();

                boolean checked1 = false;
                boolean checked2 = false;
                restored = restored_it.next();
                assertTrue(restored.getId() == content1.getId() || restored.getId() == content2.getId());
                if (restored.getId() == content1.getId()) {
                    checked1 = true;
                    assertEquals(content1.getName(), restored.getName());
                    assertArrayEquals(uwyn_image_png, restored.getImage());
                } else {
                    checked2 = true;
                    assertEquals(content2.getName(), restored.getName());
                    assertArrayEquals(rife_image_png, restored.getImage());
                }

                restored = restored_it.next();
                assertTrue(restored.getId() == content1.getId() || restored.getId() == content2.getId());
                if (restored.getId() == content1.getId()) {
                    assertFalse(checked1);
                    assertEquals(content1.getName(), restored.getName());
                    assertArrayEquals(uwyn_image_png, restored.getImage());
                } else {
                    assertFalse(checked2);
                    assertEquals(content2.getName(), restored.getName());
                    assertArrayEquals(rife_image_png, restored.getImage());
                }

                assertFalse(restored_it.hasNext());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreRepository(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageAutoRetrRep.class);
            manager.install();
            manager.getContentManager().createRepository("testrep");
            try {
                var uwyn_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var uwyn_image_gif = FileUtils.readBytes(uwyn_resource_gif);

                var content1 = new ContentImageAutoRetrRep()
                    .name("the content name")
                    .image(uwyn_image_gif);
                var id1 = manager.save(content1);

                checkContentRepository(datasource, id1, "testrep");

                var rife_resource_tif = ResourceFinderClasspath.instance().getResource("rife-logo_small.tif");
                var rife_image_tif = FileUtils.readBytes(rife_resource_tif);

                var content2 = new ContentImageAutoRetrRep()
                    .name("another content name")
                    .image(rife_image_tif);
                var id2 = manager.save(content2);

                checkContentRepository(datasource, id2, "testrep");

                var uwyn_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                var uwyn_image_png = FileUtils.readBytes(uwyn_resource_png);

                var rife_resource_png = ResourceFinderClasspath.instance().getResource("rife-logo_small.png");
                var rife_image_png = FileUtils.readBytes(rife_resource_png);

                var restored_list = manager.restore();
                ContentImageAutoRetrRep restored = null;
                var restored_it = restored_list.iterator();

                boolean checked1 = false;
                boolean checked2 = false;
                restored = restored_it.next();
                assertTrue(restored.getId() == content1.getId() || restored.getId() == content2.getId());
                if (restored.getId() == content1.getId()) {
                    checked1 = true;
                    assertEquals(content1.getName(), restored.getName());
                    assertArrayEquals(uwyn_image_png, restored.getImage());
                } else {
                    checked2 = true;
                    assertEquals(content2.getName(), restored.getName());
                    assertArrayEquals(rife_image_png, restored.getImage());
                }

                restored = restored_it.next();
                assertTrue(restored.getId() == content1.getId() || restored.getId() == content2.getId());
                if (restored.getId() == content1.getId()) {
                    assertFalse(checked1);
                    assertEquals(content1.getName(), restored.getName());
                    assertArrayEquals(uwyn_image_png, restored.getImage());
                } else {
                    assertFalse(checked2);
                    assertEquals(content2.getName(), restored.getName());
                    assertArrayEquals(rife_image_png, restored.getImage());
                }

                assertFalse(restored_it.hasNext());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreQuery(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = new ContentQueryManager<>(datasource, ContentImageAutoRetrieved.class);
            manager.install();
            try {
                var uwyn_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
                var uwyn_image_gif = FileUtils.readBytes(uwyn_resource_gif);

                var content1 = new ContentImageAutoRetrieved()
                    .name("the content name")
                    .image(uwyn_image_gif);
                manager.save(content1);

                var rife_resource_tif = ResourceFinderClasspath.instance().getResource("rife-logo_small.tif");
                var rife_image_tif = FileUtils.readBytes(rife_resource_tif);

                var content2 = new ContentImageAutoRetrieved()
                    .name("another content name")
                    .image(rife_image_tif);
                manager.save(content2);

                var uwyn_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                var uwyn_image_png = FileUtils.readBytes(uwyn_resource_png);

                var rife_resource_png = ResourceFinderClasspath.instance().getResource("rife-logo_small.png");
                var rife_image_png = FileUtils.readBytes(rife_resource_png);

                var restored_list = manager.restore(manager.getRestoreQuery().orderBy("id", Select.DESC));
                ContentImageAutoRetrieved restored = null;
                var restored_it = restored_list.iterator();
                restored = restored_it.next();
                assertEquals(content2.getId(), restored.getId());
                assertEquals(content2.getName(), restored.getName());
                assertArrayEquals(rife_image_png, restored.getImage());
                restored = restored_it.next();
                assertEquals(content1.getId(), restored.getId());
                assertEquals(content1.getName(), restored.getName());
                assertArrayEquals(uwyn_image_png, restored.getImage());
                assertFalse(restored_it.hasNext());
            } finally {
                manager.remove();
            }
        } finally {
            tearDown(datasource);
        }
    }
}
