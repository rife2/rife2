/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEngine {
    @Test
    public void testSimplePlain()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple/plain", c -> {
                    c.response().setContentType("text/plain");
                    c.print("Just some text " + c.request().getRemoteAddr() + ":" + c.request().getServerPort() + ":" + c.pathInfo());
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
                    c.print("Just some text " + c.request().getRemoteAddr() + ":" + c.request().getServerPort() + ":" + c.pathInfo());
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

}
