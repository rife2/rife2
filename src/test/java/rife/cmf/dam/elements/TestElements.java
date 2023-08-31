/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.elements;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.cmf.Content;
import rife.cmf.MimeType;
import rife.cmf.dam.ContentImage;
import rife.cmf.dam.ContentQueryManager;
import rife.cmf.dam.contentmanagers.DatabaseContentFactory;
import rife.cmf.elements.ServeContent;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.engine.*;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static rife.cmf.format.ImageFormatter.ContentAttribute.HIDPI;

public class TestElements {
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
}
