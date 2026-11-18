/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.nativeapp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rife.config.RifeConfig;
import rife.engine.Site;
import rife.engine.SseBroadcaster;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TestNativeApp {
    // a site of an app makes the JVM headless, which the other tests of the suite don't expect
    private static String headless_;

    @BeforeAll
    static void keepHeadless() {
        headless_ = System.getProperty("java.awt.headless");
    }

    @AfterAll
    static void restoreHeadless() {
        if (null == headless_) {
            System.clearProperty("java.awt.headless");
        } else {
            System.setProperty("java.awt.headless", headless_);
        }
    }

    private static Recorder ask(NativeApp app, String url) {
        return ask(app, url, "GET", "", null);
    }

    private static final java.util.concurrent.atomic.AtomicLong NUMBERS = new java.util.concurrent.atomic.AtomicLong(1000);

    private static Recorder ask(NativeApp app, String url, String method, String headers, byte[] body) {
        var recorder = new Recorder();
        app.handle(NUMBERS.incrementAndGet(), url, method, headers, body, recorder);
        return recorder;
    }

    @Test
    void testGet() {
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/hello", c -> c.print("hello " + c.parameter("name")));
            }
        });
        var answer = ask(app, "/hello?name=w%C3%B6rld");
        assertEquals(List.of("committed", "content", "completed"), answer.calls);
        assertEquals(200, answer.status);
        assertEquals("hello wörld", answer.text());
        assertTrue(answer.headers.contains("Content-Type: text/html; charset=UTF-8\n"), answer.headers);

        var origin = ask(app, "app://site/hello?name=x");
        assertEquals("hello x", origin.text());
        app.stop();
    }

    @Test
    void testPostAndHeaders() {
        var app = new NativeApp(new Site() {
            public void setup() {
                post("/form", c -> {
                    c.setHeader("X-Said", c.header("X-One") + "|" + c.header("X-Two"));
                    c.print(c.method() + ":" + c.parameter("a"));
                });
            }
        });
        var answer = ask(app, "/form", "POST", "Content-Type: application/x-www-form-urlencoded\nX-One:  1 \nnot a header\nX-Two: 2\n",
            "a=%C3%A9t%C3%A9".getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("POST:été", answer.text());
        assertTrue(answer.headers.contains("X-Said: 1|2\n"), answer.headers);
        app.stop();
    }

    @Test
    void testOrigin() {
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/root", c -> c.print(c.urlFor(c.route())));
            }
        }).property(NativeApp.SCHEME_PROPERTY, "https").property(NativeApp.HOST_PROPERTY, "app.example.invalid");
        assertEquals("https://app.example.invalid/root", ask(app, "/root").text());
        assertEquals("https://app.example.invalid/root", ask(app, "https://app.example.invalid/root").text());
        app.stop();

        var defaults = new NativeApp(new Site() {
            public void setup() {
                get("/root", c -> c.print(c.urlFor(c.route())));
            }
        });
        assertEquals("app://site/root", ask(defaults, "/root").text());
        defaults.stop();
    }

    @Test
    void testPropertiesReachTheSite() {
        var setup = new String[1];
        var app = new NativeApp(new Site() {
            public void setup() {
                setup[0] = properties().getValueString("my.setting");
                get("/said", c -> c.print(c.property("my.setting") + "|" + c.property(NativeApp.VERSION_PROPERTY)));
            }
        }).property("my.setting", "set").property(NativeApp.VERSION_PROPERTY, "2.0");
        assertEquals("set|2.0", ask(app, "/said").text());
        assertEquals("set", setup[0]);
        assertThrows(IllegalStateException.class, () -> app.property("late", "1"));
        app.stop();
    }

    @Test
    void testNothingIsToldAfterTheEnd()
    throws Exception {
        var kept = new java.io.OutputStream[1];
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/kept", c -> {
                    c.print("early");
                    kept[0] = c.outputStream();
                });
            }
        });
        var answer = ask(app, "/kept");
        assertEquals(List.of("committed", "content", "completed"), answer.calls);
        // a thread of the site that goes on writing after the request ended
        kept[0].write(new byte[40000]);
        kept[0].flush();
        assertEquals(List.of("committed", "content", "completed"), answer.calls);
        assertEquals("early", answer.text());
        app.stop();
    }

    @Test
    void testTheEndWaitsForContentBeingTold()
    throws Exception {
        var writing = new java.util.concurrent.CountDownLatch(1);
        var writer = new Thread[1];
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/racing", c -> {
                    var stream = c.outputStream();
                    stream.write("early".getBytes());
                    stream.flush();
                    // a thread of the site that's telling content while the request ends, which the end waits for
                    writer[0] = new Thread(() -> {
                        try {
                            stream.write(new byte[10]);
                            stream.flush();
                        } catch (Exception ignored) {
                        }
                    });
                    writer[0].start();
                    writing.await();
                });
            }
        });
        var order = java.util.Collections.synchronizedList(new java.util.ArrayList<String>());
        app.handle(NUMBERS.incrementAndGet(), "/racing", "GET", "", null, new Recorder() {
            public void content(byte[] bytes, int offset, int length) {
                order.add("content " + length);
                if (length == 10) {
                    writing.countDown();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {
                    }
                    order.add("told");
                }
            }

            public synchronized void completed() {
                order.add("completed");
            }

            public synchronized void failed(Throwable exception) {
                order.add("failed " + exception);
            }
        });
        writer[0].join();
        assertEquals("completed", order.get(order.size() - 1), order.toString());
        app.stop();
    }

    @Test
    void testSystemPropertiesComeFirst() {
        System.setProperty("rife.nativeapp.fromSystem", "yes");
        try {
            var app = new NativeApp(new Site() {
                public void setup() {
                    get("/told", c -> c.print(c.properties().getValueString("rife.nativeapp.fromSystem") + " " + c.properties().getValueString("rife.nativeapp.saved")));
                }
            });
            assertEquals("yes null", ask(app, "/told").text());
            app.stop();
        } finally {
            System.clearProperty("rife.nativeapp.fromSystem");
        }
    }

    @Test
    void testHeaderValuesStayOnOneLine() {
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/split", c -> {
                    c.setHeader("X-Said", "one\r\nX-Injected: two");
                    c.setHeader("X-Zero", "before\u0000after");
                });
            }
        });
        var answer = ask(app, "/split");
        assertEquals(200, answer.status);
        assertTrue(answer.headers.contains("X-Said: one X-Injected: two\n"), answer.headers);
        assertFalse(answer.headers.contains("\nX-Injected"), answer.headers);
        assertTrue(answer.headers.contains("X-Zero: before after\n"), answer.headers);
        app.stop();
    }

    @Test
    void testScriptInAnApp() {
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/page", c -> c.print(c.template("native_app")));
            }
        }).property(NativeApp.PLATFORM_PROPERTY, "ios");
        assertEquals("<html lang=\"en\"><head><script src=\"app://site/rife2-native-app.js\" data-platform=\"ios\"></script></head></html>\n", ask(app, "/page").text());

        var script = ask(app, "/rife2-native-app.js");
        assertEquals(200, script.status);
        assertTrue(script.headers.contains("Content-Type: text/javascript"), script.headers);
        assertTrue(script.text().contains("window.rife2NativeApp = {platform: platform};"));
        app.stop();

        var browser = new NativeApp(new Site() {
            public void setup() {
                get("/page", c -> c.print(c.template("native_app")));
            }
        });
        assertEquals("<html lang=\"en\"><head></head></html>\n", ask(browser, "/page").text());
        browser.stop();
    }

    @Test
    void testCookiesLast(@TempDir Path data) {
        var site = new Site() {
            public void setup() {
                get("/set", c -> c.addCookie(new rife.engine.CookieBuilder("city", "Brussels").path("/").maxAge(3600)));
                get("/get", c -> c.print(c.cookieValue("city", "none")));
            }
        };
        var first = new NativeApp(site).property(NativeApp.DATA_PROPERTY, data.toString());
        ask(first, "/set");
        assertEquals("Brussels", ask(first, "/get").text());
        first.stop();

        var again = new NativeApp(site).property(NativeApp.DATA_PROPERTY, data.toString());
        assertEquals("Brussels", ask(again, "/get").text());
        again.stop();

        var forgetful = new NativeApp(site);
        ask(forgetful, "/set");
        assertEquals("Brussels", ask(forgetful, "/get").text());
        forgetful.stop();
        var fresh = new NativeApp(site);
        assertEquals("none", ask(fresh, "/get").text());
        fresh.stop();
    }

    @Test
    void testNamedSite() {
        var app = new NativeApp().property(NativeApp.DATA_PROPERTY, "/somewhere");
        assertEquals("hello from /somewhere", ask(app, "/hello").text());
        app.stop();
    }

    @Test
    void testSiteThatDoesntStart() {
        var setups = new AtomicInteger();
        var app = new NativeApp(new Site() {
            public void setup() {
                setups.incrementAndGet();
                throw new IllegalStateException("broken");
            }
        });
        // as under a container, the site answers with its error page
        for (var i = 0; i < 2; i++) {
            var answer = ask(app, "/anything");
            assertEquals(List.of("committed", "content", "completed"), answer.calls);
            assertEquals(500, answer.status);
        }
        assertEquals(1, setups.get());
    }

    @Test
    void testSiteProvider(@TempDir Path services) throws Exception {
        // a provider is used before a site that's named, and gets the properties
        var named = services.resolve("META-INF/services");
        java.nio.file.Files.createDirectories(named);
        java.nio.file.Files.writeString(named.resolve("rife.nativeapp.SiteProvider"), "rife.nativeapp.ProvidedSite\n");
        var thread = Thread.currentThread();
        var loader = thread.getContextClassLoader();
        thread.setContextClassLoader(new java.net.URLClassLoader(new java.net.URL[]{services.toUri().toURL()}, loader));
        try {
            var app = new NativeApp().property(NativeApp.DATA_PROPERTY, "/kept");
            assertEquals("provided with /kept", ask(app, "/hello").text());
            app.stop();
        } finally {
            thread.setContextClassLoader(loader);
        }
    }

    @Test
    void testNoNamedSite() {
        var thread = Thread.currentThread();
        var loader = thread.getContextClassLoader();
        // a class loader that has no META-INF/services/rife.engine.Site
        thread.setContextClassLoader(new java.net.URLClassLoader(new java.net.URL[0], null));
        try {
            var app = new NativeApp();
            var first = ask(app, "/hello");
            assertEquals(List.of("failed"), first.calls);
            assertEquals("No site is named in META-INF/services/rife.engine.Site or rife.nativeapp.SiteProvider", first.failure.getMessage());
            // and isn't looked for again
            assertSame(first.failure, ask(app, "/hello").failure);
        } finally {
            thread.setContextClassLoader(loader);
        }
    }

    @Test
    void testUnknownMethod() {
        var app = new NativeApp(new Site() {
            public void setup() {
            }
        });
        var answer = ask(app, "/", "BREW", "", null);
        assertEquals(List.of("failed"), answer.calls);
        app.stop();
    }

    @Test
    void testCancel() {
        var broadcaster = new SseBroadcaster();
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }
        });
        var answer = new Recorder();
        app.handle(7, "/events", "GET", "", null, answer);
        assertEquals(1, broadcaster.connectionCount());
        assertEquals(List.of("committed"), answer.calls.subList(0, 1));
        broadcaster.send("tick");
        assertTrue(answer.text().contains("data: tick"), answer.text());

        app.cancel(7);
        app.cancel(7);
        assertEquals(0, broadcaster.connectionCount());
        assertEquals("cancelled", answer.calls.get(answer.calls.size() - 1));
        assertEquals(1, answer.calls.stream().filter("cancelled"::equals).count());
        app.stop();
    }

    @Test
    void testEndOfAnotherRequestKeepsAStream() {
        var broadcaster = new SseBroadcaster();
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/quick", c -> c.print("quick"));
                get("/events", c -> c.sse(broadcaster));
            }
        });
        var stream = new Recorder();
        // a stream that takes the number of a request that's ending
        app.handle(5, "/quick", "GET", "", null, new Recorder() {
            public synchronized void completed() {
                super.completed();
                app.handle(5, "/events", "GET", "", null, stream);
            }
        });
        assertEquals(1, broadcaster.connectionCount());
        app.cancel(5);
        assertEquals(0, broadcaster.connectionCount());
        assertEquals("cancelled", stream.calls.get(stream.calls.size() - 1));
        app.stop();
    }

    @Test
    void testCancelBeforeTheRequest() {
        var broadcaster = new SseBroadcaster();
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }
        });
        // the thread that cancels can get there first
        app.cancel(9);
        var answer = new Recorder();
        app.handle(9, "/events", "GET", "", null, answer);
        assertEquals(0, broadcaster.connectionCount());
        assertEquals("cancelled", answer.calls.get(answer.calls.size() - 1));
        app.stop();
    }

    @Test
    void testCancelWhileTheRequestStarts()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/events", c -> c.sse(broadcaster));
            }
        });
        var cancelling = java.util.concurrent.Executors.newSingleThreadExecutor();
        var ended = new java.util.concurrent.atomic.AtomicInteger();
        var requests = 5000;
        try {
            for (var id = 1; id <= requests; id++) {
                final long number = id;
                var go = new java.util.concurrent.atomic.AtomicBoolean();
                var cancel = cancelling.submit(() -> {
                    while (!go.get()) {
                        Thread.onSpinWait();
                    }
                    app.cancel(number);
                });
                go.set(true);
                app.handle(id, "/events", "GET", "", null, new Recorder() {
                    public synchronized void completed() {
                        ended.incrementAndGet();
                    }

                    public synchronized void failed(Throwable exception) {
                        ended.incrementAndGet();
                    }

                    public synchronized void cancelled() {
                        ended.incrementAndGet();
                    }
                });
                cancel.get();
            }
            assertEquals(requests, ended.get(), "requests that were cancelled and never ended");
        } finally {
            cancelling.shutdownNow();
            app.stop();
        }
    }

    @Test
    void testWholeAnswers()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        var writing = new java.util.concurrent.atomic.AtomicReference<Thread>();
        var app = new NativeApp(new Site() {
            public void setup() {
                get("/events", c -> {
                    c.sse(broadcaster);
                    // another thread writes to the stream before the request returns
                    var other = new Thread(() -> broadcaster.send("from elsewhere"));
                    writing.set(other);
                    other.start();
                    other.join();
                });
            }
        }).property(NativeApp.WHOLE_RESPONSES_PROPERTY, "true");
        var me = Thread.currentThread();
        var threads = java.util.Collections.synchronizedSet(new java.util.HashSet<Thread>());
        var answer = new Recorder() {
            public synchronized void content(byte[] bytes, int offset, int length) {
                threads.add(Thread.currentThread());
                super.content(bytes, offset, length);
            }

            public synchronized void cancelled() {
                threads.add(Thread.currentThread());
                super.cancelled();
            }
        };
        app.handle(1, "/events", "GET", "", null, answer);
        assertEquals(java.util.Set.of(me), threads, "only the thread that asked is told");
        assertFalse(answer.text().contains("from elsewhere"));
        assertEquals("cancelled", answer.calls.get(answer.calls.size() - 1));
        assertEquals(1, answer.calls.stream().filter("cancelled"::equals).count());
        assertEquals(0, broadcaster.connectionCount());
        // and nothing after it returned
        broadcaster.send("later");
        assertFalse(answer.text().contains("later"));
        app.stop();
    }

    @Test
    void testSiteThatCantBeMade(@TempDir Path services) throws Exception {
        var named = services.resolve("META-INF/services");
        java.nio.file.Files.createDirectories(named);
        java.nio.file.Files.writeString(named.resolve("rife.nativeapp.SiteProvider"), "rife.nativeapp.Missing\n");
        var thread = Thread.currentThread();
        var loader = thread.getContextClassLoader();
        thread.setContextClassLoader(new java.net.URLClassLoader(new java.net.URL[]{services.toUri().toURL()}, loader));
        try {
            var app = new NativeApp();
            var first = ask(app, "/hello");
            assertEquals(List.of("failed"), first.calls);
            assertInstanceOf(java.util.ServiceConfigurationError.class, first.failure.getCause());
            // and isn't looked for again
            assertSame(first.failure, ask(app, "/hello").failure);
            assertThrows(IllegalStateException.class, () -> app.property("late", "1"));
        } finally {
            thread.setContextClassLoader(loader);
        }
    }

    @Test
    void testTemp(@TempDir Path temp) {
        var previous = RifeConfig.global().getTempPath();
        try {
            var app = new NativeApp(new Site() {
                public void setup() {
                    get("/temp", c -> c.print(RifeConfig.global().getTempPath()));
                }
            }).property(NativeApp.TEMP_PROPERTY, temp.toString());
            assertEquals(temp.toString(), ask(app, "/temp").text());
            app.stop();
        } finally {
            RifeConfig.global().setTempPath(previous);
        }
    }

    @Test
    void testHeadless() {
        var previous = System.getProperty("java.awt.headless");
        try {
            System.clearProperty("java.awt.headless");
            var app = new NativeApp(new Site() {
                public void setup() {
                    // the JVM decides whether it's headless once, which another test may have made it do already
                    get("/headless", c -> c.print(System.getProperty("java.awt.headless")));
                }
            });
            assertEquals("true", ask(app, "/headless").text());
            app.stop();

            // what an app said first stays
            System.setProperty("java.awt.headless", "false");
            var told = new NativeApp(new Site() {
                public void setup() {
                }
            });
            told.runner();
            assertEquals("false", System.getProperty("java.awt.headless"));
            told.stop();
        } finally {
            if (null == previous) {
                System.clearProperty("java.awt.headless");
            } else {
                System.setProperty("java.awt.headless", previous);
            }
        }
    }

    @Test
    void testMain() {
        var out = System.out;
        var said = new ByteArrayOutputStream();
        System.setOut(new PrintStream(said, true, StandardCharsets.UTF_8));
        try {
            NativeApp.main(new String[]{"-Drife.nativeapp.data=/given", "/hello", "/missing"});
        } finally {
            System.setOut(out);
        }
        var lines = said.toString(StandardCharsets.UTF_8).split("\n");
        // hello from /given
        assertTrue(lines[0].startsWith("/hello -> 200 text/html; charset=UTF-8 completed, 17 bytes with checksum "), lines[0]);
        assertTrue(lines[1].startsWith("/missing -> 404 "), lines[1]);
        assertTrue(lines[1].contains(" unhandled completed"), lines[1]);
    }

    @Test
    void testMainSavesTheAnswers(@TempDir Path saved)
    throws Exception {
        var out = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        // a directory that isn't there yet
        saved = saved.resolve("answers");
        System.setProperty("rife.nativeapp.saved", saved.toString());
        try {
            NativeApp.main(new String[]{"/", "/hello"});
        } finally {
            System.clearProperty("rife.nativeapp.saved");
            System.setOut(out);
        }
        assertTrue(java.nio.file.Files.isRegularFile(saved.resolve("index")));
        assertTrue(java.nio.file.Files.readString(saved.resolve("hello")).startsWith("hello"));
    }
}
