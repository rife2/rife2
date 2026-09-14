/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import rife.authentication.elements.MemoryAuthenticatedSite;
import rife.config.RifeConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestProxyRootUrl {
    @AfterEach
    void tearDown() {
        RifeConfig.engine().setProxyRootUrl(null);
    }

    static class SchemeSite extends Site {
        Route scheme = get("/scheme", c -> c.print(c.scheme() + "|" + c.secure()));
        Route token = get("/token", c -> c.print(c.csrfToken()));
    }

    @Test
    void testSchemeWithoutProxyRootUrl()
    throws Exception {
        try (final var server = new TestServerRunner(new SchemeSite())) {
            assertEquals("http|false", send("/scheme", null).body());
        }
    }

    @Test
    void testSchemeFollowsProxyRootUrl()
    throws Exception {
        try (final var server = new TestServerRunner(new SchemeSite())) {
            RifeConfig.engine().setProxyRootUrl("https://example.com");
            assertEquals("https|true", send("/scheme", null).body());

            RifeConfig.engine().setProxyRootUrl("HTTPS://example.com/app");
            assertEquals("https|true", send("/scheme", null).body());

            RifeConfig.engine().setProxyRootUrl("http://example.com");
            assertEquals("http|false", send("/scheme", null).body());

            RifeConfig.engine().setProxyRootUrl("example.com");
            assertEquals("http|false", send("/scheme", null).body());
        }
    }

    @Test
    void testCsrfCookieIsSecureBehindHttpsProxy()
    throws Exception {
        try (final var server = new TestServerRunner(new SchemeSite())) {
            assertFalse(cookie(send("/token", null), "csrfToken").contains("Secure"));

            RifeConfig.engine().setProxyRootUrl("https://example.com");
            assertTrue(cookie(send("/token", null), "csrfToken").contains("Secure"));
        }
    }

    @Test
    void testAuthCookieIsSecureBehindHttpsProxy()
    throws Exception {
        try (final var server = new TestServerRunner(new MemoryAuthenticatedSite())) {
            assertFalse(cookie(send("/login", "login=guest&password=guestpass"), "authId").contains("Secure"));

            RifeConfig.engine().setProxyRootUrl("https://example.com");
            var response = send("/login", "login=guest&password=guestpass");
            assertEquals("https://example.com/landing", response.headers().firstValue("Location").orElse(null));
            assertTrue(cookie(response, "authId").contains("Secure"));
        }
    }

    private static HttpResponse<String> send(String path, String form)
    throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:8181" + path));
        if (form != null) {
            request.header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form));
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String cookie(HttpResponse<String> response, String name) {
        List<String> cookies = response.headers().allValues("Set-Cookie");
        return cookies.stream()
            .filter(c -> c.startsWith(name + "="))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no " + name + " cookie in " + cookies));
    }
}
