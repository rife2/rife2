/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.cmf.dam.contentstores.exceptions.*;

import java.awt.*;

import rife.cmf.Content;
import rife.cmf.ContentInfo;
import rife.cmf.ContentRepository;
import rife.cmf.MimeType;
import rife.cmf.dam.contentmanagers.DatabaseContentFactory;
import rife.cmf.dam.contentmanagers.DatabaseContentInfo;
import rife.config.RifeConfig;
import rife.database.*;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;
import rife.tools.ImageWaiter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.sql.Timestamp;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseImageStore {
    private static byte[] sLargeImagePng = null;

    static {
        var edge = 1200;
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var icon = new ImageIcon(image_resource_gif);
        var image_scaled = icon.getImage().getScaledInstance(edge, edge, Image.SCALE_FAST);
        ImageWaiter.wait(image_scaled);
        var buffer = new BufferedImage(image_scaled.getWidth(null), image_scaled.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        var g2 = buffer.createGraphics();
        g2.drawImage(image_scaled, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        var gradient1 = new GradientPaint(0, 0, Color.red, 175, 175, Color.yellow, true);
        g2.setPaint(gradient1);
        g2.fill(new Rectangle(0, 0, edge, edge));
        var gradient2 = new GradientPaint(100, 50, Color.green, 205, 375, Color.blue, true);
        g2.setPaint(gradient2);
        g2.fill(new Rectangle(0, 0, edge, edge));
        g2.dispose();

        try {
            var writers = ImageIO.getImageWritersByMIMEType("image/png");
            var writer = writers.next();
            var byte_out = new ByteArrayOutputStream();
            var image_out = ImageIO.createImageOutputStream(byte_out);
            writer.setOutput(image_out);
            writer.write(buffer);
            writer.dispose();
            byte_out.flush();
            byte_out.close();

            sLargeImagePng = byte_out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            var store = DatabaseImageStoreFactory.instance(datasource);
            try {
                store.install();
                fail();
            } catch (InstallContentStoreErrorException e) {
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
            var store = DatabaseImageStoreFactory.instance(datasource);
            try {
                store.remove();
                fail();
            } catch (RemoveContentStoreErrorException e) {
                assertNotNull(e.getCause());
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetSupportedMimeTypes(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);
            assertTrue(store.getSupportedMimeTypes().size() > 0);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetContentType(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);
            var info = new ContentInfo();
            info.setMimeType(MimeType.IMAGE_GIF.toString());
            assertNotNull(store.getContentType(info));
            info.setMimeType(MimeType.APPLICATION_XHTML.toString());
            assertNull(store.getContentType(info));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetFormatter(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);
            assertNotNull(store.getFormatter(MimeType.IMAGE_GIF, false));
            assertNull(store.getFormatter(MimeType.APPLICATION_XHTML, true));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentData(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            final var id = new int[]{1};
            final var manager = DatabaseContentFactory.instance(datasource);
            final var insert = new Insert(datasource)
                .into(RifeConfig.cmf().getTableContentInfo())
                .fieldParameter("version")
                .fieldParameter("repositoryId");
            if ("org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                insert.fieldsParametersExcluded(DatabaseContentInfo.class, new String[]{"contentId"});
            } else {
                insert.fieldsParameters(DatabaseContentInfo.class);
            }
            if ("com.mysql.cj.jdbc.Driver".equals(datasource.getAliasedDriver())) {
                insert.fieldParameter("created");
            }
            manager.executeUpdate(insert, statement -> {
                var content_info = new DatabaseContentInfo();
                if (!"org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                    content_info.setContentId(id[0]);
                }
                content_info.setFragment(false);
                content_info.setPath("/testpath");
                content_info.setMimeType(MimeType.IMAGE_PNG.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var store = DatabaseImageStoreFactory.instance(datasource);
            var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
            assertTrue(store.storeContentData(id[0], content, null));

            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
            final var data_image_png = FileUtils.readBytes(image_resource_png);

            store.useContentData(id[0], contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentDataLarge(Datasource datasource) {
        setup(datasource);
        try {
            final var id = new int[]{1};
            final var manager = DatabaseContentFactory.instance(datasource);
            final var insert = new Insert(datasource)
                .into(RifeConfig.cmf().getTableContentInfo())
                .fieldParameter("version")
                .fieldParameter("repositoryId");
            if ("org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                insert.fieldsParametersExcluded(DatabaseContentInfo.class, new String[]{"contentId"});
            } else {
                insert.fieldsParameters(DatabaseContentInfo.class);
            }
            if ("com.mysql.cj.jdbc.Driver".equals(datasource.getAliasedDriver())) {
                insert.fieldParameter("created");
            }
            manager.executeUpdate(insert, statement -> {
                var content_info = new DatabaseContentInfo();
                if (!"org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                    content_info.setContentId(id[0]);
                }
                content_info.setFragment(false);
                content_info.setPath("/testpath");
                content_info.setMimeType(MimeType.IMAGE_PNG.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            var store = DatabaseImageStoreFactory.instance(datasource);
            var content = new Content(MimeType.IMAGE_PNG, sLargeImagePng);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], contentData ->
                assertArrayEquals(sLargeImagePng, (byte[]) contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentDataContentEmpty(Datasource datasource) {
        setup(datasource);
        try {
            final var id = new int[]{1};
            final var manager = DatabaseContentFactory.instance(datasource);
            final var insert = new Insert(datasource)
                .into(RifeConfig.cmf().getTableContentInfo())
                .fieldParameter("version")
                .fieldParameter("repositoryId");
            if ("org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                insert.fieldsParametersExcluded(DatabaseContentInfo.class, new String[]{"contentId"});
            } else {
                insert.fieldsParameters(DatabaseContentInfo.class);
            }
            if ("com.mysql.cj.jdbc.Driver".equals(datasource.getAliasedDriver())) {
                insert.fieldParameter("created");
            }
            manager.executeUpdate(insert, statement -> {
                var content_info = new DatabaseContentInfo();
                if (!"org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                    content_info.setContentId(id[0]);
                }
                content_info.setFragment(false);
                content_info.setPath("/testpath");
                content_info.setMimeType(MimeType.IMAGE_PNG.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            var store = DatabaseImageStoreFactory.instance(datasource);

            var content = new Content(MimeType.IMAGE_PNG, null);
            assertTrue(store.storeContentData(id[0], content, null));
            store.useContentData(id[0], Assertions::assertNull);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentDataContentNull(Datasource datasource) {
        setup(datasource);
        try {
            final var id = new int[]{1};
            final var manager = DatabaseContentFactory.instance(datasource);
            final var insert = new Insert(datasource)
                .into(RifeConfig.cmf().getTableContentInfo())
                .fieldParameter("version")
                .fieldParameter("repositoryId");
            if ("org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                insert.fieldsParametersExcluded(DatabaseContentInfo.class, new String[]{"contentId"});
            } else {
                insert.fieldsParameters(DatabaseContentInfo.class);
            }
            if ("com.mysql.cj.jdbc.Driver".equals(datasource.getAliasedDriver())) {
                insert.fieldParameter("created");
            }
            manager.executeUpdate(insert, statement -> {
                var content_info = new DatabaseContentInfo();
                if (!"org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                    content_info.setContentId(id[0]);
                }
                content_info.setFragment(false);
                content_info.setPath("/testpath");
                content_info.setMimeType(MimeType.IMAGE_PNG.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            var store = DatabaseImageStoreFactory.instance(datasource);

            assertTrue(store.storeContentData(id[0], null, null));
            store.useContentData(id[0], Assertions::assertNull);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentDataMimeTypeWithoutFormatter(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            final var id = new int[]{1};
            final var manager = DatabaseContentFactory.instance(datasource);
            final var insert = new Insert(datasource)
                .into(RifeConfig.cmf().getTableContentInfo())
                .fieldParameter("version")
                .fieldParameter("repositoryId");
            if ("org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                insert.fieldsParametersExcluded(DatabaseContentInfo.class, new String[]{"contentId"});
            } else {
                insert.fieldsParameters(DatabaseContentInfo.class);
            }
            if ("com.mysql.cj.jdbc.Driver".equals(datasource.getAliasedDriver())) {
                insert.fieldParameter("created");
            }
            manager.executeUpdate(insert, statement -> {
                var content_info = new DatabaseContentInfo();
                if (!"org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                    content_info.setContentId(id[0]);
                }
                content_info.setFragment(false);
                content_info.setPath("/testpath");
                content_info.setMimeType(MimeType.APPLICATION_XHTML.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            final var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var store = DatabaseImageStoreFactory.instance(datasource);
            var content = new Content(MimeType.APPLICATION_XHTML, data_image_gif);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], contentData ->
                assertArrayEquals(data_image_gif, (byte[]) contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStoreContentDataIllegalArgument(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);

            try {
                store.storeContentData(-1, null, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                store.storeContentData(1, new Content(MimeType.IMAGE_GIF, new Object()), null);
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
    void testStoreContentDataError(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var store = DatabaseImageStoreFactory.instance(datasource);
            var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
            try {
                store.storeContentData(2, content, null);
                assertEquals("com.mysql.cj.jdbc.Driver", datasource.getAliasedDriver());
            } catch (StoreContentDataErrorException e) {
                assertEquals(2, e.getId());
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUseContentData(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
            assertTrue(manager.storeContent("/imagegif", content, null));
            var content_info = manager.getContentInfo("/imagegif");

            var store = DatabaseImageStoreFactory.instance(datasource);

            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
            final var data_image_png = FileUtils.readBytes(image_resource_png);

            store.useContentData(content_info.getContentId(), contentData ->
                assertArrayEquals(data_image_png, (byte[]) contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUseContentDataUnknown(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);
            store.useContentData(232, Assertions::assertNull);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUseContentDataIllegalArgument(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);

            try {
                store.useContentData(-1, contentData -> fail());
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                store.useContentData(23, null);
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
    void testUseContentDataError(Datasource datasource) {
        setup(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseImageStoreFactory.instance(datasource);
            try {
                store.useContentData(2, contentData -> fail());
            } catch (UseContentDataErrorException e) {
                assertEquals(2, e.getId());
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContentData(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
            assertTrue(manager.storeContent("/imagegif", content, null));
            var content_info = manager.getContentInfo("/imagegif");

            var store = DatabaseImageStoreFactory.instance(datasource);
            assertTrue(store.hasContentData(content_info.getContentId()));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContentDataContentEmpty(Datasource datasource) {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var content = new Content(MimeType.IMAGE_PNG, null);
            assertTrue(manager.storeContent("/imagegif", content, null));
            var content_info = manager.getContentInfo("/imagegif");

            var store = DatabaseImageStoreFactory.instance(datasource);
            assertFalse(store.hasContentData(content_info.getContentId()));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContentDataUnknown(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);
            assertFalse(store.hasContentData(3));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testHasContentDataIllegalArgument(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);
            try {
                store.hasContentData(-1);
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
    void testHasContentDataError(Datasource datasource) {
        setup(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseImageStoreFactory.instance(datasource);
            try {
                store.hasContentData(2);
                fail();
            } catch (HasContentDataErrorException e) {
                assertEquals(2, e.getId());
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentData(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
            manager.storeContent("/imagegif", content, null);
            var content_info = manager.getContentInfo("/imagegif");

            var store = DatabaseImageStoreFactory.instance(datasource);
            assertTrue(store.hasContentData(content_info.getContentId()));
            assertTrue(store.deleteContentData(content_info.getContentId()));
            assertFalse(store.hasContentData(content_info.getContentId()));
            assertFalse(store.deleteContentData(content_info.getContentId()));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentDataUnknown(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);
            assertFalse(store.deleteContentData(3));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteContentDataIllegalArgument(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);
            try {
                store.deleteContentData(-1);
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
    void testDeleteContentDataError(Datasource datasource) {
        setup(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseImageStoreFactory.instance(datasource);
            try {
                store.deleteContentData(2);
                fail();
            } catch (DeleteContentDataErrorException e) {
                assertEquals(2, e.getId());
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRetrieveSize(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
            assertTrue(manager.storeContent("/imagegif", content, null));
            var content_info = manager.getContentInfo("/imagegif");

            var store = DatabaseImageStoreFactory.instance(datasource);

            var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
            var data_image_png = FileUtils.readBytes(image_resource_png);

            assertEquals(store.getSize(content_info.getContentId()), data_image_png.length);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRetrieveSizeIllegalArgument(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseImageStoreFactory.instance(datasource);

            try {
                store.getSize(-1);
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
    void testRetrieveSizeError(Datasource datasource) {
        setup(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseImageStoreFactory.instance(datasource);
            try {
                store.getSize(2);
                fail();
            } catch (RetrieveSizeErrorException e) {
                assertEquals(2, e.getId());
            }
        } finally {
            tearDown(datasource);
        }
    }
}
