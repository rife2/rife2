/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rife.config.RifeConfig;
import rife.continuations.ContinuationContext;
import rife.engine.continuations.TestSimplePause;
import rife.engine.exceptions.EngineException;
import rife.tools.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestSiteRunner {
    static class Recorder implements SiteResponseListener {
        final List<String> calls = Collections.synchronizedList(new ArrayList<>());
        final ByteArrayOutputStream content = new ByteArrayOutputStream();
        final CountDownLatch ended = new CountDownLatch(1);
        volatile int status;
        volatile Map<String, List<String>> headers;
        volatile Throwable failure;

        public void committed(SiteResponse response) {
            calls.add("committed");
            status = response.getStatus();
            headers = response.getHttpHeaders();
        }

        public void content(byte[] bytes, int offset, int length) {
            calls.add("content");
            synchronized (content) {
                content.write(bytes, offset, length);
            }
        }

        public void completed() {
            calls.add("completed");
            ended.countDown();
        }

        public void failed(Throwable exception) {
            calls.add("failed");
            failure = exception;
            ended.countDown();
        }

        public void cancelled() {
            calls.add("cancelled");
            ended.countDown();
        }

        String text() {
            synchronized (content) {
                return content.toString(StandardCharsets.UTF_8);
            }
        }

        byte[] bytes() {
            synchronized (content) {
                return content.toByteArray();
            }
        }

        String last() {
            synchronized (calls) {
                return calls.get(calls.size() - 1);
            }
        }
    }

    private boolean prettyEngineExceptions_;
    private boolean logEngineExceptions_;
    private long fileUploadSizeLimit_;
    private boolean fileUploadSizeCheck_;
    private boolean fileUploadSizeException_;

    @BeforeEach
    void saveConfig() {
        prettyEngineExceptions_ = RifeConfig.engine().getPrettyEngineExceptions();
        logEngineExceptions_ = RifeConfig.engine().getLogEngineExceptions();
        fileUploadSizeLimit_ = RifeConfig.engine().getFileUploadSizeLimit();
        fileUploadSizeCheck_ = RifeConfig.engine().getFileUploadSizeCheck();
        fileUploadSizeException_ = RifeConfig.engine().getFileUploadSizeException();
    }

    @AfterEach
    void restoreConfig() {
        RifeConfig.engine()
            .setPrettyEngineExceptions(prettyEngineExceptions_)
            .setLogEngineExceptions(logEngineExceptions_)
            .setFileUploadSizeLimit(fileUploadSizeLimit_)
            .setFileUploadSizeCheck(fileUploadSizeCheck_)
            .setFileUploadSizeException(fileUploadSizeException_);
    }

    @Test
    void testGet() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/hello", c -> c.print("hello " + c.parameter("name") + " " + String.join(",", c.parameterValues("tag"))));
            }
        })) {
            var response = runner.handle("/hello?name=w%C3%B6rld&tag=a&tag=b+c");
            assertTrue(response.isHandled());
            assertEquals(200, response.getStatus());
            assertEquals("hello wörld a,b c", response.getText());
            assertEquals("text/html; charset=UTF-8", response.getContentType());
            assertEquals(List.of("text/html; charset=UTF-8"), response.getHttpHeaders().get("content-type"));
        }
    }

    @Test
    void testOrigin() {
        try (var runner = new SiteRunner().scheme("myapp").serverName("app").start(new Site() {
            public void setup() {
                get("/root", c -> c.print(c.request().getServerRootUrl(-1) + "|" + c.request().getServerPort() + "|" + c.request().isSecure() + "|" + c.urlFor(c.route())));
            }
        })) {
            assertEquals("myapp://app", runner.getServerRootUrl());
            assertEquals("myapp://app|-1|false|myapp://app/root", runner.handle("/root").getText());
            assertEquals("myapp://app|-1|false|myapp://app/root", runner.handle("myapp://app/root").getText());

            var foreign = runner.handle("https://elsewhere/root");
            assertFalse(foreign.isHandled());
            assertEquals(404, foreign.getStatus());
        }

        try (var runner = new SiteRunner().scheme("https").serverName("example.com").serverPort(8443).contextPath("/ctx").start(new Site() {
            public void setup() {
                get("/root", c -> c.print(c.request().getServerRootUrl(-1) + "|" + c.request().isSecure() + "|" + c.urlFor(c.route())));
            }
        })) {
            assertEquals("https://example.com:8443|true|https://example.com:8443/ctx/root", runner.handle("/ctx/root").getText());
            assertFalse(runner.handle("/root").isHandled());
        }
    }

    @Test
    void testPostForm() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                Element element = c -> c.print(c.method() + ":" + String.join(",", c.parameterValues("a")) + ":" + c.parameter("b"));
                post("/form", element);
                put("/form", element);
                patch("/form", element);
                patch("/body", c -> c.print(c.parameter("b") + "|" + c.body()));
            }
        })) {
            var body = "a=%C3%A9t%C3%A9+x&b=2&a=%41".getBytes(StandardCharsets.ISO_8859_1);
            var response = runner.handle("/form?a=q", new SiteRequest(RequestMethod.POST)
                .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .bodyAsBytes(body));
            assertEquals("POST:q,été x,A:2", response.getText());

            response = runner.handle("/form", new SiteRequest(RequestMethod.PUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyAsStream(new ByteArrayInputStream(body)));
            assertEquals("PUT:été x,A:2", response.getText());

            // like Jetty, a malformed escape is a bad request
            for (var malformed : List.of("a=%zz", "a=1%", "a=%4", "%zz=1")) {
                response = runner.handle("/form", form(malformed));
                assertEquals(400, response.getStatus(), malformed);
                assertEquals("", response.getText());
            }
            assertEquals(400, runner.handle("/form?a=%zz", form("b=2")).getStatus());

            // htmx sends PATCH values in the body
            response = runner.handle("/form?a=q", new SiteRequest(RequestMethod.PATCH)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyAsBytes(body));
            assertEquals("PATCH:q,été x,A:2", response.getText());
            response = runner.handle("/body", new SiteRequest(RequestMethod.PATCH)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyAsStream(new ByteArrayInputStream(body)));
            assertEquals("2|a=%C3%A9t%C3%A9+x&b=2&a=%41", response.getText());
        }
    }

    @Test
    void testMultipartUpload() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                post("/upload", c -> {
                    var file = c.file("doc");
                    try {
                        c.print(c.parameter("title") + ":" + c.parameter("q") + ":" + file.getName() + ":" +
                                file.getContentType() + ":" + FileUtils.readString(file.getFile()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        })) {
            var boundary = "----runner42";
            var body = "--" + boundary + "\r\n" +
                       "Content-Disposition: form-data; name=\"title\"\r\n\r\n" +
                       "the title\r\n" +
                       "--" + boundary + "\r\n" +
                       "Content-Disposition: form-data; name=\"doc\"; filename=\"notes.txt\"\r\n" +
                       "Content-Type: text/plain\r\n\r\n" +
                       "line one\nline two\r\n" +
                       "--" + boundary + "--\r\n";
            var response = runner.handle("/upload?q=1", new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .bodyAsStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))));
            assertEquals("the title:1:notes.txt:text/plain:line one\nline two", response.getText());

            response = runner.handle("/upload?q=%zz", new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .bodyAsBytes(body.getBytes(StandardCharsets.UTF_8)));
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    void testCookiesFromHeader() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/cookies", c -> {
                    c.addCookie(new CookieBuilder("answer", "42").path("/").maxAge(60).httpOnly(true).sameSite(SameSite.STRICT));
                    c.print(c.cookieValue("one") + "," + c.cookieValue("two") + "," + c.cookieValue("three"));
                });
            }
        })) {
            var response = runner.handle("/cookies", new SiteRequest()
                .addHeader("Cookie", "one=1; two=\"2\"")
                .addHeader("cookie", "three=3"));
            assertEquals("1,2,3", response.getText());

            var set_cookie = response.getHttpHeaders().get("Set-Cookie");
            assertEquals(1, set_cookie.size());
            var value = set_cookie.get(0);
            assertTrue(value.startsWith("answer=42"), value);
            assertTrue(value.contains("; Max-Age=60; Expires="), value);
            assertTrue(value.contains("; Path=/"), value);
            assertTrue(value.contains("; HttpOnly"), value);
            assertTrue(value.contains("; SameSite=Strict"), value);
            assertFalse(value.contains("Secure"), value);
        }
    }

    @Test
    void testSessionAcrossRequests() {
        try (var runner = new SiteRunner().cookieJar(new SiteCookieJar()).start(new Site() {
            public void setup() {
                get("/count", c -> {
                    var session = c.session();
                    var count = (Integer) session.attribute("count");
                    count = null == count ? 1 : count + 1;
                    session.setAttribute("count", count);
                    c.print(count + ":" + session.isNew());
                });
                get("/logout", c -> c.session().invalidate());
            }
        })) {
            var first = runner.handle("/count");
            assertEquals("1:true", first.getText());
            var cookie = first.getHttpHeaders().get("Set-Cookie").get(0);
            assertTrue(cookie.startsWith("JSESSIONID="), cookie);
            assertTrue(cookie.contains("; HttpOnly"), cookie);

            assertEquals("2:false", runner.handle("/count").getText());
            assertEquals("3:false", runner.handle("/count").getText());
            runner.handle("/logout");
            assertEquals("1:true", runner.handle("/count").getText());
        }
    }

    @Test
    void testSessionExpiry() {
        var now = new AtomicLong(1_000_000);
        var runner = new SiteRunner().cookieJar(new SiteCookieJar());
        runner.clock(now::get);
        runner.start(new Site() {
            public void setup() {
                get("/session", c -> {
                    var session = c.session();
                    if (c.parameter("interval") != null) {
                        session.maxInactiveInterval(Integer.parseInt(c.parameter("interval")));
                    }
                    c.print(session.isNew());
                });
            }
        });
        try (runner) {
            assertEquals("true", runner.handle("/session?interval=1").getText());
            now.addAndGet(900);
            assertEquals("false", runner.handle("/session").getText());
            // each use starts the interval again
            now.addAndGet(900);
            assertEquals("false", runner.handle("/session").getText());
            now.addAndGet(1001);
            assertEquals("true", runner.handle("/session").getText());
        }
    }

    @Test
    void testSessionTimeoutAndPurge() {
        var now = new AtomicLong(1_000_000);
        var runner = new SiteRunner().sessionTimeout(60);
        runner.clock(now::get);
        runner.start(new Site() {
            public void setup() {
                get("/session", c -> c.print(c.session().maxInactiveInterval()));
            }
        });
        try (runner) {
            assertEquals("60", runner.handle("/session").getText());
            runner.handle("/session");
            assertEquals(2, runner.sessionCount());
            now.addAndGet(60_001);
            // creating a session purges the ones that expired
            runner.handle("/session");
            assertEquals(1, runner.sessionCount());
        }
    }

    @Test
    void testCookieJarPersistence() {
        var stored = new AtomicReference<Collection<SiteCookie>>(List.of());
        var store = new SiteCookieStore() {
            public Collection<SiteCookie> load() {
                return stored.get();
            }

            public void save(Collection<SiteCookie> cookies) {
                stored.set(new ArrayList<>(cookies));
            }
        };

        var site = new Site() {
            public void setup() {
                get("/login", c -> {
                    c.addCookie(new CookieBuilder("remember", "yes").path("/").maxAge(3600));
                    c.addCookie(new CookieBuilder("scoped", "s").path("/area"));
                    c.addCookie(new CookieBuilder("transient", "t").path("/"));
                    c.addCookie(new CookieBuilder("secure", "x").path("/").secure(true));
                });
                get("/area/page", c -> c.print(c.cookieValue("remember") + "," + c.cookieValue("scoped") + "," + c.cookieValue("transient") + "," + c.cookieValue("secure")));
                get("/areas", c -> c.print(c.cookieValue("remember") + "," + c.cookieValue("scoped")));
                get("/forget", c -> c.addCookie(new CookieBuilder("remember", "").path("/").maxAge(0)));
            }
        };

        try (var runner = new SiteRunner().cookieJar(new SiteCookieJar(store)).start(site)) {
            runner.handle("/login");
            // the path matches on segments and a secure cookie never came from an insecure origin
            assertEquals("yes,s,t,null", runner.handle("/area/page").getText());
            assertEquals("yes,null", runner.handle("/areas").getText());
        }

        assertEquals(1, stored.get().size());
        var remembered = stored.get().iterator().next();
        assertEquals("remember", remembered.name());
        assertFalse(remembered.isSessionCookie());

        try (var runner = new SiteRunner().cookieJar(new SiteCookieJar(store)).start(site)) {
            // a restart keeps the cookie with an expiry and forgets the session ones
            assertEquals("yes,null,null,null", runner.handle("/area/page").getText());
            runner.handle("/forget");
            assertEquals("null,null,null,null", runner.handle("/area/page").getText());
        }
        assertTrue(stored.get().isEmpty());
    }

    @Test
    void testContinuationAcrossRequests() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/simple", TestSimplePause::new);
            }
        })) {
            var lines = runner.handle("/simple").getText().split("\n");
            assertEquals("before simple pause", lines[0]);
            assertEquals("after simple pause", runner.handle("/simple?" + SpecialParameters.CONT_ID + "=" + lines[1]).getText());
        }
    }

    @Test
    void testStreamedResponse()
    throws Exception {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/download", c -> {
                    c.setHeader("X-Kind", "chunks");
                    c.response().setContentType("application/octet-stream");
                    try {
                        var out = c.outputStream();
                        for (var i = 0; i < 3; i++) {
                            out.write(("chunk" + i + ";").getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        })) {
            var recorder = new Recorder();
            var response = runner.handle("/download", new SiteRequest(), recorder);
            assertTrue(recorder.ended.await(5, TimeUnit.SECONDS));
            assertEquals("committed", recorder.calls.get(0));
            assertEquals("completed", recorder.last());
            assertEquals(3, Collections.frequency(recorder.calls, "content"));
            assertEquals(200, recorder.status);
            assertEquals(List.of("chunks"), recorder.headers.get("x-kind"));
            assertEquals("chunk0;chunk1;chunk2;", recorder.text());
            // the content went to the listener and wasn't kept
            assertEquals(0, response.getBytes().length);
            assertTrue(response.isFinished());
        }
    }

    @Test
    void testGzip() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/page", c -> c.print("x".repeat(1000)));
            }
        })) {
            var plain = runner.handle("/page");
            assertNull(plain.getHeader("Content-Encoding"));
            assertEquals("Accept-Encoding", plain.getHeader("Vary"));
            assertEquals(1000, plain.getBytes().length);

            var compressed = runner.handle("/page", new SiteRequest().header("Accept-Encoding", "gzip, deflate"));
            assertEquals("gzip", compressed.getHeader("Content-Encoding"));
            assertEquals("Accept-Encoding", compressed.getHeader("Vary"));
            assertEquals(compressed.getBytes().length, compressed.getContentLength());
            assertTrue(compressed.getBytes().length < 100);
        }
    }

    @Test
    void testEventStreamCancelledByHost()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }
        })) {
            var request = new SiteRequest();
            var recorder = new Recorder();
            var response = runner.handle("/events", request, recorder);

            // the element returned and the detached stream stays open
            assertFalse(response.isFinished());
            assertEquals(1, broadcaster.connectionCount());
            assertEquals(200, recorder.status);
            assertTrue(recorder.headers.get("Content-Type").get(0).startsWith("text/event-stream"));

            assertEquals(1, broadcaster.send(new ServerSentEvent().name("tick").data("1")));
            assertTrue(recorder.text().contains("event: tick\ndata: 1\n"), recorder.text());

            request.cancel();
            assertEquals(0, broadcaster.connectionCount());
            assertTrue(response.isFinished());
            assertEquals(0, broadcaster.send("after cancel"));
            // a cancelled response only ends with its cancelled call
            assertFalse(recorder.calls.contains("completed"));
            assertFalse(recorder.calls.contains("failed"));
            assertEquals("cancelled", recorder.last());
        }
    }

    @Test
    void testEventStreamClosedBySite()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }
        })) {
            var recorder = new Recorder();
            runner.handle("/events", new SiteRequest(), recorder);
            broadcaster.send("hello");
            broadcaster.close();
            assertTrue(recorder.ended.await(5, TimeUnit.SECONDS));
            assertEquals("completed", recorder.last());
            assertTrue(recorder.text().contains("data: hello\n"), recorder.text());
        }
    }

    @Test
    void testStaticFiles(@TempDir Path root)
    throws Exception {
        var directory = Files.createDirectories(root.resolve("site"));
        Files.writeString(root.resolve("outside.txt"), "secret");
        Files.writeString(directory.resolve("data.txt"), "0123456789");
        Files.createDirectories(directory.resolve("sub"));
        Files.writeString(directory.resolve("sub/style.css"), "body {}");
        try (var runner = new SiteRunner().staticResourceBase(directory.toString()).start(new Site() {
            public void setup() {
                get("/page", c -> c.print("page"));
            }
        })) {
            var whole = runner.handle("/data.txt");
            assertTrue(whole.isHandled());
            assertEquals(200, whole.getStatus());
            assertEquals("0123456789", whole.getText());
            assertEquals("text/plain", whole.getContentType());
            assertEquals("10", whole.getHeader("Content-Length"));
            assertEquals("bytes", whole.getHeader("Accept-Ranges"));
            var etag = whole.getHeader("ETag");
            assertNotNull(etag);

            var range = runner.handle("/data.txt", new SiteRequest().header("Range", "bytes=2-5"));
            assertEquals(206, range.getStatus());
            assertEquals("2345", range.getText());
            assertEquals("bytes 2-5/10", range.getHeader("Content-Range"));
            assertEquals("4", range.getHeader("Content-Length"));

            assertEquals("789", runner.handle("/data.txt", new SiteRequest().header("Range", "bytes=-3")).getText());
            assertEquals("89", runner.handle("/data.txt", new SiteRequest().header("Range", "bytes=8-")).getText());
            var unsatisfiable = runner.handle("/data.txt", new SiteRequest().header("Range", "bytes=20-30"));
            assertEquals(416, unsatisfiable.getStatus());
            assertEquals("bytes */10", unsatisfiable.getHeader("Content-Range"));
            // an outdated If-Range gives the whole file
            assertEquals(200, runner.handle("/data.txt", new SiteRequest().header("Range", "bytes=2-5").header("If-Range", "\"old\"")).getStatus());

            var not_modified = runner.handle("/data.txt", new SiteRequest().header("If-None-Match", etag));
            assertEquals(304, not_modified.getStatus());
            assertEquals(0, not_modified.getBytes().length);
            var since = runner.handle("/data.txt", new SiteRequest().header("If-Modified-Since", whole.getHeader("Last-Modified")));
            assertEquals(304, since.getStatus());

            var head = runner.handle("/data.txt", new SiteRequest(RequestMethod.HEAD));
            assertEquals(200, head.getStatus());
            assertEquals("10", head.getHeader("Content-Length"));
            assertEquals(0, head.getBytes().length);

            assertEquals("body {}", runner.handle("/sub/style.css").getText());
            assertEquals("text/css", runner.handle("/sub/style.css").getContentType());

            assertEquals("page", runner.handle("/page").getText());

            for (var path : List.of("/../outside.txt", "/sub/../../outside.txt", "/%2e%2e/outside.txt", "/sub", "/sub/", "/missing.txt")) {
                var response = runner.handle(path);
                assertFalse(response.isHandled(), path);
                assertEquals(404, response.getStatus(), path);
            }

            // only GET and HEAD reach the files
            assertFalse(runner.handle("/data.txt", new SiteRequest(RequestMethod.POST)).isHandled());
        }
    }

    @Test
    void testStaticClasspathFiles() {
        try (var runner = new SiteRunner().staticUberJarResourceBase("templates").start(new Site())) {
            var response = runner.handle("/sse_param.html");
            assertTrue(response.isHandled());
            assertEquals("text/html", response.getContentType());
            assertTrue(response.getText().contains("param:user"));
            assertFalse(runner.handle("/../templates/sse_param.html").isHandled());
        }
    }

    @Test
    void testStaticStreamed(@TempDir Path directory)
    throws Exception {
        var bytes = new byte[200_000];
        new Random(4).nextBytes(bytes);
        Files.write(directory.resolve("media.bin"), bytes);
        try (var runner = new SiteRunner().staticResourceBase(directory.toString()).start(new Site())) {
            var recorder = new Recorder();
            runner.handle("/media.bin", new SiteRequest().header("Range", "bytes=1000-150999"), recorder);
            assertTrue(recorder.ended.await(5, TimeUnit.SECONDS));
            assertEquals(206, recorder.status);
            assertEquals(List.of("150000"), recorder.headers.get("Content-Length"));
            assertArrayEquals(Arrays.copyOfRange(bytes, 1000, 151000), recorder.bytes());
        }
    }

    @Test
    void testHead() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/page", c -> {
                    c.setHeader("X-Page", "yes");
                    c.print("content");
                });
            }
        })) {
            var recorder = new Recorder();
            runner.handle("/page", new SiteRequest(RequestMethod.HEAD), recorder);
            assertEquals(200, recorder.status);
            assertEquals(List.of("yes"), recorder.headers.get("X-Page"));
            assertFalse(recorder.calls.contains("content"));
            assertEquals("completed", recorder.last());
        }
    }

    @Test
    void testRedirect() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/old", c -> c.redirect("/new"));
            }
        })) {
            var recorder = new Recorder();
            var response = runner.handle("/old", new SiteRequest(), recorder);
            assertEquals(302, response.getStatus());
            assertEquals("/new", response.getHeader("Location"));
            assertEquals(List.of("committed", "completed"), recorder.calls);
            assertEquals(302, recorder.status);
        }
    }

    @Test
    void testExceptionRoute() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                exception(c -> {
                    c.setStatus(503);
                    c.print("handled " + c.engineException().getCause().getMessage());
                });
                get("/fail", c -> {
                    throw new IllegalStateException("boom");
                });
            }
        })) {
            var response = runner.handle("/fail");
            assertEquals(503, response.getStatus());
            assertEquals("handled boom", response.getText());
        }
    }

    @Test
    void testUnhandledExceptions() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/fail", c -> {
                    throw new IllegalStateException("boom");
                });
            }
        })) {
            RifeConfig.engine().setLogEngineExceptions(false);

            RifeConfig.engine().setPrettyEngineExceptions(true);
            var response = runner.handle("/fail");
            assertEquals(500, response.getStatus());
            assertTrue(response.getText().contains("boom"));

            RifeConfig.engine().setPrettyEngineExceptions(false);
            var thrown = assertThrows(RuntimeException.class, () -> runner.handle("/fail"));
            assertInstanceOf(IllegalStateException.class, thrown.getCause());

            var recorder = new Recorder();
            runner.handle("/fail", new SiteRequest(), recorder);
            assertEquals("failed", recorder.last());
            assertInstanceOf(IllegalStateException.class, recorder.failure.getCause());
        }
    }

    @Test
    void testUnrouted() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/missing", c -> c.setStatus(404));
                get("/defer", Context::defer);
            }
        })) {
            var unrouted = runner.handle("/nowhere");
            assertFalse(unrouted.isHandled());
            assertEquals(404, unrouted.getStatus());

            var answered = runner.handle("/missing");
            assertTrue(answered.isHandled());
            assertEquals(404, answered.getStatus());

            assertFalse(runner.handle("/defer").isHandled());
            // pass-through suffixes aren't routed and there are no static files
            assertFalse(runner.handle("/image.png").isHandled());

            var recorder = new Recorder();
            runner.handle("/nowhere", new SiteRequest(), recorder);
            assertEquals(List.of("committed", "completed"), recorder.calls);
            assertEquals(404, recorder.status);
        }
    }

    @Test
    void testConcurrentRequests()
    throws Exception {
        var inside = new AtomicInteger();
        var most = new AtomicInteger();
        var all_inside = new CountDownLatch(8);
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/work", c -> {
                    var count = inside.incrementAndGet();
                    most.accumulateAndGet(count, Math::max);
                    all_inside.countDown();
                    try {
                        all_inside.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    c.session().setAttribute("n", c.parameter("n"));
                    c.print(c.parameter("n") + ":" + Thread.currentThread().getName());
                    inside.decrementAndGet();
                });
            }
        })) {
            var executor = Executors.newFixedThreadPool(8);
            try {
                var futures = new ArrayList<Future<String>>();
                for (var i = 0; i < 8; i++) {
                    var n = i;
                    futures.add(executor.submit(() -> runner.handle("/work?n=" + n).getText() + ":" + Thread.currentThread().getName()));
                }
                for (var i = 0; i < 8; i++) {
                    var parts = futures.get(i).get(10, TimeUnit.SECONDS).split(":");
                    assertEquals(String.valueOf(i), parts[0]);
                    // each request ran on the thread that called handle
                    assertEquals(parts[2], parts[1]);
                }
            } finally {
                executor.shutdownNow();
            }
            assertEquals(8, most.get());
        }
    }

    @Test
    void testRequestDetails() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                post("/details", c -> {
                    var request = c.request();
                    c.print(request.getLocale() + "|" + Collections.list(request.getLocales()) + "|" +
                            request.getHeader("x-custom") + "|" + Collections.list(request.getHeaders("X-CUSTOM")) + "|" +
                            request.getRemoteAddr() + "|" + request.getProtocol() + "|" + request.getContentType() + "|" +
                            c.request().getBody() + "|" + (request.getHttpServletRequest() == null));
                });
            }
        })) {
            var response = runner.handle("/details", new SiteRequest(RequestMethod.POST)
                .header("Accept-Language", "fr-BE;q=0.8, nl, en;q=0.5, *;q=0.1")
                .addHeader("X-Custom", "one")
                .addHeader("x-custom", "two")
                .remoteAddr("10.0.0.2")
                .contentType("application/json")
                .body("{\"a\":1}"));
            assertEquals("nl|[nl, fr_BE, en]|one|[one, two]|10.0.0.2|HTTP/1.1|application/json|{\"a\":1}|true", response.getText());
        }
    }

    @Test
    void testLifecycle() {
        var runner = new SiteRunner();
        try {
            assertThrows(IllegalStateException.class, () -> runner.handle("/"));
            runner.start(new Site() {
                public void setup() {
                    get("/", c -> c.print("up"));
                }
            });
            assertThrows(IllegalStateException.class, () -> runner.start(new Site()));
            assertEquals("up", runner.handle("/").getText());

            try (var other = new SiteRunner().start(new Site() {
                public void setup() {
                    get("/", c -> c.print("other"));
                }
            })) {
                assertEquals("other", other.handle("/").getText());
                assertEquals("up", runner.handle("/").getText());
            }

            runner.stop();
            runner.stop();
            assertThrows(IllegalStateException.class, () -> runner.handle("/"));
            assertThrows(IllegalStateException.class, () -> runner.start(new Site()));
        } finally {
            runner.stop();
        }
    }

    @Test
    void testStoppedRunnerWithListener() {
        var runner = new SiteRunner();
        // calling it before the start is a mistake, which still throws
        assertThrows(IllegalStateException.class, () -> runner.handle("/", new SiteRequest(), new Recorder()));
        runner.start(new Site() {
            public void setup() {
                get("/", c -> c.print("up"));
            }
        });
        runner.stop();

        // a request that was still on its way gets its final call
        var closed = new AtomicBoolean();
        var request = new SiteRequest(RequestMethod.POST).bodyAsStream(new ByteArrayInputStream(new byte[]{1}) {
            public void close() {
                closed.set(true);
            }
        });
        var recorder = new Recorder();
        var response = runner.handle("/", request, recorder);
        assertEquals(List.of("failed"), recorder.calls);
        assertInstanceOf(IllegalStateException.class, recorder.failure);
        assertTrue(response.isFinished());
        assertTrue(closed.get());

        var cancelled = new SiteRequest();
        cancelled.cancel();
        var cancelled_recorder = new Recorder();
        runner.handle("/", cancelled, cancelled_recorder);
        assertEquals(List.of("cancelled"), cancelled_recorder.calls);
    }

    @Test
    void testCancelStreamedDownload()
    throws Exception {
        var element_failed = new CountDownLatch(1);
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/download", c -> {
                    c.response().setContentType("application/octet-stream");
                    try {
                        var out = c.outputStream();
                        for (var i = 0; i < 1000; i++) {
                            out.write(("chunk" + i).getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    } catch (IOException | EngineException e) {
                        element_failed.countDown();
                    }
                });
            }
        })) {
            var request = new SiteRequest();
            var recorder = new Recorder() {
                public void content(byte[] bytes, int offset, int length) {
                    super.content(bytes, offset, length);
                    if (Collections.frequency(calls, "content") == 2) {
                        request.cancel();
                    }
                }
            };
            var response = runner.handle("/download", request, recorder);
            assertTrue(element_failed.await(5, TimeUnit.SECONDS));
            assertEquals(List.of("committed", "content", "content", "cancelled"), recorder.calls);
            assertEquals("chunk0chunk1", recorder.text());
            assertTrue(response.isFinished());
        }
    }

    @Test
    void testListenerThatThrows() {
        var element_failed = new AtomicReference<Boolean>(false);
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/download", c -> {
                    try {
                        var out = c.outputStream();
                        out.write("one".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    } catch (IOException | EngineException e) {
                        element_failed.set(true);
                    }
                });
            }
        })) {
            var recorder = new Recorder() {
                public void committed(SiteResponse response) {
                    super.committed(response);
                    throw new IllegalStateException("the host's stream is gone");
                }
            };
            var response = runner.handle("/download", new SiteRequest(), recorder);
            // the failing listener cancelled the request, so nothing more reached it
            assertEquals(List.of("committed", "cancelled"), recorder.calls);
            assertTrue(element_failed.get());
            assertTrue(response.isFinished());
        }
    }

    @Test
    void testStopEndsEventStreams()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }
        });
        try {
            var recorder = new Recorder();
            var response = runner.handle("/events", new SiteRequest(), recorder);
            assertEquals(1, broadcaster.connectionCount());

            runner.stop();
            assertTrue(recorder.ended.await(5, TimeUnit.SECONDS));
            assertEquals("completed", recorder.last());
            assertTrue(response.isFinished());
            assertEquals(0, broadcaster.connectionCount());
        } finally {
            runner.stop();
        }
    }

    @Test
    void testStopAfterFailingDestroy()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }

            public void destroy() {
                throw new IllegalStateException("the site can't be shut down");
            }
        });
        var recorder = new Recorder();
        var response = runner.handle("/events", new SiteRequest(), recorder);
        var session = runner.createSession();

        assertThrows(IllegalStateException.class, runner::stop);
        // the streams and the sessions still end
        assertTrue(recorder.ended.await(5, TimeUnit.SECONDS));
        assertTrue(response.isFinished());
        assertEquals(0, broadcaster.connectionCount());
        assertNull(runner.findSession(session.getId()));
    }

    @Test
    void testEventStreamClosedByElementStoresCookiesOnce() {
        var stored = new AtomicInteger();
        var jar = new SiteCookieJar() {
            void store(SiteRequest request, Collection<Cookie> cookies) {
                stored.incrementAndGet();
                super.store(request, cookies);
            }
        };
        try (var runner = new SiteRunner().cookieJar(jar).start(new Site() {
            public void setup() {
                get("/events", c -> {
                    c.addCookie(new CookieBuilder("seen", "yes").path("/"));
                    c.sse(new SseBroadcaster()).close();
                });
            }
        })) {
            var response = runner.handle("/events");
            assertTrue(response.isFinished());
            assertEquals(1, stored.get());
            assertEquals("yes", jar.getCookies().get(0).value());
        }
    }

    @Test
    void testSessionAfterCommit() {
        var refused = new AtomicBoolean();
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/late", c -> {
                    try {
                        var out = c.outputStream();
                        out.write("one".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        c.session();
                    } catch (IllegalStateException e) {
                        refused.set(true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        })) {
            // like Jetty, the session cookie can't reach the listener anymore
            var recorder = new Recorder();
            runner.handle("/late", new SiteRequest(), recorder);
            assertTrue(refused.get());
            assertEquals("one", recorder.text());

            // without a listener, the cookies are only final at the end
            refused.set(false);
            var response = runner.handle("/late");
            assertFalse(refused.get());
            assertEquals(runner.getSessionCookieName(), response.getNewCookies().get(0).getName());
        }
    }

    @Test
    void testWholeResponseClosesEventStream() {
        var broadcaster = new SseBroadcaster();
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster).send("first"));
            }
        })) {
            var response = runner.handle("/events");
            assertTrue(response.isFinished());
            assertEquals(0, broadcaster.connectionCount());
            assertTrue(response.getText().contains("data: first\n"), response.getText());
        }
    }

    @Test
    void testMalformedPaths(@TempDir Path root)
    throws Exception {
        var directory = Files.createDirectories(root.resolve("site"));
        Files.writeString(directory.resolve("data.txt"), "data");
        Files.createDirectories(directory.resolve("WEB-INF"));
        Files.writeString(directory.resolve("WEB-INF/web.xml"), "<web-app/>");
        Files.createDirectories(directory.resolve("meta-inf"));
        Files.writeString(directory.resolve("meta-inf/secret.txt"), "secret");
        var outside = Files.writeString(root.resolve("outside.txt"), "outside");
        var paths = new ArrayList<>(List.of("/img%zz.png", "/nowhere%", "/a%zz", "/%", "/WEB-INF/web.xml", "/web-inf/web.xml", "/meta-inf/secret.txt"));
        try {
            Files.createSymbolicLink(directory.resolve("link.txt"), outside);
            paths.add("/link.txt");
        } catch (IOException | UnsupportedOperationException e) {
            // creating symbolic links needs a privilege on Windows
        }

        for (var static_files : List.of(true, false)) {
            var runner = new SiteRunner();
            if (static_files) {
                runner.staticResourceBase(directory.toString());
            }
            try (runner) {
                runner.start(new Site());
                for (var path : paths) {
                    var response = runner.handle(path);
                    assertFalse(response.isHandled(), path);
                    assertEquals(404, response.getStatus(), path);
                }
                assertEquals(static_files, runner.handle("/data.txt").isHandled());
            }
        }

        assertThrows(IllegalArgumentException.class, () -> new SiteRunner().staticUberJarResourceBase(""));
        assertThrows(IllegalArgumentException.class, () -> new SiteRunner().staticUberJarResourceBase("/"));
    }

    @Test
    void testStaticJarResources()
    throws Exception {
        var entry = ((JarURLConnection) getClass().getClassLoader().getResource("jakarta/servlet/LocalStrings.properties").openConnection()).getJarEntry();
        var size = entry.getSize();
        try (var runner = new SiteRunner().staticUberJarResourceBase("jakarta/servlet").start(new Site())) {
            var response = runner.handle("/LocalStrings.properties");
            assertTrue(response.isHandled());
            assertEquals(200, response.getStatus());
            assertEquals(String.valueOf(size), response.getHeader("Content-Length"));
            assertTrue(response.getText().contains("err.not_iso8859_1"), response.getText());
            // the entries of a jar are versioned by their content
            var etag = response.getHeader("ETag");
            assertEquals("\"" + Long.toHexString(entry.getCrc()) + "-" + Long.toHexString(size) + "\"", etag);
            assertEquals(304, runner.handle("/LocalStrings.properties", new SiteRequest().header("If-None-Match", etag)).getStatus());

            var range = runner.handle("/LocalStrings.properties", new SiteRequest().header("Range", "bytes=0-3"));
            assertEquals(206, range.getStatus());
            assertEquals("bytes 0-3/" + size, range.getHeader("Content-Range"));
            assertEquals(4, range.getBytes().length);
            // the directories of a jar aren't files
            assertFalse(runner.handle("/http").isHandled());
        }
    }

    @Test
    void testMultipartLimits() {
        var boundary = "----limits";
        var body = ("--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"doc\"; filename=\"big.txt\"\r\n" +
                    "Content-Type: text/plain\r\n\r\n" +
                    "x".repeat(100) + "\r\n" +
                    "--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                post("/upload", c -> c.print(c.file("doc").wasSizeExceeded()));
            }
        })) {
            RifeConfig.engine().setFileUploadSizeLimit(10).setFileUploadSizeCheck(true).setFileUploadSizeException(true);
            // the web view gets an answer, like with a form that's too large
            assertEquals(413, runner.handle("/upload", new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary).bodyAsBytes(body)).getStatus());

            var recorder = new Recorder();
            runner.handle("/upload", new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary).bodyAsBytes(body), recorder);
            assertEquals(List.of("committed", "completed"), recorder.calls);
            assertEquals(413, recorder.status);

            // a body that can't be read as multipart is a bad request
            assertEquals(400, runner.handle("/upload", new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "multipart/form-data").bodyAsBytes(body)).getStatus());

            // an upload directory that can't be used is the site's own failure
            var path = RifeConfig.engine().getFileUploadPath();
            try {
                var not_a_directory = File.createTempFile("not-a-directory", ".txt");
                not_a_directory.deleteOnExit();
                RifeConfig.engine().setFileUploadPath(not_a_directory.getAbsolutePath());
                var failing = new Recorder();
                runner.handle("/upload", new SiteRequest(RequestMethod.POST)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary).bodyAsBytes(body), failing);
                assertEquals(List.of("failed"), failing.calls);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                RifeConfig.engine().setFileUploadPath(path);
            }

            RifeConfig.engine().setFileUploadSizeException(false);
            assertEquals("true", runner.handle("/upload", new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary).bodyAsBytes(body)).getText());
        }
    }

    @Test
    void testGzipStreamed() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/page", c -> c.print("x".repeat(1000)));
            }
        })) {
            var recorder = new Recorder();
            runner.handle("/page", new SiteRequest().header("Accept-Encoding", "gzip"), recorder);
            assertEquals(List.of("gzip"), recorder.headers.get("Content-Encoding"));
            var compressed = recorder.bytes();
            assertEquals(List.of(String.valueOf(compressed.length)), recorder.headers.get("Content-Length"));
            assertTrue(compressed.length < 100);
        }
    }

    @Test
    void testCookieJarMatching() {
        assertTrue(SiteCookieJar.domainMatches("app.example.com", "example.com"));
        assertTrue(SiteCookieJar.domainMatches("example.com", "example.com"));
        assertFalse(SiteCookieJar.domainMatches("badexample.com", "example.com"));
        assertFalse(SiteCookieJar.domainMatches("app.example.com", "com"));
        assertFalse(SiteCookieJar.domainMatches("127.0.0.1", "0.0.1"));
        assertTrue(SiteCookieJar.pathMatches("/area/page", "/area"));
        assertTrue(SiteCookieJar.pathMatches("/area/page", "/area/"));
        assertFalse(SiteCookieJar.pathMatches("/areas", "/area"));
        assertEquals("/a/b", SiteCookieJar.defaultPath("/a/b/c"));
        assertEquals("/", SiteCookieJar.defaultPath("/a"));

        var jar = new SiteCookieJar();
        var site = new Site() {
            public void setup() {
                get("/set", c -> {
                    c.addCookie(new CookieBuilder("shared", "s").domain("example.com").path("/"));
                    c.addCookie(new CookieBuilder("tld", "t").domain("com").path("/"));
                    c.addCookie(new CookieBuilder("own", "o").path("/"));
                });
                get("/read", c -> c.print(c.cookieValue("shared") + "," + c.cookieValue("tld") + "," + c.cookieValue("own")));
            }
        };
        try (var app = new SiteRunner().serverName("app.example.com").cookieJar(jar).start(site);
             var other = new SiteRunner().serverName("other.example.com").cookieJar(jar).start(site)) {
            app.handle("/set");
            assertEquals("s,null,o", app.handle("/read").getText());
            // a host-only cookie stays with its host
            assertEquals("s,null,null", other.handle("/read").getText());
        }
    }

    @Test
    void testSecureCookies() {
        var jar = new SiteCookieJar();
        try (var runner = new SiteRunner().scheme("myapp").serverName("app").secure(true).cookieJar(jar).start(new Site() {
            public void setup() {
                get("/set", c -> c.addCookie(new CookieBuilder("remember", c.parameter("v")).path("/").maxAge(60).secure(true)));
                get("/plain", c -> c.addCookie(new CookieBuilder("remember", "plain").path("/").maxAge(60)));
                get("/read", c -> c.print(c.cookieValue("remember") + ":" + c.request().isSecure()));
                get("/session", c -> c.session());
            }
        })) {
            runner.handle("/set?v=yes");
            assertEquals("yes:true", runner.handle("/read").getText());
            assertTrue(runner.handle("/session").getHttpHeaders().get("Set-Cookie").get(0).contains("; Secure"));

            // an insecure request can neither read nor replace the secure cookie
            assertEquals("null:false", runner.handle("/read", new SiteRequest().secure(false)).getText());
            runner.handle("/plain", new SiteRequest().secure(false));
            assertEquals("yes:true", runner.handle("/read").getText());
        }
    }

    @Test
    void testCookieValidation() {
        try (var runner = new SiteRunner().cookieJar(new SiteCookieJar()).start(new Site() {
            public void setup() {
                get("/value", c -> c.addCookie(new CookieBuilder("search", "a; JSESSIONID=stolen")));
                get("/attribute", c -> c.addCookie(new CookieBuilder("search", "a").path("/; Domain=evil.com")));
                get("/quoted", c -> c.addCookie(new CookieBuilder("search", "\"quoted\"")));
            }
        })) {
            RifeConfig.engine().setPrettyEngineExceptions(true).setLogEngineExceptions(false);
            for (var path : List.of("/value", "/attribute")) {
                var response = runner.handle(path);
                assertEquals(500, response.getStatus(), path);
                assertNull(response.getHttpHeaders().get("Set-Cookie"), path);
            }
            assertTrue(runner.handle("/quoted").getHttpHeaders().get("Set-Cookie").get(0).startsWith("search=\"quoted\""));
        }

        assertTrue(SiteResponse.isValidCookieValue("abc-123_%2F"));
        assertFalse(SiteResponse.isValidCookieValue("a b"));
        assertFalse(SiteResponse.isValidCookieValue("a\r\nb"));
        assertFalse(SiteResponse.isValidAttributeValue("/\r\nSet-Cookie: x"));
    }

    @Test
    void testOriginMatching() {
        assertTrue(SiteRunner.isSameOrigin("myapp://app/x", "myapp://app"));
        assertTrue(SiteRunner.isSameOrigin("MYAPP://APP/x", "myapp://app"));
        assertTrue(SiteRunner.isSameOrigin("myapp://app", "myapp://app"));
        assertFalse(SiteRunner.isSameOrigin("myapp://app.evil.com/x", "myapp://app"));
        assertFalse(SiteRunner.isSameOrigin("myapp://apple/x", "myapp://app"));
        assertFalse(SiteRunner.isSameOrigin("myapp://app@evil.com/x", "myapp://app"));
        assertFalse(SiteRunner.isSameOrigin("myapp://app:9999/x", "myapp://app"));

        var jar = new SiteCookieJar();
        try (var runner = new SiteRunner().scheme("myapp").serverName("app").cookieJar(jar).start(new Site() {
            public void setup() {
                get("/set", c -> c.addCookie(new CookieBuilder("token", "t").path("/")));
                get("/proxy", PathInfoHandling.CAPTURE, c -> c.print(c.pathInfo()));
            }
        })) {
            runner.handle("/set");
            var foreign = new SiteRequest();
            assertFalse(runner.handle("myapp://app.evil.com/set", foreign).isHandled());
            // the cookies of the origin don't go along with a foreign request
            assertNull(foreign.getHeader("Cookie"));
            assertEquals("http://a", runner.handle("/proxy/http://a").getText());
        }
    }

    @Test
    void testFormLimits() {
        try (var runner = new SiteRunner().maxFormContentSize(20).maxFormParameters(3).start(new Site() {
            public void setup() {
                post("/form", c -> c.print(c.parameter("a")));
            }
        })) {
            assertEquals("1", runner.handle("/form", form("a=1&b=2&c=3")).getText());
            var too_large = runner.handle("/form", form("a=1&b=" + "x".repeat(30)));
            assertEquals(400, too_large.getStatus());
            assertEquals("", too_large.getText());
            assertEquals(400, runner.handle("/form", form("a=1&b=2&c=3&d=4")).getStatus());
            // like Jetty, the values of one name count once
            assertEquals("1", runner.handle("/form", form("a=1&a=2&a=3&a=4")).getText());
            var streamed = new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyAsStream(new ByteArrayInputStream(("a=" + "x".repeat(100)).getBytes(StandardCharsets.UTF_8)));
            assertEquals(400, runner.handle("/form", streamed).getStatus());
        }
        assertEquals(200000, new SiteRunner().getMaxFormContentSize());
        assertEquals(1000, new SiteRunner().getMaxFormParameters());
    }

    private static SiteRequest form(String body) {
        return new SiteRequest(RequestMethod.POST)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyAsBytes(body.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testSessionUrlParameters() {
        var site = new Site() {
            public void setup() {
                get("/create", c -> {
                    c.session().setAttribute("who", "me");
                    c.print(c.session().id() + "|" + c.response().encodeURL("/next"));
                });
                get("/read", c -> c.print(c.session(false) == null ? "none" : c.session().attribute("who")));
            }
        };
        try (var runner = new SiteRunner().start(site)) {
            var parts = runner.handle("/create").getText().split("\\|");
            assertEquals("/next", parts[1]);
            assertEquals("none", runner.handle("/read;jsessionid=" + parts[0]).getText());
        }
        try (var runner = new SiteRunner().sessionUrlParameters(true).start(site)) {
            var parts = runner.handle("/create").getText().split("\\|");
            assertEquals("/next;jsessionid=" + parts[0], parts[1]);
            assertEquals("me", runner.handle("/read;jsessionid=" + parts[0]).getText());
        }
    }

    @Test
    void testSetupErrors() {
        RifeConfig.engine().setLogEngineExceptions(false);
        RifeConfig.engine().setPrettyEngineExceptions(true);
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                throw new IllegalStateException("broken setup");
            }
        })) {
            var response = runner.handle("/anything");
            assertEquals(500, response.getStatus());
            assertTrue(response.getText().contains("broken setup"));
        }

        RifeConfig.engine().setPrettyEngineExceptions(false);
        assertThrows(IllegalStateException.class, () -> new SiteRunner().start(new Site() {
            public void setup() {
                throw new IllegalStateException("broken setup");
            }
        }));
    }

    @Test
    void testNoContinuationLeftOnThread() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/simple", TestSimplePause::new);
            }
        })) {
            var lines = runner.handle("/simple").getText().split("\n");
            assertNull(ContinuationContext.getActiveContext());
            runner.handle("/simple?" + SpecialParameters.CONT_ID + "=" + lines[1]);
            assertNull(ContinuationContext.getActiveContext());
        }
    }

    @Test
    void testHandlingMisuse() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/", c -> c.print("up"));
            }
        })) {
            var recorder = new Recorder();
            assertThrows(IllegalArgumentException.class, () -> runner.handle(null, new SiteRequest(), recorder));
            assertThrows(IllegalArgumentException.class, () -> runner.handle("/", null, recorder));
            assertThrows(IllegalArgumentException.class, () -> runner.handle("/", new SiteRequest(), null));

            var request = new SiteRequest();
            runner.handle("/", request);
            assertThrows(IllegalStateException.class, () -> runner.handle("/", request));
            assertThrows(IllegalStateException.class, () -> runner.handle("/", request, recorder));
            assertTrue(recorder.calls.isEmpty());
        }
    }

    @Test
    void testResponseDetails() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/text", c -> {
                    c.setHeader("X-Gone", "soon");
                    c.response().setHeader("X-Gone", null);
                    c.response().setContentType("text/plain; charset=UTF-8; format=flowed");
                    c.print("été");
                });
                get("/defer", c -> {
                    c.setStatus(201);
                    c.defer();
                });
            }
        })) {
            var response = runner.handle("/text");
            assertNull(response.getHeader("X-Gone"));
            assertFalse(response.getHttpHeaders().containsKey("X-Gone"));
            assertEquals("UTF-8", response.getCharacterEncoding());
            assertEquals("été", response.getText());
            assertEquals(List.of("5"), response.getHttpHeaders().get("Content-Length"));

            var head = runner.handle("/text", new SiteRequest(RequestMethod.HEAD));
            assertEquals(List.of("5"), head.getHttpHeaders().get("Content-Length"));

            var deferred = runner.handle("/defer");
            assertFalse(deferred.isHandled());
            assertEquals(404, deferred.getStatus());
        }
    }

    @Test
    void testCancelWhileListenerBlocks()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }
        })) {
            var request = new SiteRequest();
            var in_content = new CountDownLatch(1);
            var cancelled = new CountDownLatch(1);
            runner.handle("/events", request, new Recorder() {
                public void content(byte[] bytes, int offset, int length) {
                    super.content(bytes, offset, length);
                    // the other thread cancels while this listener call is still in progress
                    in_content.countDown();
                    try {
                        cancelled.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });

            var sender = new Thread(() -> broadcaster.send("blocked"));
            sender.start();
            assertTrue(in_content.await(5, TimeUnit.SECONDS));

            var canceller = new Thread(request::cancel);
            canceller.start();
            canceller.join(2000);
            assertFalse(canceller.isAlive(), "cancel() waited for the listener");
            cancelled.countDown();
            sender.join(5000);
            assertFalse(sender.isAlive());
            assertEquals(0, broadcaster.connectionCount());
        }
    }

    @Test
    void testCookieStoreSavesOnlyChanges() {
        var saves = new AtomicInteger();
        var store = new SiteCookieStore() {
            public Collection<SiteCookie> load() {
                return List.of();
            }

            public void save(Collection<SiteCookie> cookies) {
                saves.incrementAndGet();
            }
        };
        try (var runner = new SiteRunner().cookieJar(new SiteCookieJar(store)).start(new Site() {
            public void setup() {
                get("/remember", c -> c.addCookie(new CookieBuilder("remember", c.parameter("v")).path("/").maxAge(3600)));
                get("/session", c -> c.addCookie(new CookieBuilder("transient", "t").path("/")));
                get("/stream", c -> {
                    c.addCookie(new CookieBuilder("remember", "streamed").path("/").maxAge(3600));
                    c.print("streamed");
                });
            }
        })) {
            runner.handle("/remember?v=1");
            assertEquals(1, saves.get());
            // a session cookie isn't stored, so there's nothing to save
            runner.handle("/session");
            assertEquals(1, saves.get());
            runner.handle("/remember?v=2");
            assertEquals(2, saves.get());
            runner.handle("/stream", new SiteRequest(), new Recorder());
            assertEquals(3, saves.get());
        }
    }

    @Test
    void testQueryParsedAlikeForEveryBody() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                post("/echo", c -> c.print(String.join(",", c.parameterValues("q")) + "|" + c.parameter("p")));
            }
        })) {
            var boundary = "----alike";
            var multipart = ("--" + boundary + "\r\n" +
                             "Content-Disposition: form-data; name=\"q\"\r\n\r\n" +
                             "body\r\n" +
                             "--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
            var query = "/echo?q=%C3%A9t%C3%A9&q=%2B&p=a+b";
            var urlencoded = runner.handle(query, new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyAsBytes("q=body".getBytes(StandardCharsets.UTF_8))).getText();
            var multipart_text = runner.handle(query, new SiteRequest(RequestMethod.POST)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .bodyAsBytes(multipart)).getText();
            assertEquals("été,+,body|a b", urlencoded);
            assertEquals(urlencoded, multipart_text);
        }
    }

    @Test
    void testStaticBaseIsReplaced(@TempDir Path directory)
    throws Exception {
        Files.writeString(directory.resolve("only-in-directory.txt"), "directory");
        var runner = new SiteRunner()
            .staticResourceBase(directory.toString())
            .staticUberJarResourceBase("templates");
        try (runner) {
            runner.start(new Site());
            // like Server, the later base replaces the earlier one
            assertFalse(runner.handle("/only-in-directory.txt").isHandled());
            assertTrue(runner.handle("/sse_param.html").isHandled());
        }

        var reversed = new SiteRunner()
            .staticUberJarResourceBase("templates")
            .staticResourceBase(directory.toString());
        try (reversed) {
            reversed.start(new Site());
            assertTrue(reversed.handle("/only-in-directory.txt").isHandled());
            assertFalse(reversed.handle("/sse_param.html").isHandled());
        }
    }

    @Test
    void testRangeParsing() {
        assertArrayEquals(new long[]{0, 9}, SiteStaticResources.parseRange("bytes=0-", 10));
        assertArrayEquals(new long[]{2, 5}, SiteStaticResources.parseRange("BYTES=2-5", 10));
        assertArrayEquals(new long[]{8, 9}, SiteStaticResources.parseRange("bytes=8-100", 10));
        assertArrayEquals(new long[]{0, 9}, SiteStaticResources.parseRange("bytes=-100", 10));
        assertArrayEquals(new long[0], SiteStaticResources.parseRange("bytes=-0", 10));
        assertArrayEquals(new long[0], SiteStaticResources.parseRange("bytes=10-", 10));
        assertArrayEquals(new long[0], SiteStaticResources.parseRange("bytes=0-", 0));
        // what can't be served as a single range gives the whole file
        assertNull(SiteStaticResources.parseRange("bytes=5-2", 10));
        assertNull(SiteStaticResources.parseRange("bytes=0-1,3-4", 10));
        assertNull(SiteStaticResources.parseRange("bytes=-", 10));
        assertNull(SiteStaticResources.parseRange("bytes=a-b", 10));
        assertNull(SiteStaticResources.parseRange("items=0-1", 10));
        assertNull(SiteStaticResources.parseRange("bytes=99999999999999999999-", 10));
    }

    @Test
    void testEncodeUrl() {
        try (var runner = new SiteRunner().sessionUrlParameters(true).start(new Site() {
            public void setup() {
                get("/links", c -> {
                    var id = c.session().id();
                    var response = c.response();
                    c.print(String.join("|",
                        response.encodeURL("/a?x=1").equals("/a;jsessionid=" + id + "?x=1") ? "query" : "no query",
                        response.encodeURL("/a#top").equals("/a;jsessionid=" + id + "#top") ? "fragment" : "no fragment",
                        response.encodeURL("/a;jsessionid=old?x=1").equals("/a;jsessionid=" + id + "?x=1") ? "replaced" : "not replaced",
                        response.encodeURL("/a;jsessionid=old").equals("/a;jsessionid=" + id) ? "replaced at end" : "not replaced at end"));
                });
            }
        })) {
            assertEquals("query|fragment|replaced|replaced at end", runner.handle("/links").getText());
        }
    }

    @Test
    void testContextPathNormalisation() {
        var site = new Site() {
            public void setup() {
                get("/page", c -> c.print(c.request().getContextPath()));
            }
        };
        for (var context_path : List.of("ctx", "/ctx", "/ctx/", "ctx/")) {
            try (var runner = new SiteRunner().contextPath(context_path).start(site)) {
                assertEquals("/ctx", runner.handle("/ctx/page").getText(), context_path);
            }
        }
        for (var context_path : Arrays.asList("", "/", null)) {
            try (var runner = new SiteRunner().contextPath(context_path).start(site)) {
                assertEquals("", runner.handle("/page").getText(), String.valueOf(context_path));
            }
        }
    }

    @Test
    void testResponseLocale() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/locale", c -> c.response().setLocale(Locale.CANADA_FRENCH));
                get("/script", c -> c.response().setLocale(Locale.forLanguageTag("zh-Hant-TW")));
            }
        })) {
            var response = runner.handle("/locale");
            assertEquals("fr-CA", response.getHeader("Content-Language"));
            assertEquals(Locale.CANADA_FRENCH, response.getLocale());
            assertEquals("zh-Hant-TW", runner.handle("/script").getHeader("Content-Language"));
        }
    }

    @Test
    void testCookieJarAttributes() {
        var jar = new SiteCookieJar();
        try (var runner = new SiteRunner().serverName("app.example.com").cookieJar(jar).start(new Site() {
            public void setup() {
                get("/set", c -> {
                    var expires = new CookieBuilder("dated", "d").path("/").cookie();
                    expires.setAttribute("Expires", "Thu, 01 Jan 2099 00:00:00 GMT");
                    c.response().addCookie(expires);
                    var expired = new CookieBuilder("gone", "g").path("/").cookie();
                    expired.setAttribute("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
                    c.response().addCookie(expired);
                    c.addCookie(new CookieBuilder("dotted", "x").domain(".Example.com").path("/"));
                    var ancient = new CookieBuilder("dated", "old").path("/").cookie();
                    ancient.setAttribute("Expires", "Mon, 01 Jan 1900 00:00:00 GMT");
                    c.response().addCookie(ancient);
                    c.addCookie(new CookieBuilder("sid", "host").path("/"));
                    c.addCookie(new CookieBuilder("sid", "domain").domain("app.example.com").path("/"));
                });
                get("/keep", c -> {
                    var expires = new CookieBuilder("dated", "d").path("/").cookie();
                    expires.setAttribute("Expires", "Thu, 01 Jan 2099 00:00:00 GMT");
                    c.response().addCookie(expires);
                });
            }
        })) {
            runner.handle("/set");
            var cookies = new HashMap<String, SiteCookie>();
            var sids = 0;
            for (var cookie : jar.getCookies()) {
                cookies.put(cookie.name(), cookie);
                if (cookie.name().equals("sid")) {
                    sids++;
                }
            }
            // a date before 1970 removes the cookie, like any date in the past
            assertEquals(Set.of("dotted", "sid"), cookies.keySet());
            // a cookie for the domain of its own host replaces the host's cookie
            assertEquals(1, sids);
            assertEquals("domain", cookies.get("sid").value());

            runner.handle("/keep");
            cookies.clear();
            for (var cookie : jar.getCookies()) {
                cookies.put(cookie.name(), cookie);
            }
            assertEquals(4070908800000L, cookies.get("dated").expires());
            assertEquals("example.com", cookies.get("dotted").domain());
            assertTrue(cookies.get("dotted").isSessionCookie());
        }
    }

    @Test
    void testWebJarsWithoutStaticBase() {
        try (var runner = new SiteRunner().start(new Site())) {
            var response = runner.handle("/site-runner-webjar.txt");
            assertTrue(response.isHandled());
            assertEquals("from a web jar", response.getText());
        }
    }

    @Test
    void testForeignRequestBodyIsntRead() {
        try (var runner = new SiteRunner().contextPath("/ctx").maxFormContentSize(5).start(new Site() {
            public void setup() {
                post("/form", c -> c.print(c.parameter("a")));
            }
        })) {
            for (var url : List.of("https://elsewhere.com/ctx/form", "/other/form")) {
                var read = new AtomicBoolean(false);
                var closed = new AtomicBoolean(false);
                var body = new ByteArrayInputStream(("a=" + "x".repeat(100)).getBytes(StandardCharsets.UTF_8)) {
                    public synchronized int read(byte[] b, int off, int len) {
                        read.set(true);
                        return super.read(b, off, len);
                    }

                    public void close() {
                        closed.set(true);
                    }
                };
                var response = runner.handle(url, new SiteRequest(RequestMethod.POST)
                    .contentType("application/x-www-form-urlencoded")
                    .bodyAsStream(body));
                assertFalse(response.isHandled(), url);
                assertEquals(404, response.getStatus(), url);
                // the body is left to the application
                assertFalse(read.get(), url);
                assertFalse(closed.get(), url);
            }
        }
    }

    @Test
    void testFailingCookieStore() {
        var store = new SiteCookieStore() {
            public Collection<SiteCookie> load() {
                return List.of(new SiteCookie("remember", "yes", "localhost", null, "/", Long.MAX_VALUE, false, false, null),
                    new SiteCookie(null, "broken", "localhost", null, null, Long.MAX_VALUE, false, false, null));
            }

            public void save(Collection<SiteCookie> cookies) {
                throw new IllegalStateException("the disk is full");
            }
        };
        var jar = new SiteCookieJar(store);
        try (var runner = new SiteRunner().cookieJar(jar).start(new Site() {
            public void setup() {
                get("/remember", c -> c.print(c.cookieValue("remember")));
            }
        })) {
            // a restored cookie without a name or a path is left out
            assertEquals("yes", runner.handle("/remember").getText());
            jar.clear();
            assertTrue(jar.getCookies().isEmpty());
        }
    }

    @Test
    void testUnlimitedFormFromStream() {
        try (var runner = new SiteRunner().maxFormContentSize(Integer.MAX_VALUE).start(new Site() {
            public void setup() {
                post("/form", c -> c.print(c.parameter("a")));
            }
        })) {
            assertEquals("1", runner.handle("/form", new SiteRequest(RequestMethod.POST)
                .contentType("application/x-www-form-urlencoded")
                .bodyAsStream(new ByteArrayInputStream("a=1".getBytes(StandardCharsets.UTF_8)))).getText());
        }
    }

    @Test
    void testSessionIdAmongPathParameters() {
        try (var runner = new SiteRunner().sessionUrlParameters(true).start(new Site() {
            public void setup() {
                get("/start", c -> c.print(c.session().id()));
                get("/a/b", c -> c.print(c.session().isNew() + ":" + c.session().id()));
            }
        })) {
            var id = runner.handle("/start").getText();
            assertEquals("false:" + id, runner.handle("/a/b;jsessionid=" + id + ";x=1").getText());
            assertEquals("false:" + id, runner.handle("/a/b;x=1;jsessionid=" + id).getText());
            assertEquals("false:" + id, runner.handle("/a;jsessionid=" + id + "/b").getText());
            assertEquals("false:" + id, runner.handle("/a;jsessionid=" + id + "/b;jsessionid=").getText());
        }
    }

    @Test
    void testCookiePathWithPathParameters() {
        try (var runner = new SiteRunner().cookieJar(new SiteCookieJar()).start(new Site() {
            public void setup() {
                get("/area/login", c -> c.addCookie(new CookieBuilder("scoped", "s").path("/area/page")));
                get("/area/page", c -> c.print(c.cookieValue("scoped")));
            }
        })) {
            runner.handle("/area/login");
            assertEquals("s", runner.handle("/area/page;x=1").getText());
        }
    }

    @Test
    void testCookieFileStore(@TempDir Path directory)
    throws Exception {
        var file = directory.resolve("nested").resolve("cookies.txt").toFile();
        var store = new SiteCookieFileStore(file);
        assertTrue(store.load().isEmpty());

        var cookies = List.of(
            new SiteCookie("token", "a b=c\t;\u00e9", "app", null, "/", 4102444800000L, true, true, "Lax"),
            new SiteCookie("empty", "", "app", "app.example", "/sub", 4102444800000L, false, false, null));
        store.save(cookies);
        assertEquals(cookies, new ArrayList<>(new SiteCookieFileStore(file).load()));
        assertFalse(new File(file.getAbsolutePath() + ".tmp").exists());

        // a later save replaces the cookies, and a line that can't be read is left out
        store.save(List.of(cookies.get(1)));
        Files.writeString(file.toPath(), Files.readString(file.toPath()) + "broken\n");
        assertEquals(List.of(cookies.get(1)), new ArrayList<>(store.load()));
    }

    @Test
    void testCookieFileStoreAcrossRestarts(@TempDir Path directory) {
        var file = directory.resolve("cookies.txt").toFile();
        var site = new Site() {
            public void setup() {
                get("/remember", c -> c.addCookie(new CookieBuilder("city", "ghent").path("/").maxAge(3600)));
                get("/city", c -> c.print(c.cookieValue("city")));
            }
        };
        try (var runner = new SiteRunner().cookieJar(new SiteCookieJar(new SiteCookieFileStore(file))).start(site)) {
            runner.handle("/remember");
        }
        try (var runner = new SiteRunner().cookieJar(new SiteCookieJar(new SiteCookieFileStore(file))).start(site)) {
            assertEquals("ghent", runner.handle("/city").getText());
        }
    }

    @Test
    void testHeadBetweenGetAndAnyMethod() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/get-first", c -> c.setHeader("X-Route", "get"));
                route("/get-first", c -> c.setHeader("X-Route", "any"));
                route("/any-first", c -> c.setHeader("X-Route", "any"));
                get("/any-first", c -> c.setHeader("X-Route", "get"));
                get("/explicit", c -> c.setHeader("X-Route", "get"));
                route("/explicit", c -> c.setHeader("X-Route", "any"));
                head("/explicit", c -> c.setHeader("X-Route", "head"));
            }
        })) {
            // the order of registration decides, like it does for GET
            assertEquals("get", runner.handle("/get-first", new SiteRequest(RequestMethod.HEAD)).getHeader("X-Route"));
            assertEquals("any", runner.handle("/any-first", new SiteRequest(RequestMethod.HEAD)).getHeader("X-Route"));
            assertEquals("any", runner.handle("/get-first", new SiteRequest(RequestMethod.POST)).getHeader("X-Route"));
            assertEquals("head", runner.handle("/explicit", new SiteRequest(RequestMethod.HEAD)).getHeader("X-Route"));
        }
    }

    @Test
    void testContentTypeIsAHeader() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                post("/type", c -> c.print(c.header("Content-Type") + "|" + Collections.list(c.request().getHeaderNames()) + "|" + c.request().getContentType()));
            }
        })) {
            // like with a servlet container
            assertEquals("application/json|[Content-Type]|application/json",
                runner.handle("/type", new SiteRequest(RequestMethod.POST).contentType("application/json")).getText());
            assertEquals("text/plain|[Content-Type]|text/plain",
                runner.handle("/type", new SiteRequest(RequestMethod.POST).contentType("application/json").header("Content-Type", "text/plain")).getText());
        }
    }

    @Test
    void testSecureRunnerMarksFrameworkCookies() {
        for (var secure : new boolean[]{true, false}) {
            try (var runner = new SiteRunner().scheme("myapp").serverName("app").secure(secure).start(new rife.authentication.elements.MemoryAuthenticatedSite() {
                public void setup() {
                    super.setup();
                    get("/csrf", c -> {
                        c.ensureCsrfToken();
                        c.print("token");
                    });
                }
            })) {
                // a custom scheme isn't https, but the runner says whether it's secure
                var login = runner.handle("/login", form("login=guest&password=guestpass"));
                var auth = login.getNewCookies().stream().filter(cookie -> cookie.getName().equals("authId")).findFirst().orElseThrow();
                assertEquals(secure, auth.getSecure());
                var csrf = runner.handle("/csrf").getNewCookies().stream().filter(cookie -> cookie.getName().equals(RifeConfig.engine().getCsrfCookieName())).findFirst().orElseThrow();
                assertEquals(secure, csrf.getSecure());
            }
        }
    }

    @Test
    void testCookieJarReplacesCookieHeader() {
        var jar = new SiteCookieJar();
        try (var runner = new SiteRunner().cookieJar(jar).start(new Site() {
            public void setup() {
                get("/set", c -> c.addCookie(new CookieBuilder("token", "real").path("/")));
                get("/read", c -> c.print(c.cookieValue("token") + "|" + c.cookieValue("script")));
            }
        })) {
            runner.handle("/set");
            // what a script in the web view set never reaches the site
            assertEquals("real|null", runner.handle("/read", new SiteRequest().header("Cookie", "token=forged; script=1")).getText());
        }
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/read", c -> c.print(c.cookieValue("token")));
            }
        })) {
            assertEquals("passed", runner.handle("/read", new SiteRequest().header("Cookie", "token=passed")).getText());
        }
    }

    @Test
    void testCancelledRequestIsntAnError() {
        var exception_route_ran = new AtomicBoolean();
        var handler = new java.util.logging.Handler() {
            final List<String> messages = Collections.synchronizedList(new ArrayList<>());
            public void publish(java.util.logging.LogRecord record) {
                messages.add(record.getMessage());
            }
            public void flush() {
            }
            public void close() {
            }
        };
        var logger = java.util.logging.Logger.getLogger("rife.engine");
        logger.addHandler(handler);
        RifeConfig.engine().setLogEngineExceptions(true);
        try (var runner = new SiteRunner().scheme("myapp").serverName("app").start(new Site() {
            public void setup() {
                exception(c -> exception_route_ran.set(true));
                get("/download", c -> {
                    try {
                        var out = c.outputStream();
                        for (var i = 0; i < 100; i++) {
                            out.write("chunk".getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                get("/broken", c -> {
                    throw new IllegalStateException("broken");
                });
            }
        })) {
            var request = new SiteRequest();
            var recorder = new Recorder() {
                public void content(byte[] bytes, int offset, int length) {
                    super.content(bytes, offset, length);
                    request.cancel();
                }
            };
            runner.handle("/download", request, recorder);
            // the web view stopped loading, which isn't a failure of the site
            assertEquals("cancelled", recorder.last());
            assertFalse(exception_route_ran.get());
            assertTrue(handler.messages.isEmpty(), handler.messages.toString());

            runner.handle("/broken", new SiteRequest(), new Recorder());
            assertTrue(exception_route_ran.get());
            // without a port, there's none in the message
            assertTrue(handler.messages.get(0).startsWith("Error on host app\n"), handler.messages.get(0));
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void testEarlierUploadsAreRemoved(@TempDir Path directory)
    throws Exception {
        var earlier = directory.resolve("upl123.tmp").toFile();
        var current = directory.resolve("upl456.tmp").toFile();
        var other = directory.resolve("notes.txt").toFile();
        for (var file : List.of(earlier, current, other)) {
            Files.writeString(file.toPath(), "data");
        }
        assertTrue(earlier.setLastModified(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
        assertTrue(other.setLastModified(System.currentTimeMillis() - 24 * 60 * 60 * 1000));

        var path = RifeConfig.engine().getFileUploadPath();
        RifeConfig.engine().setFileUploadPath(directory.toString());
        try (var runner = new SiteRunner().start(new Site())) {
            // what an earlier run of the application left behind is removed
            assertFalse(earlier.exists());
            assertTrue(current.exists());
            assertTrue(other.exists());
        } finally {
            RifeConfig.engine().setFileUploadPath(path);
        }
    }

    @Test
    void testContextPathIsPublic() {
        assertEquals("/ctx", new SiteRunner().contextPath("ctx/").getContextPath());
        assertEquals("", new SiteRunner().getContextPath());
    }

    @Test
    void testSingleValueHttpHeaders() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/headers", c -> {
                    c.response().addHeader("X-Many", "one");
                    c.response().addHeader("X-Many", "two");
                    c.addCookie(new CookieBuilder("a", "1").path("/"));
                    c.addCookie(new CookieBuilder("b", "2").path("/"));
                    c.print("headers");
                });
            }
        })) {
            var response = runner.handle("/headers");
            assertEquals(2, response.getHttpHeaders().get("Set-Cookie").size());
            var headers = response.getSingleValueHttpHeaders();
            assertEquals("one, two", headers.get("x-many"));
            assertEquals("7", headers.get("Content-Length"));
            assertFalse(headers.containsKey("Set-Cookie"));
        }
    }

    @Test
    void testCancelledAfterCallUnderWay()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }
        })) {
            var in_content = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var recorder = new Recorder() {
                public void content(byte[] bytes, int offset, int length) {
                    super.content(bytes, offset, length);
                    if (new String(bytes, offset, length, StandardCharsets.UTF_8).contains("slow")) {
                        in_content.countDown();
                        try {
                            release.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        calls.add("content done");
                    }
                }
            };
            var request = new SiteRequest();
            runner.handle("/events", request, recorder);

            var sender = new Thread(() -> broadcaster.send("slow"));
            sender.start();
            assertTrue(in_content.await(5, TimeUnit.SECONDS));
            request.cancel();
            // the call that's under way has to be over first
            assertFalse(recorder.calls.contains("cancelled"));
            release.countDown();
            sender.join(5000);
            assertTrue(recorder.ended.await(5, TimeUnit.SECONDS));
            assertEquals("content done", recorder.calls.get(recorder.calls.size() - 2));
            assertEquals("cancelled", recorder.last());
            assertEquals(1, Collections.frequency(recorder.calls, "cancelled"));
        }
    }

    @Test
    void testNoCancelledAfterCompleted() {
        try (var runner = new SiteRunner().start(new Site() {
            public void setup() {
                get("/page", c -> c.print("page"));
            }
        })) {
            var request = new SiteRequest();
            var recorder = new Recorder();
            runner.handle("/page", request, recorder);
            request.cancel();
            assertEquals(List.of("committed", "content", "completed"), recorder.calls);
        }
    }

    @Test
    void testCancelledStaticDownload(@TempDir Path directory)
    throws Exception {
        Files.writeString(directory.resolve("data.txt"), "0123456789");
        try (var runner = new SiteRunner().staticResourceBase(directory.toString()).start(new Site())) {
            var request = new SiteRequest() {
                public String getHeader(String name) {
                    // the web view stops loading while the file is served
                    if ("Range".equalsIgnoreCase(name)) {
                        cancel();
                    }
                    return super.getHeader(name);
                }
            };
            var response = runner.handle("/data.txt", request);
            assertTrue(response.isFinished());
            assertEquals("", response.getText());
        }
    }

    @Test
    void testCancelledResponseKeepsNoCookies() {
        var jar = new SiteCookieJar();
        try (var runner = new SiteRunner().cookieJar(jar).start(new Site() {
            public void setup() {
                get("/set", c -> {
                    c.addCookie(new CookieBuilder("token", "t").path("/"));
                    c.print("set");
                });
            }
        })) {
            var request = new SiteRequest();
            var recorder = new Recorder() {
                public void committed(SiteResponse response) {
                    super.committed(response);
                    request.cancel();
                }
            };
            runner.handle("/set", request, recorder);
            // the web view never saw the headers with the cookie
            assertTrue(jar.getCookies().isEmpty());
        }
    }

    @Test
    void testTextBodyUsesLaterEncoding() {
        var request = new SiteRequest(RequestMethod.POST).body("été").characterEncoding("UTF-16");
        assertArrayEquals("été".getBytes(StandardCharsets.UTF_16), request.getBodyAsBytes());
        assertEquals("été", request.getBody());
        request.characterEncoding("ISO-8859-1");
        assertArrayEquals("été".getBytes(StandardCharsets.ISO_8859_1), request.getBodyAsBytes());

        var bytes = new SiteRequest(RequestMethod.POST).bodyAsBytes("été".getBytes(StandardCharsets.UTF_8)).characterEncoding("UTF-8");
        assertEquals("été", bytes.getBody());
        bytes.characterEncoding("ISO-8859-1");
        assertEquals(new String("été".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), bytes.getBody());
    }

    @Test
    void testCancelledBeforeHandling() {
        try (var runner = new SiteRunner().contextPath("/ctx").maxFormContentSize(5).start(new Site() {
            public void setup() {
                post("/form", c -> fail("a cancelled request isn't processed"));
                get("/page", c -> fail("a cancelled request isn't processed"));
            }
        })) {
            for (var url : List.of("https://elsewhere.com/ctx/form", "/other/form", "/ctx/form")) {
                var request = new SiteRequest(RequestMethod.POST)
                    .contentType("application/x-www-form-urlencoded")
                    .body("a=" + "x".repeat(100));
                request.cancel();
                var recorder = new Recorder();
                var response = runner.handle(url, request, recorder);
                // the web view stopped the request, so only its ending reaches it
                assertEquals(List.of("cancelled"), recorder.calls, url);
                assertTrue(response.isFinished(), url);
            }

            var request = new SiteRequest();
            request.cancel();
            var recorder = new Recorder();
            assertTrue(runner.handle("/ctx/page", request, recorder).isFinished());
            assertEquals(List.of("cancelled"), recorder.calls);
        }
    }
}
