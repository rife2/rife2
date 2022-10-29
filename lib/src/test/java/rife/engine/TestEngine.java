/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
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
    public void testSimplePlain()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple/plain", c -> {
                    c.contentType("text/plain");
                    c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo());
                });
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
                final TextPage page = webClient.getPage("http://localhost:8181/simple/plain");
                assertEquals("text/plain", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:", page.getContent());
            }
        }
    }

    @Test
    public void testSimpleHtml()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple/html", c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo());
                });
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/simple/html");
                assertEquals("text/html", page.getWebResponse().getContentType());
                assertEquals("Just some text 127.0.0.1:8181:", page.asNormalizedText());
            }
        }
    }

    @Test
    public void testSimplePathInfo()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple/pathinfo", PathInfoHandling.CAPTURE, c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.serverPort() + ":" + c.pathInfo());
                });
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
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
                    webClient.getPage("http://localhost:8181/simple/pathinfoddd");
                    fail("Expecting 404");
                } catch (FailingHttpStatusCodeException e) {
                    // success
                }
            }
        }
    }

    @Test
    public void testHeaders()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/headers", c -> {
                    c.header("Content-Disposition", "attachment; filename=thefile.zip");
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    cal.set(2002, Calendar.OCTOBER, 25, 19, 20, 58);
                    c.header("DateHeader", cal.getTimeInMillis());
                    c.header("IntHeader", 1212);

                    c.print("headers");
                });
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/headers");
                assertTrue(page.getWebResponse().getResponseHeaders().size() > 4);
                assertEquals("attachment; filename=thefile.zip", page.getWebResponse().getResponseHeaderValue("CONTENT-DISPOSITION"));
                assertEquals("Fri, 25 Oct 2002 19:20:58 GMT", page.getWebResponse().getResponseHeaderValue("DATEHEADER"));
                assertEquals("1212", page.getWebResponse().getResponseHeaderValue("INTHEADER"));
            }
        }
    }

    @Test
    public void testCookies()
    throws IOException {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/cookies1", c -> {
                    if (c.hasCookie("cookie1") &&
                        c.hasCookie("cookie2") &&
                        c.hasCookie("cookie3")) {
                        c.cookie(new Cookie("cookie3", c.cookieValue("cookie1")));
                        c.cookie(new Cookie("cookie4", c.cookieValue("cookie2")));
                    }

                    c.print("source");
                });

                get("/cookies2", c -> {
                    c.print(c.cookieValue("cookie2") + "," + c.cookieValue("cookie3") + "," + c.cookieValue("cookie4"));
                });
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
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
    public void testContentlength()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/contentlength", c -> {
                    var out = "this goes out";
                    c.contentLength(out.length());
                    var outputstream = c.outputStream();
                    outputstream.write(out.getBytes(StandardCharsets.ISO_8859_1));
                });
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
                final TextPage page = webClient.getPage("http://localhost:8181/contentlength");
                assertEquals(13, page.getWebResponse().getContentLength());
                assertEquals("this goes out", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testDynamicContenttype()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/dynamiccontenttype", c -> {
                    switch (c.parameter("switch")) {
                        case "text" -> c.contentType("text/plain");
                        case "html" -> c.contentType("text/html");
                    }
                });
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals(webClient.getPage("http://localhost:8181/dynamiccontenttype?switch=text").getWebResponse().getContentType(), "text/plain");
                assertEquals(webClient.getPage("http://localhost:8181/dynamiccontenttype?switch=html").getWebResponse().getContentType(), "text/html");
            }
        }
    }

    @Test
    public void testBinary()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/binary", c -> {
                    c.outputStream().write(IntegerUtils.intToBytes(87634675));
                });
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
                final UnexpectedPage page = webClient.getPage("http://localhost:8181/binary");
                InputStream inputstream = page.getWebResponse().getContentAsStream();
                byte[] integer_bytes = new byte[4];
                assertEquals(4, inputstream.read(integer_bytes));
                assertEquals(87634675, IntegerUtils.bytesToInt(integer_bytes));
            }
        }
    }

    @Test
    public void testPrintAndWriteBuffer()
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
            try (final WebClient webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/printandwrite_buffer");
                assertEquals("write2write4print1print3", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testPrintAndWriteNoBuffer()
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
            try (final WebClient webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/printandwrite_nobuffer");
                assertEquals("print1write2print3write4", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGenerateForm()
    throws Exception {
        try (final var server = new TestServerRunner(new GenerateFormSite())) {
            try (final WebClient webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form");
                assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_values").getContent(), page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/form?remove=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_fields").getContent(), page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGenerateFormPrefix()
    throws Exception {
        try (final var server = new TestServerRunner(new GenerateFormSite())) {
            try (final WebClient webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form?prefix=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_values").getContent(), page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/form?prefix=1&remove=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix").getContent(), page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGenerateEmptyForm()
    throws Exception {
        try (final var server = new TestServerRunner(new GenerateEmptyFormSite())) {
            try (final WebClient webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form_empty");
                assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_empty").getContent(), page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/form_empty?remove=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_fields").getContent(), page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGenerateEmptyFormPrefix()
    throws Exception {
        try (final var server = new TestServerRunner(new GenerateEmptyFormSite())) {
            try (final WebClient webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form_empty?prefix=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_empty").getContent(), page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/form_empty?prefix=1&remove=1");
                assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix").getContent(), page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGroup()
    throws Exception {
        try (final var server = new TestServerRunner(new GroupSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("/three", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("/four", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGroupPrefix()
    throws Exception {
        try (final var server = new TestServerRunner(new GroupPrefixSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("/group/three", webClient.getPage("http://localhost:8181/group/three").getWebResponse().getContentAsString());
                assertEquals("/group/four", webClient.getPage("http://localhost:8181/group/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGroupsMultiLevel()
    throws Exception {
        try (final var server = new TestServerRunner(new GroupsMultiLevelSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("/prefix1/three", webClient.getPage("http://localhost:8181/prefix1/three").getWebResponse().getContentAsString());
                assertEquals("/prefix1/four", webClient.getPage("http://localhost:8181/prefix1/four").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/five", webClient.getPage("http://localhost:8181/prefix1/prefix2/five").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/six", webClient.getPage("http://localhost:8181/prefix1/prefix2/six").getWebResponse().getContentAsString());
                assertEquals("/seven", webClient.getPage("http://localhost:8181/seven").getWebResponse().getContentAsString());
                assertEquals("/eight", webClient.getPage("http://localhost:8181/eight").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/nine", webClient.getPage("http://localhost:8181/prefix1/prefix2/nine").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/ten", webClient.getPage("http://localhost:8181/prefix1/prefix2/ten").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/prefix3/eleven", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/eleven").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/prefix3/twelve", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/twelve").getWebResponse().getContentAsString());
            }
        }
    }

    // TODO : test path info routes in groups

    @Test
    public void testBefore()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("before1before2/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("before1before2/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testAfter()
    throws Exception {
        try (final var server = new TestServerRunner(new AfterSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/oneafter1after2", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/twoafter1after2", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfter()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("before1before2/oneafter1after2", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("before1before2/twoafter1after2", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeGroup()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeGroupSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("before1before2/three", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("before1before2/four", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testAfterGroup()
    throws Exception {
        try (final var server = new TestServerRunner(new AfterGroupSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("/threeafter1after2", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("/fourafter1after2", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfterGroup()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterGroupSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfterGroupsMultiLevel()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterGroupsMultiLevelSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());

                assertEquals("before1before2/prefix1/three", webClient.getPage("http://localhost:8181/prefix1/three").getWebResponse().getContentAsString());
                assertEquals("before1before2/prefix1/four", webClient.getPage("http://localhost:8181/prefix1/four").getWebResponse().getContentAsString());
                assertEquals("before1before2/prefix1/prefix2/fiveafter1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/five").getWebResponse().getContentAsString());
                assertEquals("before1before2/prefix1/prefix2/sixafter1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/six").getWebResponse().getContentAsString());

                assertEquals("before5/seven", webClient.getPage("http://localhost:8181/seven").getWebResponse().getContentAsString());
                assertEquals("before5/eight", webClient.getPage("http://localhost:8181/eight").getWebResponse().getContentAsString());

                assertEquals("before1before2before3before4/prefix1/prefix2/nineafter1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/nine").getWebResponse().getContentAsString());
                assertEquals("before1before2before3before4/prefix1/prefix2/tenafter1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/ten").getWebResponse().getContentAsString());
                assertEquals("before1before2before3before4/prefix1/prefix2/prefix3/elevenafter3after4after1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/eleven").getWebResponse().getContentAsString());
                assertEquals("before1before2before3before4/prefix1/prefix2/prefix3/twelveafter3after4after1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/twelve").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfterGroupRespond()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterGroupRespondSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());

                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("before1", webClient.getPage("http://localhost:8181/three?respond1=true").getWebResponse().getContentAsString());
                assertEquals("before1before2", webClient.getPage("http://localhost:8181/three?respond2=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/three", webClient.getPage("http://localhost:8181/three?respond3=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three?respond4=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1", webClient.getPage("http://localhost:8181/three?respond5=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three?respond6=true").getWebResponse().getContentAsString());

                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
                assertEquals("before1", webClient.getPage("http://localhost:8181/four?respond1=true").getWebResponse().getContentAsString());
                assertEquals("before1before2", webClient.getPage("http://localhost:8181/four?respond2=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four??respond3=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/four", webClient.getPage("http://localhost:8181/four?respond4=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1", webClient.getPage("http://localhost:8181/four?respond5=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four??respond6=true").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfterGroupNext()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterGroupNextSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());

                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("before2/threeafter1after2", webClient.getPage("http://localhost:8181/three?next1=true").getWebResponse().getContentAsString());
                assertEquals("before1/threeafter1after2", webClient.getPage("http://localhost:8181/three?next2=true").getWebResponse().getContentAsString());
                assertEquals("before1before2after1after2", webClient.getPage("http://localhost:8181/three?next3=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three?next4=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter2", webClient.getPage("http://localhost:8181/three?next5=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1", webClient.getPage("http://localhost:8181/three?next6=true").getWebResponse().getContentAsString());

                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
                assertEquals("before2/fourafter1after2", webClient.getPage("http://localhost:8181/four?next1=true").getWebResponse().getContentAsString());
                assertEquals("before1/fourafter1after2", webClient.getPage("http://localhost:8181/four?next2=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four??next3=true").getWebResponse().getContentAsString());
                assertEquals("before1before2after1after2", webClient.getPage("http://localhost:8181/four?next4=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter2", webClient.getPage("http://localhost:8181/four?next5=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1", webClient.getPage("http://localhost:8181/four?next6=true").getWebResponse().getContentAsString());
            }
        }
    }
}
