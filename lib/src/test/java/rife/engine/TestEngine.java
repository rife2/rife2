/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.engine.annotations.Parameter;
import rife.engine.exceptions.AnnotatedElementInstanceFieldException;
import rife.engine.exceptions.EngineException;
import rife.template.TemplateFactory;
import rife.tools.IntegerUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public class TestEngine {
    @Test
    void testSimplePlain()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple/plain", c -> {
                    c.setContentType("text/plain");
                    c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo());
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                final TextPage page = webClient.getPage("http://localhost:8181/simple/plain");
                assertEquals("text/plain", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:", page.getContent());
            }
        }
    }

    @Test
    void testSimpleHtml()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple/html", c -> c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo()));
            }
        })) {
            try (final var webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/simple/html");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:", page.asNormalizedText());
            }
        }
    }

    @Test
    void testSimplePathInfo()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple/pathinfo", PathInfoHandling.CAPTURE, c -> c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo()));
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page;

                page = webClient.getPage("http://localhost:8181/simple/pathinfo/some/path");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:some/path", page.asNormalizedText());

                page = webClient.getPage("http://localhost:8181/simple/pathinfo/");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:", page.asNormalizedText());

                page = webClient.getPage("http://localhost:8181/simple/pathinfo");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:", page.asNormalizedText());

                page = webClient.getPage("http://localhost:8181/simple/pathinfo/another_path_info");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:another_path_info", page.asNormalizedText());

                try {
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
                    webClient.getPage("http://localhost:8181/simple/pathinfoddd");
                    fail("Expecting 404");
                } catch (FailingHttpStatusCodeException e) {
                    // success
                }
            }
        }
    }

    @Test
    void testPathInfoMapping()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/pathinfo/map", PathInfoHandling.MAP(m -> m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")), c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo());
                    c.print(":" + c.parameter("param1"));
                    c.print(":" + c.parameter("param2"));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page;

                page = webClient.getPage("http://localhost:8181/pathinfo/map/text/val1/x4321");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:text/val1/x4321:val1:4321", page.asNormalizedText());

                try {
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
                    webClient.getPage("http://localhost:8181/pathinfo/map/ddd");
                    fail("Expecting 404");
                } catch (FailingHttpStatusCodeException e) {
                    // success
                }
            }
        }
    }

    @Test
    void testPathInfoMappingMultiple()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/pathinfo/map",
                    PathInfoHandling.MAP(
                        m -> m.t("text").s().p("param1"),
                        m -> m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")
                    ), c -> {
                        c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo());
                        c.print(":" + c.parameter("param1"));
                        c.print(":" + c.parameter("param2"));
                    });
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page;

                page = webClient.getPage("http://localhost:8181/pathinfo/map/text/val1/x4321");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:text/val1/x4321:val1:4321", page.asNormalizedText());

                page = webClient.getPage("http://localhost:8181/pathinfo/map/text/val1");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:text/val1:val1:null", page.asNormalizedText());

                try {
                    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
                    webClient.getPage("http://localhost:8181/pathinfo/map/ddd");
                    fail("Expecting 404");
                } catch (FailingHttpStatusCodeException e) {
                    // success
                }
            }
        }
    }

    @Test
    void testPathInfoMappingUrlGeneration()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                var path_info = get("/pathinfo/map", PathInfoHandling.MAP(m -> m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")), c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo());
                    c.print(":" + c.parameter("param1"));
                    c.print(":" + c.parameter("param2"));
                });
                get("/", c -> {
                    c.print(c.urlFor(path_info).param("param1", "v1").param("param2", "412"));
                });

            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page;

                page = webClient.getPage("http://localhost:8181/");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("http://localhost:8181/pathinfo/map/text/v1/x412", page.getWebResponse().getContentAsString());


                page = webClient.getPage(page.getWebResponse().getContentAsString());
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:text/v1/x412:v1:412", page.asNormalizedText());
            }
        }
    }

    @Test
    void testHeaders()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/headers", c -> {
                    c.addHeader("Content-Disposition", "attachment; filename=thefile.zip");
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    cal.set(2002, Calendar.OCTOBER, 25, 19, 20, 58);
                    c.addDateHeader("DateHeader", cal.getTimeInMillis());
                    c.addHeader("IntHeader", 1212);

                    c.print("headers");
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/headers");
                assertTrue(page.getWebResponse().getResponseHeaders().size() > 4);
                assertEquals("attachment; filename=thefile.zip", page.getWebResponse().getResponseHeaderValue("CONTENT-DISPOSITION"));
                assertEquals("Fri, 25 Oct 2002 19:20:58 GMT", page.getWebResponse().getResponseHeaderValue("DATEHEADER"));
                assertEquals("1212", page.getWebResponse().getResponseHeaderValue("INTHEADER"));
            }
        }
    }

    @Test
    void testCookies()
    throws IOException {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/cookies1", c -> {
                    var names = c.cookieNames();

                    if (names.size() == 3 &&
                        names.contains("cookie1") &&
                        names.contains("cookie2") &&
                        names.contains("cookie3") &&
                        c.hasCookie("cookie1") &&
                        c.hasCookie("cookie2") &&
                        c.hasCookie("cookie3")) {
                        c.addCookie(new CookieBuilder("cookie3", c.cookieValue("cookie1")));
                        c.addCookie(new CookieBuilder("cookie4", c.cookieValue("cookie2")));
                    }

                    c.print("source");
                });

                get("/cookies2", c -> c.print(c.cookieValue("cookie2") + "," + c.cookieValue("cookie3") + "," + c.cookieValue("cookie4")));
            }
        })) {
            try (final var webClient = new WebClient()) {
                var manager = webClient.getCookieManager();
                manager.addCookie(new com.gargoylesoftware.htmlunit.util.Cookie("localhost", "cookie1", "firstcookie"));
                manager.addCookie(new com.gargoylesoftware.htmlunit.util.Cookie("localhost", "cookie2", "secondcookie"));
                manager.addCookie(new com.gargoylesoftware.htmlunit.util.Cookie("localhost", "cookie3", "thirdcookie"));

                final HtmlPage page1 = webClient.getPage("http://localhost:8181/cookies1");
                assertEquals(page1.getWebResponse().getContentAsString(), "source");

                assertEquals(webClient.getCookieManager().getCookie("cookie3").getValue(), "firstcookie");
                assertEquals(webClient.getCookieManager().getCookie("cookie4").getValue(), "secondcookie");

                manager.addCookie(new com.gargoylesoftware.htmlunit.util.Cookie("localhost", "cookie4", "fourthcookie"));

                final HtmlPage page2 = webClient.getPage("http://localhost:8181/cookies2");
                assertEquals(page2.getWebResponse().getContentAsString(), "secondcookie,firstcookie,fourthcookie");
            }
        }
    }

    @Test
    void testContentlength()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/contentlength", c -> {
                    var out = "this goes out";
                    c.setContentLength(out.length());
                    var outputstream = c.outputStream();
                    outputstream.write(out.getBytes(StandardCharsets.ISO_8859_1));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                final TextPage page = webClient.getPage("http://localhost:8181/contentlength");
                assertEquals(13, page.getWebResponse().getContentLength());
                assertEquals("this goes out", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testDynamicContenttype()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/dynamiccontenttype", c -> {
                    switch (c.parameter("switch")) {
                        case "text" -> c.setContentType("text/plain");
                        case "html" -> c.setContentType("text/html");
                    }
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                assertEquals(webClient.getPage("http://localhost:8181/dynamiccontenttype?switch=text").getWebResponse().getContentType(), "text/plain");
                assertEquals(webClient.getPage("http://localhost:8181/dynamiccontenttype?switch=html").getWebResponse().getContentType(), "text/html");
            }
        }
    }

    @Test
    void testBinary()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/binary", c -> c.outputStream().write(IntegerUtils.intToBytes(87634675)));
            }
        })) {
            try (final var webClient = new WebClient()) {
                final UnexpectedPage page = webClient.getPage("http://localhost:8181/binary");
                InputStream inputstream = page.getWebResponse().getContentAsStream();
                byte[] integer_bytes = new byte[4];
                assertEquals(4, inputstream.read(integer_bytes));
                assertEquals(87634675, IntegerUtils.bytesToInt(integer_bytes));
            }
        }
    }

    @Test
    void testPrintAndWriteBuffer()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/printandwrite_buffer", c -> {
                    c.enableTextBuffer(true);

                    c.print("print1");
                    c.outputStream().write("write2".getBytes(c.response().getCharacterEncoding()));
                    c.print("print3");
                    c.outputStream().write("write4".getBytes(c.response().getCharacterEncoding()));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/printandwrite_buffer");
                assertEquals("write2write4print1print3", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testPrintAndWriteNoBuffer()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/printandwrite_nobuffer", c -> {
                    c.enableTextBuffer(false);

                    c.print("print1");
                    c.outputStream().write("write2".getBytes(c.response().getCharacterEncoding()));
                    c.print("print3");
                    c.outputStream().write("write4".getBytes(c.response().getCharacterEncoding()));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/printandwrite_nobuffer");
                assertEquals("print1write2print3write4", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testGenerateForm()
    throws Exception {
        try (final var server = new TestServerRunner(new GenerateFormSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form");
                assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_values").getContent(), page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/form?remove=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_fields").getContent(), page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testGenerateFormPrefix()
    throws Exception {
        try (final var server = new TestServerRunner(new GenerateFormSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form?prefix=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_values").getContent(), page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/form?prefix=1&remove=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix").getContent(), page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testGenerateEmptyForm()
    throws Exception {
        try (final var server = new TestServerRunner(new GenerateEmptyFormSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form_empty");
                assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_empty").getContent(), page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/form_empty?remove=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_fields").getContent(), page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testGenerateEmptyFormPrefix()
    throws Exception {
        try (final var server = new TestServerRunner(new GenerateEmptyFormSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form_empty?prefix=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_empty").getContent(), page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/form_empty?prefix=1&remove=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix").getContent(), page.getWebResponse().getContentAsString());
            }
        }
    }

    class Routes extends Router {
        final Route route = route("/route", c -> {
        });
        final Route another = route("/another", c -> {
        });
    }

    @Test
    void testRouterSite() {
        var site = new Site() {
            final Routes routes = group("/routes", new Routes());
            final Routes moreRoutes = group("/moreRoutes", new Routes());
        };

        assertSame(site, site.site());
        assertSame(site, site.routes.site());
        assertSame(site, site.moreRoutes.site());
    }

    @Test
    void testResolveRoutes() {
        var site = new Site() {
            final Route route1 = route("/route1", c -> {
            });
            final Route route2 = route("/route2", c -> {
            });
            final Routes routes = group("/routes", new Routes());
            final Routes moreRoutes = group("/moreRoutes", new Routes());
        };

        assertSame(site.route1, site.resolveRoute(".route1"));
        assertSame(site.route2, site.resolveRoute(".route2"));
        assertSame(site.routes.route, site.resolveRoute(".routes.route"));
        assertSame(site.routes.another, site.resolveRoute(".routes.another"));
        assertSame(site.moreRoutes.route, site.resolveRoute(".moreRoutes.route"));
        assertSame(site.moreRoutes.another, site.resolveRoute(".moreRoutes.another"));

        assertSame(site.route1, site.resolveRoute("route1"));
        assertNull(site.routes.resolveRoute("route1"));
        assertNull(site.moreRoutes.resolveRoute("route1"));

        assertSame(site.route2, site.resolveRoute("route2"));
        assertNull(site.routes.resolveRoute("route2"));
        assertNull(site.moreRoutes.resolveRoute("route2"));

        assertSame(site.routes.route, site.resolveRoute("routes.route"));
        assertNull(site.routes.resolveRoute("routes.route"));
        assertNull(site.moreRoutes.resolveRoute("routes.route"));

        assertSame(site.routes.another, site.resolveRoute("routes.another"));
        assertNull(site.routes.resolveRoute("routes.another"));
        assertNull(site.moreRoutes.resolveRoute("routes.another"));

        assertSame(site.moreRoutes.route, site.resolveRoute("moreRoutes.route"));
        assertNull(site.routes.resolveRoute("moreRoutes.route"));
        assertNull(site.moreRoutes.resolveRoute("moreRoutes.route"));

        assertSame(site.moreRoutes.another, site.resolveRoute("moreRoutes.another"));
        assertNull(site.routes.resolveRoute("moreRoutes.another"));
        assertNull(site.moreRoutes.resolveRoute("moreRoutes.another"));

        assertNull(site.resolveRoute("route"));
        assertSame(site.routes.route, site.routes.resolveRoute("route"));
        assertSame(site.moreRoutes.route, site.moreRoutes.resolveRoute("route"));

        assertNull(site.resolveRoute("another"));
        assertSame(site.routes.another, site.routes.resolveRoute("another"));
        assertSame(site.moreRoutes.another, site.moreRoutes.resolveRoute("another"));

        assertSame(site.route2, site.resolveRoute(".route1^route2"));
        assertSame(site.route1, site.resolveRoute(".route2^route1"));
        assertSame(site.routes.another, site.resolveRoute(".routes.route^another"));
        assertSame(site.routes.route, site.resolveRoute(".routes.another^route"));
        assertSame(site.route1, site.resolveRoute(".routes.another^^route1"));
        assertSame(site.route2, site.resolveRoute(".routes.another^^route2"));
        assertSame(site.moreRoutes.another, site.resolveRoute(".moreRoutes.route^another"));
        assertSame(site.moreRoutes.route, site.resolveRoute(".moreRoutes.another^route"));
        assertSame(site.route1, site.resolveRoute(".moreRoutes.route^^route1"));
        assertSame(site.route2, site.resolveRoute(".moreRoutes.another^^route2"));
        assertSame(site.moreRoutes.another, site.resolveRoute(".routes.another^^moreRoutes.another"));
        assertSame(site.moreRoutes.route, site.resolveRoute(".routes.route^^moreRoutes.route"));
        assertSame(site.routes.another, site.resolveRoute(".moreRoutes.another^^routes.another"));
        assertSame(site.routes.route, site.resolveRoute(".moreRoutes.route^^routes.route"));

        assertNull(site.resolveRoute("^route1"));
        assertSame(site.route1, site.routes.resolveRoute("^route1"));
        assertSame(site.route1, site.moreRoutes.resolveRoute("^route1"));

        assertNull(site.resolveRoute("^route2"));
        assertSame(site.route2, site.routes.resolveRoute("^route2"));
        assertSame(site.route2, site.moreRoutes.resolveRoute("^route2"));
    }

    @Test
    void testFallbacks()
    throws Exception {
        try (final var server = new TestServerRunner(new FallbacksSite())) {
            try (final var webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("fallback1", webClient.getPage("http://localhost:8181/ones").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two/info").getWebResponse().getContentAsString());
                assertEquals("fallback1", webClient.getPage("http://localhost:8181/twos").getWebResponse().getContentAsString());

                assertEquals("fallback1", webClient.getPage("http://localhost:8181/prefix1").getWebResponse().getContentAsString());
                assertEquals("/prefix1/three", webClient.getPage("http://localhost:8181/prefix1/three").getWebResponse().getContentAsString());
                assertEquals("fallback1", webClient.getPage("http://localhost:8181/prefix1/threes").getWebResponse().getContentAsString());

                assertEquals("fallback2", webClient.getPage("http://localhost:8181/prefix1/prefix2").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/four", webClient.getPage("http://localhost:8181/prefix1/prefix2/four").getWebResponse().getContentAsString());
                assertEquals("fallback2", webClient.getPage("http://localhost:8181/prefix1/prefix2/fours").getWebResponse().getContentAsString());

                assertEquals("fallback4", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/prefix3/five", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/five").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/prefix3/five", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/five/info").getWebResponse().getContentAsString());
                assertEquals("fallback4", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/fives").getWebResponse().getContentAsString());

                assertEquals("/prefix1/prefix2/six", webClient.getPage("http://localhost:8181/prefix1/prefix2/six").getWebResponse().getContentAsString());
                assertEquals("fallback2", webClient.getPage("http://localhost:8181/prefix1/prefix2/sixs").getWebResponse().getContentAsString());

                assertEquals("/seven", webClient.getPage("http://localhost:8181/seven").getWebResponse().getContentAsString());
                assertEquals("fallback1", webClient.getPage("http://localhost:8181/sevens").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testPreventElementInstanceAnnotations() {
        RifeConfig.engine().setPrettyEngineExceptions(false);
        try {
            try (final var server = new TestServerRunner(new Site() {
                public void setup() {
                    get("/route", new Element() {
                        @Parameter String parameter;
                        public void process(Context c) {
                        }
                    });
                }
            })) {
                fail("Expected setup exception");
            }
        } catch (EngineException e) {
            assertTrue(e.getCause() instanceof AnnotatedElementInstanceFieldException);
            assertEquals(((AnnotatedElementInstanceFieldException)e.getCause()).getRoute().path(), "/route");
            assertEquals(((AnnotatedElementInstanceFieldException)e.getCause()).getField(), "parameter");
        } finally {
            RifeConfig.engine().setPrettyEngineExceptions(true);
        }
    }

}
