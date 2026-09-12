/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import rife.config.RifeConfig;
import rife.ioc.HierarchicalProperties;
import rife.servlet.RifeFilter;
import rife.test.MockConversation;
import rife.test.MockRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

public class TestReloadEndpoint {
    static final String INSTANCE = "test-instance";

    // the endpoint only opens for an application that its reloader started,
    // which the tests stand in for by naming their own parent process
    @BeforeEach
    void activateReload() {
        var parent = ProcessHandle.current().parent();
        assumeTrue(parent.isPresent(), "the parent process has to be visible for the reloader to activate");
        System.setProperty(Reloader.INSTANCE_PROPERTY, INSTANCE);
        System.setProperty(Reloader.SUPERVISOR_PROPERTY, String.valueOf(parent.get().pid()));
    }

    @AfterEach
    void deactivateReload() {
        System.clearProperty(Reloader.INSTANCE_PROPERTY);
        System.clearProperty(Reloader.SUPERVISOR_PROPERTY);
    }

    static void withoutReloader() {
        System.clearProperty(Reloader.INSTANCE_PROPERTY);
        System.clearProperty(Reloader.SUPERVISOR_PROPERTY);
    }

    static Site site() {
        return new Site() {
            public void setup() {
                get("/page", c -> c.print("<html><BODY><p>page</p></BODY></html>"));
                get("/fragment", c -> c.print("<div>fragment</div>"));
                get("/text", c -> {
                    c.setContentType("text/plain");
                    c.print("<p>text</p></body>");
                });
                get("/sized", c -> {
                    var page = "<html><body>sized</body></html>";
                    c.setContentLength(page.length());
                    c.print(page);
                });
                get("/tag", c -> c.print(c.template("reload_script")));
                get("/tagdefault", c -> c.print(c.template("reload_script_default")));
                get("/error", c -> {
                    throw new RuntimeException("reload error");
                });
            }
        };
    }

    static Site failingSite() {
        return new Site() {
            public void setup() {
                throw new RuntimeException("setup error");
            }
        };
    }

    static int count(String text, String part) {
        var count = 0;
        var index = text.indexOf(part);
        while (index != -1) {
            count++;
            index = text.indexOf(part, index + part.length());
        }
        return count;
    }

    static String request(MockConversation conversation, String url) {
        return conversation.doRequest(url).getText();
    }

    static Set<Thread> watcherThreads() {
        return Thread.getAllStackTraces().keySet().stream()
            .filter(thread -> ReloadEndpoint.WATCHER_THREAD.equals(thread.getName()))
            .collect(Collectors.toSet());
    }

    @Test
    void testScriptInjectedBeforeBodyEnd() {
        var conversation = new MockConversation(site());
        try {
            var text = request(conversation, "/page");
            assertTrue(text.startsWith("<html><BODY><p>page</p><script " + ReloadEndpoint.MARKER + ">"), text);
            assertTrue(text.endsWith("</script></BODY></html>"), text);
            assertEquals(1, count(text, ReloadEndpoint.MARKER));
            assertTrue(text.contains("\"" + INSTANCE + "\""), "the script compares against this instance");
            assertTrue(text.contains("\"" + ReloadEndpoint.PATH + "\""), "the script connects to the reload endpoint");
            assertTrue(text.contains("location.replace(location.href)"), "reloading mustn't send a form again");
            assertTrue(text.contains("readyState===2"), "a stream that failed for good has to be replaced");
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testScriptUsesTheGateUrl() {
        RifeConfig.engine().setWebappContextPath("/ctx");
        var conversation = new MockConversation(site());
        try {
            assertTrue(request(conversation, "/page").contains("\"/ctx" + ReloadEndpoint.PATH + "\""));
        } finally {
            conversation.destroy();
            RifeConfig.engine().setWebappContextPath(null);
        }
    }

    @Test
    void testScriptEscapesTheInstance() {
        System.setProperty(Reloader.INSTANCE_PROPERTY, "quote\"and\\slash");
        var conversation = new MockConversation(site());
        try {
            assertTrue(request(conversation, "/page").contains("\"quote\\\"and\\\\slash\""),
                "the instance is escaped for the script it sits in");
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testFragmentsAndOtherContentUntouched() {
        var conversation = new MockConversation(site());
        try {
            var fragment = conversation.doRequest("/fragment", new MockRequest().header("HX-Request", "true")).getText();
            assertEquals("<div>fragment</div>", fragment, "an htmx fragment lands in a page that has the script");
            assertEquals("<p>text</p></body>", request(conversation, "/text"));
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testPageWithoutBodyTagGetsScriptAtTheEnd() {
        var conversation = new MockConversation(site());
        try {
            var text = request(conversation, "/fragment");
            assertTrue(text.startsWith("<div>fragment</div><script " + ReloadEndpoint.MARKER + ">"), text);
            assertTrue(text.endsWith("</script>"), text);
            assertEquals(1, count(text, ReloadEndpoint.MARKER));
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testRoutesUnderTheReservedPrefixAreReported() {
        var records = new ArrayList<LogRecord>();
        var handler = new Handler() {
            public void publish(LogRecord record) {
                records.add(record);
            }

            public void flush() {
            }

            public void close() {
            }
        };
        var logger = Logger.getLogger("rife.engine");
        logger.addHandler(handler);
        try {
            new Site() {
                public void setup() {
                    get(Router.RESERVED_PATH_PREFIX + "mine", c -> c.print("shadowed"));
                }
            }.setup();
        } finally {
            logger.removeHandler(handler);
        }
        assertTrue(records.stream().anyMatch(record -> record.getMessage().contains(Router.RESERVED_PATH_PREFIX + "mine")),
            "registering a route under the reserved prefix has to be reported");
    }

    @Test
    void testTemplateTagPlacesScriptOnce() {
        var conversation = new MockConversation(site());
        try {
            var text = request(conversation, "/tag");
            assertEquals(1, count(text, ReloadEndpoint.MARKER), text);
            assertTrue(text.startsWith("<html><body><p>tag</p><script " + ReloadEndpoint.MARKER + ">"), text);
            assertTrue(text.contains("</script><p>after</p></body></html>"), text);

            var defaulted = request(conversation, "/tagdefault");
            assertEquals(1, count(defaulted, ReloadEndpoint.MARKER), defaulted);
            assertFalse(defaulted.contains("fallback"), "the script replaces the default content");
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testErrorPagesGetScript() {
        var conversation = new MockConversation(site());
        try {
            var text = request(conversation, "/error");
            assertTrue(text.contains("reload error"), text);
            assertEquals(1, count(text, ReloadEndpoint.MARKER));
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testSetupErrorPagesGetScript() {
        var conversation = new MockConversation(failingSite());
        try {
            var text = request(conversation, "/page");
            assertTrue(text.contains("setup error"), text);
            assertEquals(1, count(text, ReloadEndpoint.MARKER));
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testInactiveWithoutReloader() {
        withoutReloader();
        var conversation = new MockConversation(site());
        try {
            assertEquals("<html><BODY><p>page</p></BODY></html>", request(conversation, "/page"));
            assertEquals("<html><body><p>tag</p><p>after</p></body></html>\n", request(conversation, "/tag"));
            assertEquals("<html><body><p>tag</p>fallback</body></html>\n", request(conversation, "/tagdefault"));
            assertEquals(404, conversation.doRequest(ReloadEndpoint.PATH).getStatus());
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testInactiveWhenTheSupervisorIsntTheParent() {
        System.setProperty(Reloader.SUPERVISOR_PROPERTY, String.valueOf(ProcessHandle.current().pid()));
        var conversation = new MockConversation(site());
        try {
            assertEquals("<html><BODY><p>page</p></BODY></html>", request(conversation, "/page"));
        } finally {
            conversation.destroy();
        }
    }

    // the reloader always names itself, a lone instance property is left over
    // from somewhere else
    @Test
    void testInactiveWithoutASupervisor() {
        System.clearProperty(Reloader.SUPERVISOR_PROPERTY);
        var conversation = new MockConversation(site());
        try {
            assertEquals("<html><BODY><p>page</p></BODY></html>", request(conversation, "/page"));
        } finally {
            conversation.destroy();
        }
    }

    // a response whose length was announced can't take the script anymore
    @Test
    void testResponseWithAContentLengthIsLeftAlone() {
        var conversation = new MockConversation(site());
        try {
            assertEquals("<html><body>sized</body></html>", request(conversation, "/sized"));
        } finally {
            conversation.destroy();
        }
    }

    @Test
    void testOnlyTheGateThatOpenedTheEndpointClosesIt() {
        var site = site();
        var first = new MockConversation(site);
        var second = new MockConversation(site);
        try {
            first.destroy();
            assertTrue(request(second, "/page").contains(ReloadEndpoint.MARKER),
                "the gate that still serves the site keeps its endpoint");
        } finally {
            second.destroy();
        }
    }

    @Test
    void testInactiveWithAnUnusableSupervisor() {
        System.setProperty(Reloader.SUPERVISOR_PROPERTY, "not-a-pid");
        var conversation = new MockConversation(site());
        try {
            assertEquals("<html><BODY><p>page</p></BODY></html>", request(conversation, "/page"));
        } finally {
            conversation.destroy();
        }
    }

    // a property that arrives from the environment or a web.xml parameter
    // must never open this endpoint in a deployment
    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testOnlyJvmArgumentsActivateTheEndpoint()
    throws Exception {
        withoutReloader();
        var server = new Server().port(8193).host("localhost").minThreads(1).maxThreads(4);
        server.properties().put(Reloader.INSTANCE_PROPERTY, INSTANCE);
        server.properties().put(Reloader.SUPERVISOR_PROPERTY, String.valueOf(ProcessHandle.current().parent().orElseThrow().pid()));
        server.start(site());
        try {
            var client = HttpClient.newHttpClient();
            var page = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8193/page")).build(),
                HttpResponse.BodyHandlers.ofString());
            assertEquals("<html><BODY><p>page</p></BODY></html>", page.body(), "a site property doesn't inject the script");

            var endpoint = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8193" + ReloadEndpoint.PATH)).build(),
                HttpResponse.BodyHandlers.ofString());
            assertEquals(404, endpoint.statusCode(), "a site property doesn't open the endpoint");
        } finally {
            server.stop();
        }
    }

    static Server startServer(int port, Site site, String staticResourceBase) {
        var server = new Server()
            .port(port)
            .host("localhost")
            .minThreads(1)
            .maxThreads(4);
        if (staticResourceBase != null) {
            server.staticResourceBase(staticResourceBase);
        }
        return server.start(site);
    }

    static BufferedReader connect(int port)
    throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + ReloadEndpoint.PATH)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        var reader = new BufferedReader(new InputStreamReader(response.body()));
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/event-stream"));
        return reader;
    }

    static List<String> readEvent(BufferedReader reader)
    throws IOException {
        var lines = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(":")) {
                continue;
            }
            if (line.isEmpty()) {
                if (!lines.isEmpty()) {
                    return lines;
                }
                continue;
            }
            lines.add(line.replaceFirst(": ", ":"));
        }
        throw new AssertionError("the event stream ended before an event arrived");
    }

    static void assertStreamsInstance(Site site, int port)
    throws Exception {
        var server = startServer(port, site, null);
        try (var reader = connect(port)) {
            var event = readEvent(reader);
            assertTrue(event.contains("event:instance"), event.toString());
            assertTrue(event.contains("retry:250"), event.toString());
            assertTrue(event.contains("data:" + INSTANCE), event.toString());
        } finally {
            server.stop();
        }
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testEndpointStreamsInstance()
    throws Exception {
        assertStreamsInstance(site(), 8187);
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testEndpointStreamsInstanceAfterSetupError()
    throws Exception {
        assertStreamsInstance(failingSite(), 8188);
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testStaticFileChangesReloadTheBrowser(@TempDir Path base)
    throws Exception {
        var style = base.resolve("style.css");
        Files.writeString(style, "p{color:red}");

        var server = startServer(8190, site(), base.toString());
        try (var reader = connect(8190)) {
            assertTrue(readEvent(reader).contains("event:instance"));

            Thread.sleep(3000);
            assertFalse(reader.ready(), "watching the files of a resource base doesn't reload the browser by itself");

            Files.writeString(style, "p{color:blue}");
            assertTrue(readEvent(reader).contains("event:reload"), "a changed static file has to reload the browser");
        } finally {
            server.stop();
        }
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testEveryBrowserIsReloaded(@TempDir Path base)
    throws Exception {
        var style = base.resolve("style.css");
        Files.writeString(style, "p{color:red}");

        var server = startServer(8194, site(), base.toString());
        try (var first = connect(8194);
             var second = connect(8194)) {
            assertTrue(readEvent(first).contains("event:instance"));
            assertTrue(readEvent(second).contains("event:instance"));

            Files.writeString(style, "p{color:blue}");
            assertTrue(readEvent(first).contains("event:reload"));
            assertTrue(readEvent(second).contains("event:reload"), "every connected browser is reloaded");
        } finally {
            server.stop();
        }
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testTomcatStaticFileChangesReloadTheBrowser(@TempDir Path base)
    throws Exception {
        var style = base.resolve("style.css");
        Files.writeString(style, "p{color:red}");

        var server = new TomcatServer().hostname("localhost").port(8286).addContext(base.toString());
        server.start(site());
        try (var reader = connect(8286)) {
            assertTrue(readEvent(reader).contains("event:instance"));

            Files.writeString(style, "p{color:blue}");
            assertTrue(readEvent(reader).contains("event:reload"), "the doc base of the Tomcat server is watched too");
        } finally {
            server.stop();
        }
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testSiteKeepsReloadingWhenItIsServedAgain(@TempDir Path base)
    throws Exception {
        var style = base.resolve("style.css");
        Files.writeString(style, "p{color:red}");
        var site = site();

        startServer(8192, site, base.toString()).stop();

        var server = startServer(8192, site, base.toString());
        try (var reader = connect(8192)) {
            assertTrue(readEvent(reader).contains("event:instance"));

            Files.writeString(style, "p{color:blue}");
            assertTrue(readEvent(reader).contains("event:reload"), "a site that is served again still reloads");
        } finally {
            server.stop();
        }
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testClassChangesDontReloadTheBrowser(@TempDir Path base)
    throws Exception {
        var class_file = base.resolve("Compiled.class");
        Files.writeString(class_file, "compiled");

        var server = startServer(8191, site(), base.toString());
        try (var reader = connect(8191)) {
            assertTrue(readEvent(reader).contains("event:instance"));

            Files.writeString(class_file, "recompiled");
            Thread.sleep(3000);
            assertFalse(reader.ready(), "the reloader restarts the application for class changes instead");

            // the same stream still has to work, so that the check above
            // can't pass because the connection quietly died
            Files.writeString(base.resolve("style.css"), "p{}");
            assertTrue(readEvent(reader).contains("event:reload"), "the stream was still alive");
        } finally {
            server.stop();
        }
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testFailedStartLeavesNothingBehind()
    throws Exception {
        var watchers = watcherThreads();
        assertThrows(RuntimeException.class, () -> startServer(8195, site(), "this/resource/base/isnt/there"));

        var deadline = System.currentTimeMillis() + 10000;
        var added = watcherThreads();
        while (System.currentTimeMillis() < deadline) {
            added = watcherThreads();
            added.removeAll(watchers);
            if (added.isEmpty()) {
                break;
            }
            Thread.sleep(100);
        }
        assertTrue(added.isEmpty(), "the watcher of a server that failed to start has to stop");
    }

    // the streams are for the browser that the application rendered a page for
    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testEndpointRefusesOtherRequests()
    throws Exception {
        var server = startServer(8197, site(), null);
        try {
            var client = HttpClient.newHttpClient();
            var posted = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8197" + ReloadEndpoint.PATH))
                .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(405, posted.statusCode(), "only a GET opens a stream");

            var cross_site = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8197" + ReloadEndpoint.PATH))
                .header("Sec-Fetch-Site", "cross-site").build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(403, cross_site.statusCode(), "a page from another site doesn't open a stream");
        } finally {
            server.stop();
        }
    }

    // the gate of a server that failed to start has to let go of the endpoint,
    // or the site would count one gate too many for the rest of its life
    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testSiteServedAgainAfterAFailedStartLeavesNothingBehind(@TempDir Path base)
    throws Exception {
        var site = site();
        assertThrows(RuntimeException.class, () -> startServer(8198, site, "this/resource/base/isnt/there"));
        assertNull(site.reloadEndpoint(), "a failed start lets go of the endpoint");

        var server = startServer(8198, site, base.toString());
        assertNotNull(site.reloadEndpoint(), "serving the site again takes a new endpoint");
        server.stop();
        assertNull(site.reloadEndpoint(), "the last gate to stop takes the endpoint with it");
    }

    static org.eclipse.jetty.server.Server startNonAsyncServer(int port, Site site)
    throws Exception {
        var server = new org.eclipse.jetty.server.Server();
        var connector = new org.eclipse.jetty.server.ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        var handler = new ServletContextHandler();
        handler.setContextPath("/");

        var filter = new RifeFilter();
        filter.init(new HierarchicalProperties(), site);
        var holder = new FilterHolder(filter);
        holder.setAsyncSupported(false);
        handler.addFilter(holder, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addServlet(new ServletHolder("default", DefaultServlet.class), "/");

        server.setHandler(handler);
        server.start();
        return server;
    }

    // a container without asynchronous requests can't stream events, saying so
    // keeps the browser from reconnecting forever
    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testUnsupportedAsyncAnswersUnavailable()
    throws Exception {
        var server = startNonAsyncServer(8196, site());
        try {
            var response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:8196" + ReloadEndpoint.PATH)).build(),
                HttpResponse.BodyHandlers.ofString());
            assertEquals(503, response.statusCode());
        } finally {
            server.stop();
        }
    }
}
