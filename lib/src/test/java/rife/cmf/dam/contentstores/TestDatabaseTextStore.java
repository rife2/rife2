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
import rife.cmf.dam.contentmanagers.DatabaseContentFactory;
import rife.cmf.dam.contentmanagers.DatabaseContentInfo;
import rife.config.RifeConfig;
import rife.database.*;
import rife.database.queries.Insert;
import rife.database.queries.Select;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseTextStore {
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
            var info = new ContentInfo();
            info.setMimeType(MimeType.APPLICATION_XHTML.toString());
            assertNotNull(store.getContentType(info));
            info.setMimeType(MimeType.IMAGE_GIF.toString());
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
            var store = DatabaseTextStoreFactory.instance(datasource);
            assertNotNull(store.getFormatter(MimeType.APPLICATION_XHTML, false));
            assertNotNull(store.getFormatter(MimeType.TEXT_PLAIN, false));
            assertNull(store.getFormatter(MimeType.IMAGE_GIF, true));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStoreContentDataXhtml(Datasource datasource) {
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

            final var data = "<i>cool beans</i><p>hôt <a href=\"http://uwyn.com\">chili</a></p>";

            var store = DatabaseTextStoreFactory.instance(datasource);
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], contentData -> {
                var data_encoded = "<i>cool beans</i><p>h&ocirc;t <a href=\"http://uwyn.com\">chili</a></p>";
                assertEquals(data_encoded, contentData);
            });
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStoreContentDataXhtmlLarge(Datasource datasource) {
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

            final var data_part = "<i>cool beans</i><p>hôt <a href=\"http://uwyn.com\">chili</a></p>";
            final var data = data_part.repeat(14000);

            var store = DatabaseTextStoreFactory.instance(datasource);
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], contentData -> {
                var data_encoded_part = "<i>cool beans</i><p>h&ocirc;t <a href=\"http://uwyn.com\">chili</a></p>";
                assertEquals(data_encoded_part.repeat(14000), contentData);
            });
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStoreContentDataTextPlain(Datasource datasource) {
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
                content_info.setMimeType(MimeType.TEXT_PLAIN.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            final var data = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Suspendisse lacinia, neque eget euismod scelerisque, arcu est accumsan lectus, id accumsan elit nunc eget elit.";

            var store = DatabaseTextStoreFactory.instance(datasource);
            var content = new Content(MimeType.TEXT_PLAIN, data).fragment(true);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], contentData -> assertEquals(data, contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStoreContentDataTextPlainLarge(Datasource datasource) {
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
                content_info.setMimeType(MimeType.TEXT_PLAIN.toString());
                content_info.setCreated(new Timestamp(new Date().getTime()));
                statement
                    .setInt("version", 1)
                    .setInt("repositoryId", manager.executeGetFirstInt(new Select(datasource)
                        .from(RifeConfig.cmf().getTableContentRepository())
                        .field("repositoryId")
                        .where("name", "=", ContentRepository.DEFAULT)))
                    .setBean(content_info);
            });

            final var data_part = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Suspendisse lacinia, neque eget euismod scelerisque, arcu est accumsan lectus, id accumsan elit nunc eget elit.";
            final var data = data_part.repeat(5000);

            var store = DatabaseTextStoreFactory.instance(datasource);
            var content = new Content(MimeType.TEXT_PLAIN, data).fragment(true);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], contentData -> assertEquals(data, contentData));
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

            var store = DatabaseTextStoreFactory.instance(datasource);
            var content = new Content(MimeType.APPLICATION_XHTML, null);
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

            var store = DatabaseTextStoreFactory.instance(datasource);
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

            final var data = "<i>cool beans</i><p>hot <a href=\"http://uwyn.com\">chili</a></p>";

            var store = DatabaseTextStoreFactory.instance(datasource);
            var content = new Content(MimeType.IMAGE_PNG, data).fragment(true);
            assertTrue(store.storeContentData(id[0], content, null));

            store.useContentData(id[0], contentData -> assertEquals(data, contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStoreContentDataIllegalArgument(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseTextStoreFactory.instance(datasource);

            try {
                store.storeContentData(-1, null, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                store.storeContentData(1, new Content(MimeType.APPLICATION_XHTML, new Object()), null);
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
            var data = "<i>cool beans</i><p>hot <a href=\"http://uwyn.com\">chili</a></p>";
            var store = DatabaseTextStoreFactory.instance(datasource);
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true);
            try {
                store.storeContentData(2, content, null);
                assertTrue("com.mysql.cj.jdbc.Driver".equals(datasource.getAliasedDriver()));
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

            final var data = "<i>cool beans</i><p>hot <a href=\"http://uwyn.com\">chili</a></p>";

            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true);
            assertTrue(manager.storeContent("/testxhtml", content, null));
            var content_info = manager.getContentInfo("/testxhtml");

            var store = DatabaseTextStoreFactory.instance(datasource);

            store.useContentData(content_info.getContentId(), contentData -> assertEquals(data, contentData));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testUseContentDataUnknown(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);

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
    public void testUseContentDataError(Datasource datasource) {
        setUp(datasource);
        try {
            DatabaseContentFactory.instance(datasource).remove();
            var store = DatabaseTextStoreFactory.instance(datasource);
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

            var data = "<i>cool beans</i><p>hot <a href=\"http://uwyn.com\">chili</a></p>";
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true);
            assertTrue(manager.storeContent("/testxhtml", content, null));
            var content_info = manager.getContentInfo("/testxhtml");

            var store = DatabaseTextStoreFactory.instance(datasource);
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

            var content = new Content(MimeType.APPLICATION_XHTML, null);
            assertTrue(manager.storeContent("/testxhtml", content, null));
            var content_info = manager.getContentInfo("/testxhtml");

            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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

            var data = "<i>cool beans</i><p>hot <a href=\"http://uwyn.com\">chili</a></p>";
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true);
            assertTrue(manager.storeContent("/testxhtml", content, null));
            var content_info = manager.getContentInfo("/testxhtml");

            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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
            var store = DatabaseTextStoreFactory.instance(datasource);
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

            var data = "<i>cool beans</i><p>hot <a href=\"http://uwyn.com\">chili</a></p>";
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true);
            assertTrue(manager.storeContent("/testxhtml", content, null));
            var content_info = manager.getContentInfo("/testxhtml");

            var store = DatabaseTextStoreFactory.instance(datasource);

            assertEquals(store.getSize(content_info.getContentId()), data.length());
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRetrieveSizeIllegalArgument(Datasource datasource) {
        setUp(datasource);
        try {
            var store = DatabaseTextStoreFactory.instance(datasource);

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
            var store = DatabaseTextStoreFactory.instance(datasource);
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
