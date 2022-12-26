/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.cmf.dam.contentstores.exceptions.*;

import rife.cmf.Content;
import rife.cmf.ContentInfo;
import rife.cmf.ContentRepository;
import rife.cmf.MimeType;
import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.cmf.dam.contentmanagers.DatabaseContentFactory;
import rife.cmf.dam.contentmanagers.DatabaseContentInfo;
import rife.config.RifeConfig;
import rife.database.*;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Date;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseRawStore {
    private byte[] getSmallRaw() {
        var size = 60 * 1024; // 60kb
        var binary = new byte[size];
        for (var i = 0; i < size; i++) {
            binary[i] = (byte) (i % 127);
        }

        return binary;
    }

    private byte[] getLargeRaw() {
        var size = 1024 * 1024 * 4; // 4Mb
        var binary = new byte[size];
        for (var i = 0; i < size; i++) {
            binary[i] = (byte) (i % 255);
        }

        return binary;
    }

    public void setUp(Datasource datasource) {
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
    public void testInstallError(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
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
    public void testRemoveError(Datasource datasource) {
        setUp(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseRawStoreFactory.instance(datasource);
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
    public void testGetSupportedMimeTypes(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
            assertTrue(store.getSupportedMimeTypes().size() > 0);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetContentType(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
            var info = new ContentInfo();
            info.setMimeType(MimeType.RAW.toString());
            assertNull(store.getContentType(info));
            info.setAttributes(new HashMap<>() {{
                put("content-type", "application/something");
            }});
            assertNotNull(store.getContentType(info));
            info.setAttributes(null);
            assertNull(store.getContentType(info));
            info.setName("test.ogg");
            assertNotNull(store.getContentType(info));
            info.setName("test.unknown");
            assertNull(store.getContentType(info));
            info.setMimeType(MimeType.APPLICATION_XHTML.toString());
            assertNull(store.getContentType(info));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetFormatter(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
            assertNotNull(store.getFormatter(MimeType.RAW, false));
            assertNull(store.getFormatter(MimeType.APPLICATION_XHTML, true));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStoreContentDataStream(Datasource datasource) {
        setUp(datasource);
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
                content_info.setMimeType(MimeType.RAW.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            final var raw = getSmallRaw();

            var store = DatabaseRawStoreFactory.instance(datasource);
            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], new ContentDataUserWithoutResult() {
                public void useContentData(Object contentData)
                throws InnerClassException {
                    try {
                        var received = FileUtils.readBytes((InputStream) contentData);
                        assertArrayEquals(raw, received);
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
    public void testStoreContentDataBytes(Datasource datasource) {
        setUp(datasource);
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
                content_info.setMimeType(MimeType.RAW.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            final var raw = getSmallRaw();

            var store = DatabaseRawStoreFactory.instance(datasource);
            var content = new Content(MimeType.RAW, raw);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], new ContentDataUserWithoutResult() {
                public void useContentData(Object contentData)
                throws InnerClassException {
                    try {
                        var received = FileUtils.readBytes((InputStream) contentData);
                        assertArrayEquals(raw, received);
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
    public void testStoreContentDataLargeStream(Datasource datasource) {
        setUp(datasource);
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
                content_info.setMimeType(MimeType.RAW.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            final var raw = getLargeRaw();

            var store = DatabaseRawStoreFactory.instance(datasource);
            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], new ContentDataUserWithoutResult() {
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
    public void testStoreContentDataLargeBytes(Datasource datasource) {
        setUp(datasource);
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
                content_info.setMimeType(MimeType.RAW.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            final var raw = getLargeRaw();

            var store = DatabaseRawStoreFactory.instance(datasource);
            var content = new Content(MimeType.RAW, raw);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], new ContentDataUserWithoutResult() {
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
    public void testStoreContentDataContentEmpty(Datasource datasource) {
        setUp(datasource);
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
                content_info.setMimeType(MimeType.RAW.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            var store = DatabaseRawStoreFactory.instance(datasource);

            var content = new Content(MimeType.RAW, null);
            assertTrue(store.storeContentData(id[0], content, null));
            store.useContentData(id[0], Assertions::assertNull);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStoreContentDataContentNull(Datasource datasource) {
        setUp(datasource);
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
                content_info.setMimeType(MimeType.RAW.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            var store = DatabaseRawStoreFactory.instance(datasource);

            assertTrue(store.storeContentData(id[0], null, null));
            store.useContentData(id[0], Assertions::assertNull);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStoreContentDataMimeTypeWithoutFormatter(Datasource datasource) {
        setUp(datasource);
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
                content_info.setMimeType(MimeType.IMAGE_GIF.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            final var raw = getSmallRaw();

            var store = DatabaseRawStoreFactory.instance(datasource);
            var content = new Content(MimeType.IMAGE_GIF, new ByteArrayInputStream(raw));
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], new ContentDataUserWithoutResult() {
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
    public void testStoreContentDataIllegalArgument(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);

            try {
                store.storeContentData(-1, null, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                store.storeContentData(1, new Content(MimeType.RAW, new Object()), null);
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
    public void testStoreContentDataError(Datasource datasource) {
        setUp(datasource);
        try {
            var raw = getSmallRaw();

            var store = DatabaseRawStoreFactory.instance(datasource);
            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
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
    public void testUseContentData(Datasource datasource) {
        setUp(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            final var raw = getSmallRaw();

            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
            assertTrue(manager.storeContent("/rawdata", content, null));
            var content_info = manager.getContentInfo("/rawdata");

            var store = DatabaseRawStoreFactory.instance(datasource);

            store.useContentData(content_info.getContentId(), new ContentDataUserWithoutResult() {
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
    public void testUseContentDataUnknown(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);

            store.useContentData(232, Assertions::assertNull);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testUseContentDataIllegalArgument(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);

            try {
                store.useContentData(-1, contentData -> fail());
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                store.useContentData(-1, null);
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
    public void testUseContentDataError(Datasource datasource) {
        setUp(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseRawStoreFactory.instance(datasource);
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
    public void testHasContentData(Datasource datasource) {
        setUp(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var raw = getSmallRaw();

            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
            assertTrue(manager.storeContent("/rawdata", content, null));
            var content_info = manager.getContentInfo("/rawdata");

            var store = DatabaseRawStoreFactory.instance(datasource);
            assertTrue(store.hasContentData(content_info.getContentId()));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testHasContentDataContentEmpty(Datasource datasource) {
        setUp(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var content = new Content(MimeType.RAW, null);
            assertTrue(manager.storeContent("/rawdata", content, null));
            var content_info = manager.getContentInfo("/rawdata");

            var store = DatabaseRawStoreFactory.instance(datasource);
            assertFalse(store.hasContentData(content_info.getContentId()));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testHasContentDataUnknown(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
            assertFalse(store.hasContentData(3));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testHasContentDataIllegalArgument(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
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
    public void testHasContentDataError(Datasource datasource) {
        setUp(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseRawStoreFactory.instance(datasource);
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
    public void testDeleteContentData(Datasource datasource) {
        setUp(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var raw = getSmallRaw();

            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
            assertTrue(manager.storeContent("/rawdata", content, null));
            var content_info = manager.getContentInfo("/rawdata");

            var store = DatabaseRawStoreFactory.instance(datasource);
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
    public void testDeleteContentDataUnknown(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
            assertFalse(store.deleteContentData(3));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testDeleteContentDataIllegalArgument(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
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
    public void testDeleteContentDataError(Datasource datasource) {
        setUp(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseRawStoreFactory.instance(datasource);
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
    public void testRetrieveSize(Datasource datasource) {
        setUp(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var raw = getSmallRaw();

            var content = new Content(MimeType.RAW, new ByteArrayInputStream(raw));
            assertTrue(manager.storeContent("/rawdata", content, null));
            var content_info = manager.getContentInfo("/rawdata");

            var store = DatabaseRawStoreFactory.instance(datasource);

            assertEquals(store.getSize(content_info.getContentId()), raw.length);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRetrieveSizeIllegalArgument(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);

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
    public void testRetrieveSizeError(Datasource datasource) {
        setUp(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseRawStoreFactory.instance(datasource);
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
