/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class TestStaticResources {
    static final String RESOURCE_BASE = "src/test/resources";
    static final String STATIC_FILE = "/parsed_html.html";

    static Site siteWithRoute() {
        return new Site() {
            public void setup() {
                get("/hello", c -> c.print("Hello World"));
            }
        };
    }

    static HttpResponse<String> get(int port, String path)
    throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .timeout(Duration.ofSeconds(30))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // the default servlet has to keep serving the static base next to the
    // routes, whatever url pattern it ends up mapped under
    static void assertServesStaticAndRoutes(int port)
    throws Exception {
        var stat = get(port, STATIC_FILE);
        assertEquals(200, stat.statusCode(), "the static resource base has to be served");
        assertTrue(stat.body().contains("<html>"), "and has to return the file's real content");

        assertEquals(404, get(port, "/does-not-exist.html").statusCode(),
            "a missing static resource still has to be a 404");

        var route = get(port, "/hello");
        assertEquals(200, route.statusCode(), "routes keep working alongside static resources");
        assertEquals("Hello World", route.body());
    }

    @Test
    @Timeout(120)
    void testServerServesStaticResources()
    throws Exception {
        var server = new Server()
            .port(8183)
            .host("localhost")
            .minThreads(1)
            .maxThreads(4)
            .staticResourceBase(RESOURCE_BASE);
        server.start(siteWithRoute());
        try {
            assertServesStaticAndRoutes(8183);
        } finally {
            server.stop();
        }
    }

    @Test
    @Timeout(120)
    void testTomcatContextServesStaticResources()
    throws Exception {
        var server = new TomcatServer()
            .hostname("localhost")
            .port(8284)
            .addContext(RESOURCE_BASE);
        server.start(siteWithRoute());
        try {
            assertServesStaticAndRoutes(8284);
        } finally {
            server.stop();
        }
    }

    @Test
    @Timeout(120)
    void testTomcatWebappServesStaticResources()
    throws Exception {
        var server = new TomcatServer()
            .hostname("localhost")
            .port(8285)
            .addWebapp(RESOURCE_BASE);
        server.start(siteWithRoute());
        try {
            assertServesStaticAndRoutes(8285);
        } finally {
            server.stop();
        }
    }
}
