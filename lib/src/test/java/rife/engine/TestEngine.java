/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;

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
}
