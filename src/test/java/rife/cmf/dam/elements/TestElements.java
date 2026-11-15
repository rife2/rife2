/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.elements;

import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.cmf.Content;
import rife.cmf.MimeType;
import rife.cmf.dam.ContentImage;
import rife.cmf.dam.ContentQueryManager;
import rife.cmf.dam.contentmanagers.DatabaseContentFactory;
import rife.cmf.dam.contentstores.DatabaseRawStoreFactory;
import rife.cmf.elements.ServeContent;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.engine.PathInfoHandling;
import rife.engine.Site;
import rife.engine.TestServerRunner;
import rife.resources.ResourceFinderClasspath;
import rife.test.MockConversation;
import rife.tools.FileUtils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static rife.cmf.format.ImageFormatter.ContentAttribute.HIDPI;

public class TestElements {
    public void setup(Datasource datasource) {
        System.setProperty("org.slf4j.simpleLogger.log." + XmlPage.class.getName(), "error");
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
    void testServeContentRaw(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var size = (int) (65535 * 5.8);
            var binary = new byte[size];
            for (var i = 0; i < size; i++) {
                binary[i] = (byte) (i % 255);
            }

            var manager = DatabaseContentFactory.instance(datasource);
            var content = new Content(MimeType.RAW, new ByteArrayInputStream(binary)).name("mycoollib.so");
            manager.storeContent("/rawdata", content, null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                try (final var webClient = new WebClient()) {
                    UnexpectedPage page = webClient.getPage("http://localhost:8181/serve/rawdata");
                    var response = page.getWebResponse();
                    assertEquals("application/octet-stream", response.getContentType());
                    assertEquals("inline; filename=\"mycoollib.so\"", response.getResponseHeaderValue("Content-Disposition"));
                    assertEquals(size, response.getContentLength());
                    assertArrayEquals(binary, FileUtils.readBytes(response.getContentAsStream()));

                    page = webClient.getPage("http://localhost:8181/serve/rawdata/mycoollib.so");
                    response = page.getWebResponse();
                    assertEquals("application/octet-stream", response.getContentType());
                    assertEquals(size, response.getContentLength());
                    assertArrayEquals(binary, FileUtils.readBytes(response.getContentAsStream()));
                }
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentImage(Datasource datasource)
    throws Exception {
        RifeConfig.engine().setPassThroughSuffixes(Collections.emptySet());
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);

            var content = new Content(MimeType.IMAGE_PNG, data_image_gif).name("uwyn.png");
            manager.storeContent("/imagegif", content, null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                try (final var webClient = new WebClient()) {
                    var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
                    var data_image_png = FileUtils.readBytes(image_resource_png);

                    UnexpectedPage page = webClient.getPage("http://localhost:8181/serve/imagegif");
                    var response = page.getWebResponse();
                    assertEquals(MimeType.IMAGE_PNG.toString(), response.getContentType());
                    assertEquals(data_image_png.length, response.getContentLength());
                    assertArrayEquals(data_image_png, FileUtils.readBytes(response.getContentAsStream()));

                    page = webClient.getPage("http://localhost:8181/serve/imagegif/uwyn.png");
                    response = page.getWebResponse();
                    assertEquals(MimeType.IMAGE_PNG.toString(), response.getContentType());
                    assertEquals(data_image_png.length, response.getContentLength());
                    assertArrayEquals(data_image_png, FileUtils.readBytes(response.getContentAsStream()));
                }
            }
        } finally {
            tearDown(datasource);
            RifeConfig.engine().setPassThroughSuffixes(RifeConfig.EngineConfig.DEFAULT_PASS_THROUGH_SUFFIXES);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentText(Datasource datasource)
    throws Exception {
        RifeConfig.engine().setPassThroughSuffixes(Collections.emptySet());
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var data = "<i>cool beans</i><p>hot <a href=\"https://uwyn.com\">chili</a></p>";
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true).name("mytext.html");
            manager.storeContent("/textxhtml", content, null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                try (final var webClient = new WebClient()) {
                    XmlPage page = webClient.getPage("http://localhost:8181/serve/textxhtml");
                    var response = page.getWebResponse();
                    assertEquals(MimeType.APPLICATION_XHTML.toString(), response.getContentType());
                    assertEquals(data, response.getContentAsString());

                    page = webClient.getPage("http://localhost:8181/serve/textxhtml/mytext.html");
                    response = page.getWebResponse();
                    assertEquals(MimeType.APPLICATION_XHTML.toString(), response.getContentType());
                    assertEquals(data, response.getContentAsString());
                }
            }
        } finally {
            tearDown(datasource);
            RifeConfig.engine().setPassThroughSuffixes(RifeConfig.EngineConfig.DEFAULT_PASS_THROUGH_SUFFIXES);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentRepository(Datasource datasource)
    throws Exception {
        RifeConfig.engine().setPassThroughSuffixes(Collections.emptySet());
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            manager.createRepository("nondefault");

            var data = "<i>cool beans</i><p>hot <a href=\"https://uwyn.com\">chili</a></p>";
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true).name("mytext.html");
            manager.storeContent("nondefault:/textxhtml", content, null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve_repository", PathInfoHandling.CAPTURE, new ServeContent(datasource, "nondefault"));
                }
            })) {
                try (final var webClient = new WebClient()) {
                    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                    XmlPage page = webClient.getPage("http://localhost:8181/serve_repository/textxhtml");
                    var response = page.getWebResponse();
                    assertEquals(MimeType.APPLICATION_XHTML.toString(), response.getContentType());
                    assertEquals(data, response.getContentAsString());

                    page = webClient.getPage("http://localhost:8181/serve_repository/textxhtml/mytext.html");
                    response = page.getWebResponse();
                    assertEquals(MimeType.APPLICATION_XHTML.toString(), response.getContentType());
                    assertEquals(data, response.getContentAsString());

                    response = webClient.getPage("http://localhost:8181/serve/textxhtml").getWebResponse();
                    assertEquals(404, response.getStatusCode());

                    response = webClient.getPage("http://localhost:8181/servetextxhtml/mytext.html").getWebResponse();
                    assertEquals(404, response.getStatusCode());
                }
            }
        } finally {
            tearDown(datasource);
            RifeConfig.engine().setPassThroughSuffixes(RifeConfig.EngineConfig.DEFAULT_PASS_THROUGH_SUFFIXES);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentUnknown(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                try (final var webClient = new WebClient()) {
                    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                    var response = webClient.getPage("http://localhost:8181/serve/imageunknown").getWebResponse();
                    assertEquals(404, response.getStatusCode());
                }
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentNoPathinfo(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                try (final var webClient = new WebClient()) {
                    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                    var response = webClient.getPage("http://localhost:8181/serve").getWebResponse();
                    assertEquals(404, response.getStatusCode());
                }
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testContentQueryManagerContentForHtml(Datasource datasource)
    throws Exception {
        setup(datasource);
        var manager = new ContentQueryManager<>(datasource, ContentImage.class);
        manager.install();
        try {
            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);
            var content = new ContentImage()
                .name("the content name")
                .image(data_image_gif);
            content.getConstrainedProperty("image").contentAttribute(HIDPI, false);

            var id = manager.save(content);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    var serve = get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                    get("/contentforhtml", c -> {
                        var id = c.parameterInt("id");

                        var manager = new ContentQueryManager<>(datasource, ContentImage.class);
                        var content = manager.restore(id);
                        c.print(manager.getContentForHtml(id, "image", c, serve));
                        c.print(manager.getContentForHtml(content, "image", c, serve));
                    });
                }
            })) {
                try (final var webClient = new WebClient()) {
                    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                    HtmlPage page = webClient.getPage("http://localhost:8181/contentforhtml?id=" + id);
                    assertEquals("<img src=\"http://localhost:8181/serve/contentimage/" + id + "/image\" width=\"1280\" height=\"406\" alt=\"\" />" +
                                 "<img src=\"http://localhost:8181/serve/contentimage/" + id + "/image\" width=\"1280\" height=\"406\" alt=\"\" />", page.getWebResponse().getContentAsString());
                }
            }
        } finally {
            manager.remove();
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testContentQueryManagerContentForHtmlHiDpi(Datasource datasource)
    throws Exception {
        setup(datasource);
        var manager = new ContentQueryManager<>(datasource, ContentImage.class);
        manager.install();
        try {
            var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
            var data_image_gif = FileUtils.readBytes(image_resource_gif);
            var content = new ContentImage()
                .name("the content name")
                .image(data_image_gif);

            var id = manager.save(content);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    var serve = get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                    get("/contentforhtml", c -> {
                        var id = c.parameterInt("id");

                        var manager = new ContentQueryManager<>(datasource, ContentImage.class);
                        var content = manager.restore(id);
                        c.print(manager.getContentForHtml(id, "image", c, serve));
                        c.print(manager.getContentForHtml(content, "image", c, serve));
                    });
                }
            })) {
                try (final var webClient = new WebClient()) {
                    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                    HtmlPage page = webClient.getPage("http://localhost:8181/contentforhtml?id=" + id);
                    assertEquals("<img src=\"http://localhost:8181/serve/contentimage/" + id + "/image\" width=\"640\" height=\"203\" alt=\"\" />" +
                                 "<img src=\"http://localhost:8181/serve/contentimage/" + id + "/image\" width=\"640\" height=\"203\" alt=\"\" />", page.getWebResponse().getContentAsString());
                }
            }
        } finally {
            manager.remove();
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentModifiedSince(Datasource datasource)
    throws Exception {
        RifeConfig.engine().setPassThroughSuffixes(Collections.emptySet());
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);

            var data = "<i>cool beans</i><p>hot <a href=\"https://uwyn.com\">chili</a></p>";
            var content = new Content(MimeType.APPLICATION_XHTML, data).fragment(true);
            manager.storeContent("/textxhtml", content, null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                try (final var webClient = new WebClient()) {
                    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
                    // the cache would revalidate with the ETag, which takes precedence
                    webClient.getCache().setMaxSize(0);

                    var request = new WebRequest(new URL("http://localhost:8181/serve/textxhtml"));
                    request.setAdditionalHeader("If-Modified-Since", "24 Aug 2204 15:14:06 GMT");
                    assertEquals(304, webClient.getPage(request).getWebResponse().getStatusCode());

                    request = new WebRequest(new URL("http://localhost:8181/serve/textxhtml"));
                    request.setAdditionalHeader("If-Modified-Since", "1 Oct 1999 09:23:10 GMT");
                    assertEquals(200, webClient.getPage(request).getWebResponse().getStatusCode());
                }
            }
        } finally {
            tearDown(datasource);
            RifeConfig.engine().setPassThroughSuffixes(RifeConfig.EngineConfig.DEFAULT_PASS_THROUGH_SUFFIXES);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentEtag(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);
            manager.storeContent("/tagged", new Content(MimeType.RAW, "first".getBytes(StandardCharsets.UTF_8)), null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                var response = serve("/tagged");
                assertEquals(200, response.statusCode());
                var etag = response.headers().firstValue("ETag").orElseThrow();
                assertTrue(etag.matches("\"[0-9a-f]+-[0-9a-f]+\""), etag);

                var not_modified = serve("/tagged", "If-None-Match", etag);
                assertEquals(304, not_modified.statusCode());
                assertEquals(etag, not_modified.headers().firstValue("ETag").orElse(null));
                assertEquals(304, serve("/tagged", "If-None-Match", "\"other\", W/" + etag).statusCode());
                assertEquals(304, serve("/tagged", "If-None-Match", "*").statusCode());
                assertEquals(200, serve("/tagged", "If-None-Match", "\"other\"").statusCode());

                // If-None-Match takes precedence over If-Modified-Since
                assertEquals(200, serve("/tagged", "If-None-Match", "\"other\"",
                    "If-Modified-Since", "Fri, 31 Dec 2100 23:59:59 GMT").statusCode());

                // storing the content again changes its tag
                manager.storeContent("/tagged", new Content(MimeType.RAW, "second".getBytes(StandardCharsets.UTF_8)), null);
                var changed = serve("/tagged", "If-None-Match", etag);
                assertEquals(200, changed.statusCode());
                assertEquals("second", changed.body());
                assertNotEquals(etag, changed.headers().firstValue("ETag").orElse(null));

            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentRange(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var size = 200000;
            var data = new byte[size];
            for (var i = 0; i < size; i++) {
                data[i] = (byte) (i % 251);
            }
            var manager = DatabaseContentFactory.instance(datasource);
            manager.storeContent("/media", new Content(MimeType.RAW, new ByteArrayInputStream(data)), null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                var full = serveBytes("/media");
                assertEquals(200, full.statusCode());
                assertEquals("bytes", full.headers().firstValue("Accept-Ranges").orElse(null));
                assertArrayEquals(data, full.body());
                var etag = full.headers().firstValue("ETag").orElseThrow();
                var last_modified = full.headers().firstValue("Last-Modified").orElseThrow();

                assertRange(data, serveBytes("/media", "Range", "bytes=0-9"), 0, 9);
                // across the boundary of the stored chunks
                assertRange(data, serveBytes("/media", "Range", "bytes=65530-65545"), 65530, 65545);
                assertRange(data, serveBytes("/media", "Range", "bytes=150000-"), 150000, size - 1);
                assertRange(data, serveBytes("/media", "Range", "bytes=-5"), size - 5, size - 1);
                assertRange(data, serveBytes("/media", "Range", "bytes=199990-300000"), 199990, size - 1);

                var unsatisfiable = serveBytes("/media", "Range", "bytes=" + size + "-");
                assertEquals(416, unsatisfiable.statusCode());
                assertEquals("bytes */" + size, unsatisfiable.headers().firstValue("Content-Range").orElse(null));

                // a range of another version gives the whole content
                assertRange(data, serveBytes("/media", "Range", "bytes=0-9", "If-Range", etag), 0, 9);
                assertRange(data, serveBytes("/media", "Range", "bytes=0-9", "If-Range", last_modified), 0, 9);
                assertEquals(200, serveBytes("/media", "Range", "bytes=0-9", "If-Range", "\"other\"").statusCode());
                assertEquals(200, serveBytes("/media", "Range", "bytes=0-9", "If-Range", "W/" + etag).statusCode());

            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentEmptyRaw(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);
            manager.storeContent("/empty", new Content(MimeType.RAW, new byte[0]), null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                // empty data is content too, whatever the request asks for
                var full = serveBytes("/empty");
                assertEquals(200, full.statusCode());
                assertEquals(0, full.body().length);
                var etag = full.headers().firstValue("ETag").orElseThrow();
                assertEquals(304, serveBytes("/empty", "If-None-Match", etag).statusCode());
                var unsatisfiable = serveBytes("/empty", "Range", "bytes=0-");
                assertEquals(416, unsatisfiable.statusCode());
                assertEquals("bytes */0", unsatisfiable.headers().firstValue("Content-Range").orElse(null));
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentDataRangeArguments(Datasource datasource) {
        setup(datasource);
        try {
            var store = DatabaseRawStoreFactory.instance(datasource);
            var errors = new java.util.ArrayList<String>();
            var conversation = new MockConversation(new Site() {
                public void setup() {
                    get("/range", c -> {
                        for (var range : new long[][]{{-1, 10}, {5, 2}}) {
                            try {
                                store.serveContentData(c, 1, range[0], range[1]);
                            } catch (IllegalArgumentException e) {
                                errors.add(e.getMessage());
                            }
                        }
                    });
                }
            });
            conversation.doRequest("/range");
            assertEquals(List.of("start can't be negative", "end can't lie before start"), errors);
        } finally {
            tearDown(datasource);
        }
    }

    // how every kind of stored content is cached and served in parts
    private record ServedCase(String path, Content content, String contentType, boolean weak, boolean ranges) {
    }

    private static List<ServedCase> servedCases()
    throws Exception {
        var raw = new byte[100000];
        for (var i = 0; i < raw.length; i++) {
            raw[i] = (byte) (i % 239);
        }
        var png = FileUtils.readBytes(ResourceFinderClasspath.instance().getResource("uwyn.png"));
        var jpeg = FileUtils.readBytes(ResourceFinderClasspath.instance().getResource("uwyn.jpg"));
        var gif = FileUtils.readBytes(ResourceFinderClasspath.instance().getResource("uwyn.gif"));
        return List.of(
            new ServedCase("/png", new Content(MimeType.IMAGE_PNG, png), "image/png", false, false),
            new ServedCase("/jpeg", new Content(MimeType.IMAGE_JPEG, jpeg), "image/jpeg", false, false),
            new ServedCase("/gif", new Content(MimeType.IMAGE_GIF, gif), "image/gif", false, false),
            new ServedCase("/xhtml", new Content(MimeType.APPLICATION_XHTML, "<p>page</p>").fragment(true), "application/xhtml+xml", true, false),
            new ServedCase("/text", new Content(MimeType.TEXT_PLAIN, "plain text"), "text/plain", true, false),
            new ServedCase("/xml", new Content(MimeType.TEXT_XML, "<notes><note>xml</note></notes>"), "text/xml", true, false),
            new ServedCase("/raw", new Content(MimeType.RAW, raw.clone()), "application/octet-stream", false, true),
            new ServedCase("/song", new Content(MimeType.RAW, raw.clone()).name("song.mp3"), "audio/mpeg", false, true),
            new ServedCase("/video", new Content(MimeType.RAW, raw.clone()).attribute("content-type", "video/mp4"), "video/mp4", false, true),
            new ServedCase("/notes", new Content(MimeType.RAW, "the notes".getBytes(StandardCharsets.UTF_8)).name("notes.txt"), "text/plain", true, false));
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentCachingAndRanges(Datasource datasource)
    throws Exception {
        setup(datasource);
        var gzip = RifeConfig.engine().getGzipCompression();
        try {
            var manager = DatabaseContentFactory.instance(datasource);
            var cases = servedCases();
            for (var served : cases) {
                manager.storeContent(served.path(), served.content(), null);
            }

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                for (var served : cases) {
                    assertServed(served, served.weak(), served.ranges());
                }

                // without compression, every tag is strong, but only raw data
                // is served in parts, the other stores don't keep their bytes
                RifeConfig.engine().setGzipCompression(false);
                for (var served : cases) {
                    var raw = served.content().getMimeType() == MimeType.RAW;
                    assertServed(served, false, raw);
                }
            }
        } finally {
            RifeConfig.engine().setGzipCompression(gzip);
            tearDown(datasource);
        }
    }

    private static void assertServed(ServedCase served, boolean weak, boolean ranges)
    throws Exception {
        var path = served.path();
        var full = serveBytes(path);
        assertEquals(200, full.statusCode(), path);
        var content_type = full.headers().firstValue("Content-Type").orElse("");
        assertTrue(content_type.startsWith(served.contentType()), path);
        // only text takes a charset
        var textual = served.contentType().startsWith("text/") || served.contentType().endsWith("+xml");
        assertEquals(textual, content_type.contains("charset="), path + " " + content_type);
        assertTrue(full.body().length > 3, path);
        var etag = full.headers().firstValue("ETag").orElseThrow();
        assertEquals(weak, etag.startsWith("W/"), path + " " + etag);
        var last_modified = full.headers().firstValue("Last-Modified").orElseThrow();
        assertEquals(ranges, full.headers().firstValue("Accept-Ranges").isPresent(), path);

        // revalidation, with the cache headers on the not modified response
        var not_modified = serveBytes(path, "If-None-Match", etag);
        assertEquals(304, not_modified.statusCode(), path);
        assertEquals(etag, not_modified.headers().firstValue("ETag").orElse(null), path);
        assertEquals(last_modified, not_modified.headers().firstValue("Last-Modified").orElse(null), path);
        assertTrue(not_modified.headers().firstValue("Cache-Control").isPresent(), path);
        // a compressible response varies on the compression, also when not modified
        assertEquals(weak, not_modified.headers().allValues("Vary").contains("Accept-Encoding"), path);
        assertEquals(0, not_modified.body().length, path);
        var opaque = etag.startsWith("W/") ? etag.substring(2) : etag;
        assertEquals(304, serveBytes(path, "If-None-Match", "W/" + opaque).statusCode(), path);
        assertEquals(304, serveBytes(path, "If-None-Match", "\"other\", " + opaque).statusCode(), path);
        assertEquals(304, serveBytes(path, "If-None-Match", "*").statusCode(), path);
        assertEquals(200, serveBytes(path, "If-None-Match", "\"other\"").statusCode(), path);
        assertEquals(304, serveBytes(path, "If-Modified-Since", last_modified).statusCode(), path);
        assertEquals(200, serveBytes(path, "If-Modified-Since", "Fri, 01 Oct 1999 09:23:10 GMT").statusCode(), path);
        assertEquals(200, serveBytes(path, "If-Modified-Since", "not a date").statusCode(), path);
        assertEquals(200, serveBytes(path, "If-None-Match", "\"other\"", "If-Modified-Since", last_modified).statusCode(), path);

        // parts of the content
        var part = serveBytes(path, "Range", "bytes=1-3");
        var head = serveBytes(path, "HEAD", "Range", "bytes=1-3");
        if (ranges) {
            var length = full.body().length;
            assertEquals(206, part.statusCode(), path);
            assertEquals("bytes 1-3/" + length, part.headers().firstValue("Content-Range").orElse(null), path);
            assertArrayEquals(java.util.Arrays.copyOfRange(full.body(), 1, 4), part.body(), path);
            // ranges are only defined for GET
            assertEquals(200, head.statusCode(), path);
            assertEquals(String.valueOf(length), head.headers().firstValue("Content-Length").orElse(null), path);
            assertTrue(head.headers().firstValue("Content-Range").isEmpty(), path);
            assertEquals(0, head.body().length, path);
            assertEquals(416, serveBytes(path, "Range", "bytes=" + length + "-").statusCode(), path);
            assertEquals(206, serveBytes(path, "Range", "bytes=1-3", "If-Range", etag).statusCode(), path);
            assertEquals(206, serveBytes(path, "Range", "bytes=1-3", "If-Range", last_modified).statusCode(), path);
            assertEquals(200, serveBytes(path, "Range", "bytes=1-3", "If-Range", "\"other\"").statusCode(), path);
            assertEquals(200, serveBytes(path, "Range", "bytes=1-3", "If-Range", "Fri, 01 Oct 1999 09:23:10 GMT").statusCode(), path);
        } else {
            assertEquals(200, part.statusCode(), path);
            assertTrue(part.headers().firstValue("Content-Range").isEmpty(), path);
            assertArrayEquals(full.body(), part.body(), path);
            assertEquals(200, head.statusCode(), path);
        }
    }

    private static void assertRange(byte[] data, HttpResponse<byte[]> response, int start, int end) {
        assertEquals(206, response.statusCode());
        assertEquals("bytes " + start + "-" + end + "/" + data.length, response.headers().firstValue("Content-Range").orElse(null));
        assertArrayEquals(java.util.Arrays.copyOfRange(data, start, end + 1), response.body());
    }

    // a first argument of HEAD sends a HEAD request
    private static HttpResponse<byte[]> serveBytes(String path, String... headers)
    throws Exception {
        var request = HttpRequest.newBuilder(new URI("http://localhost:8181/serve" + path));
        if (headers.length > 0 && "HEAD".equals(headers[0])) {
            request.method("HEAD", HttpRequest.BodyPublishers.noBody());
            headers = java.util.Arrays.copyOfRange(headers, 1, headers.length);
        }
        if (headers.length > 0) {
            request.headers(headers);
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private static HttpResponse<String> serve(String path, String... headers)
    throws Exception {
        var request = HttpRequest.newBuilder(new URI("http://localhost:8181/serve" + path));
        if (headers.length > 0) {
            request.headers(headers);
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testServeContentEncodedPath(Datasource datasource)
    throws Exception {
        setup(datasource);
        try {
            var manager = DatabaseContentFactory.instance(datasource);
            manager.storeContent("/c++ notes", new Content(MimeType.RAW, "the notes".getBytes(StandardCharsets.UTF_8)), null);

            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
                }
            })) {
                var client = HttpClient.newHttpClient();
                var request = HttpRequest.newBuilder(new URI("http://localhost:8181/serve/c++%20notes")).build();
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode());
                assertEquals("application/octet-stream", response.headers().firstValue("Content-Type").orElse(null));
                assertEquals("the notes", response.body());
            }
        } finally {
            tearDown(datasource);
        }
    }
}
