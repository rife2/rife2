/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import rife.config.RifeConfig;
import rife.test.MockConversation;
import rife.test.MockRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestSse {
    @Test
    void testMockEventStream() {
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/events", c -> {
                    try (var sse = c.sse()) {
                        assertTrue(sse.isOpen());
                        assertEquals("13", sse.lastEventId());
                        sse.send(new ServerSentEvent().name("greeting").id("1").retry(5000).data("hello\nworld"));
                        sse.comment("keep-alive");
                        sse.send("plain");
                    }
                });
            }
        });

        var response = m.doRequest("/events", new MockRequest().header("Last-Event-ID", "13"));
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().startsWith("text/event-stream"));

        assertEquals("""
            event: greeting
            id: 1
            retry: 5000
            data: hello
            data: world

            : keep-alive

            data: plain

            """, response.getText());

        var events = response.getEvents();
        assertEquals(3, events.size());

        assertEquals("greeting", events.get(0).getName());
        assertEquals("1", events.get(0).getId());
        assertEquals(5000, events.get(0).getRetry());
        assertEquals("hello\nworld", events.get(0).getData());
        assertTrue(events.get(0).getComments().isEmpty());

        assertNull(events.get(1).getName());
        assertNull(events.get(1).getData());
        assertEquals(List.of("keep-alive"), events.get(1).getComments());

        assertNull(events.get(2).getName());
        assertNull(events.get(2).getId());
        assertEquals(-1, events.get(2).getRetry());
        assertEquals("plain", events.get(2).getData());
    }

    static class FragmentSite extends Site {
        Route events = get("/events", c -> {
            var t = c.template("sse_fragment");
            t.setValue("count", 42);
            c.sse().send(new ServerSentEvent().name("count").template(t));
        });
    }

    @Test
    void testMockEventStreamTemplate() {
        var m = new MockConversation(new FragmentSite());

        var events = m.doRequest("/events").getEvents();
        assertEquals(1, events.size());
        assertEquals("count", events.get(0).getName());
        assertEquals("<div>count 42 <a href=\"http://localhost/events\">stream</a></div>", events.get(0).getData());
    }

    static class BlockSite extends Site {
        Route events = get("/events", c -> {
            var t = c.template("sse_blocks");
            var sse = c.sse();

            t.setValue("symbol", "ACME");
            t.setValue("price", "42.10");
            sse.send(new ServerSentEvent().name("price").templateBlock(t, "price_row"));

            t.setValue("price", "42.35");
            sse.send(t, "price_row");
        });
    }

    @Test
    void testMockTemplateBlock() {
        var m = new MockConversation(new BlockSite());

        var events = m.doRequest("/events").getEvents();
        assertEquals(2, events.size());
        assertEquals("price", events.get(0).getName());
        assertEquals("<p>ACME: 42.10 <a href=\"http://localhost/events\">watch</a></p>", events.get(0).getData());
        assertNull(events.get(1).getName());
        assertEquals("<p>ACME: 42.35 <a href=\"http://localhost/events\">watch</a></p>", events.get(1).getData());
    }

    @Test
    void testMockTemplateCapture() {
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/events", c -> {
                    var sse = c.sse();
                    sse.send("plain data");
                    sse.comment("keep-alive");

                    var t = c.template("sse_blocks");
                    t.setValue("symbol", "ACME");
                    t.setValue("price", "42.10");
                    sse.send(t, "price_row");

                    // the captured template snapshots the values at send time
                    t.setValue("price", "99.99");
                    sse.send(t, "price_row");
                });
            }
        });

        var events = m.doRequest("/events").getEvents();
        assertEquals(4, events.size());
        assertNull(events.get(0).getTemplate());
        assertNull(events.get(1).getTemplate());
        assertEquals("ACME", events.get(2).getTemplate().getValue("symbol"));
        assertEquals("42.10", events.get(2).getTemplate().getValue("price"));
        assertEquals("99.99", events.get(3).getTemplate().getValue("price"));
    }

    @Test
    void testMockUnknownBlock() {
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/events", c -> {
                    var sse = c.sse();
                    assertThrows(rife.template.exceptions.BlockUnknownException.class,
                        () -> sse.send(c.template("sse_blocks"), "unknown_block"));
                    sse.send("still open");
                });
            }
        });

        var events = m.doRequest("/events").getEvents();
        assertEquals(1, events.size());
        assertEquals("still open", events.get(0).getData());
    }

    @Test
    void testMockDetachedBroadcast() {
        var broadcaster = new SseBroadcaster();
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/stream", c -> c.sse(broadcaster));
            }
        });

        var response = m.doRequest("/stream");
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().startsWith("text/event-stream"));
        assertEquals(1, broadcaster.connectionCount());

        assertEquals(1, broadcaster.send(new ServerSentEvent().name("tick").data("42")));
        assertEquals(1, broadcaster.comment("bye"));

        var events = response.getEvents();
        assertEquals(2, events.size());
        assertEquals("tick", events.get(0).getName());
        assertEquals("42", events.get(0).getData());
        assertEquals(List.of("bye"), events.get(1).getComments());

        broadcaster.close();
        assertEquals(0, broadcaster.connectionCount());
        assertEquals(0, broadcaster.send("after close"));
    }

    static class DetachedBlockSite extends Site {
        final SseBroadcaster broadcaster = new SseBroadcaster();
        Route events = get("/events", c -> c.sse(broadcaster));
    }

    @Test
    void testBroadcastTemplatePerConnectionContext() {
        var site = new DetachedBlockSite();
        var m = new MockConversation(site);

        var alice = m.doRequest("/events?user=alice");
        var bob = m.doRequest("/events?user=bob");

        // a single broadcast template resolves its filtered tags against
        // the context of each receiving connection
        var t = rife.template.TemplateFactory.HTML.get("sse_param");
        assertEquals(2, site.broadcaster.send(t));

        assertEquals("<p>hello alice</p>", alice.getEvents().get(0).getData());
        assertEquals("<p>hello bob</p>", bob.getEvents().get(0).getData());

        // the captured templates hold each connection's own values
        assertEquals("alice", alice.getEvents().get(0).getTemplate().getValue("param:user"));
        assertEquals("bob", bob.getEvents().get(0).getTemplate().getValue("param:user"));

        site.broadcaster.close();
    }

    static class BrokenResponse extends AbstractResponse {
        BrokenResponse(Request request) {
            super(request);
        }

        protected void _setContentType(String contentType) {
        }

        protected java.io.OutputStream _getOutputStream()
        throws java.io.IOException {
            throw new java.io.IOException("broken pipe");
        }

        public java.io.PrintWriter getWriter() {
            return null;
        }

        public void setLocale(java.util.Locale locale) {
        }

        public java.util.Locale getLocale() {
            return null;
        }

        public String getCharacterEncoding() {
            return "UTF-8";
        }

        public void setContentLength(int length) {
        }

        public void addCookie(jakarta.servlet.http.Cookie cookie) {
        }

        public void addHeader(String name, String value) {
        }

        public void addDateHeader(String name, long date) {
        }

        public void addIntHeader(String name, int integer) {
        }

        public boolean containsHeader(String name) {
            return false;
        }

        public void sendError(int statusCode) {
        }

        public void sendError(int statusCode, String message) {
        }

        public void sendRedirect(String location) {
        }

        public void setDateHeader(String name, long date) {
        }

        public void setHeader(String name, String value) {
        }

        public void setIntHeader(String name, int value) {
        }

        public void setStatus(int statusCode) {
        }

        public String encodeURL(String url) {
            return url;
        }

        public jakarta.servlet.http.HttpServletResponse getHttpServletResponse() {
            return null;
        }
    }

    @Test
    void testFailedInitialFlushNotRegistered() {
        var broadcaster = new SseBroadcaster();
        var request = new MockRequest();
        var context = new Context("", new Site() {
            public void setup() {
            }
        }, request, new BrokenResponse(request), null);

        // the initial header flush fails, the connection is closed and
        // isn't registered with the broadcaster
        var connection = context.sse(broadcaster);
        assertFalse(connection.isOpen());
        assertEquals(0, broadcaster.connectionCount());
        assertEquals(0, broadcaster.send("nobody listens"));
    }

    @Test
    void testExceptionAfterDetachClosesConnection() {
        var broadcaster = new SseBroadcaster();
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/stream", c -> {
                    c.sse(broadcaster);
                    throw new RuntimeException("boom after detach");
                });
            }
        });

        var response = m.doRequest("/stream");

        // the connection is closed and deregistered, and no error page is
        // written into the committed event stream
        assertEquals(0, broadcaster.connectionCount());
        assertEquals("", response.getText());
    }

    static class HolderSite extends Site {
        final SseBroadcaster broadcaster = new SseBroadcaster();
        final SseConnection[] holder = new SseConnection[1];
        Route events = get("/events", c -> holder[0] = c.sse(broadcaster));
    }

    // broadcaster-generated IDs are composed of an epoch and a sequence
    // number, these helpers extract the sequence and construct a cursor
    // that belongs to the broadcaster's epoch
    private static long seq(String id) {
        return Long.parseLong(id.substring(id.indexOf('-') + 1));
    }

    private static String cursor(SseBroadcaster broadcaster, long sequence) {
        var last = broadcaster.lastEventId();
        return last.substring(0, last.indexOf('-') + 1) + sequence;
    }

    @Test
    void testElementModeConnectionClosedAfterElementReturns() {
        var holder = new SseConnection[1];
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/events", c -> {
                    var sse = c.sse();
                    holder[0] = sse;
                    sse.send("only event");
                    assertTrue(sse.isOpen());
                });
            }
        });

        var response = m.doRequest("/events");
        assertEquals(1, response.getEvents().size());
        assertFalse(holder[0].isOpen());
    }

    @Test
    void testElementModeErrorAndRedirectCloseStream() {
        var holder = new SseConnection[1];
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/failing", c -> {
                    var sse = c.sse();
                    holder[0] = sse;
                    sse.send("before failure");
                    throw new RuntimeException("element failure");
                });
                Route target = get("/elsewhere", c -> c.print("elsewhere"));
                get("/redirecting", c -> {
                    var sse = c.sse();
                    holder[0] = sse;
                    sse.send("before redirect");
                    c.redirect(target);
                });
            }
        });

        // the error report isn't rendered into the committed event stream
        var failed = m.doRequest("/failing");
        var failed_events = failed.getEvents();
        assertEquals(1, failed_events.size());
        assertEquals("before failure", failed_events.get(0).getData());
        assertFalse(failed.getText().contains("element failure"));
        assertFalse(holder[0].isOpen());

        // a redirect can't be honored on a committed event stream either
        var redirected = m.doRequest("/redirecting");
        var redirected_events = redirected.getEvents();
        assertEquals(1, redirected_events.size());
        assertEquals("before redirect", redirected_events.get(0).getData());
        assertFalse(holder[0].isOpen());
    }

    @Test
    void testRedirectAfterDetachClosesConnection() {
        var broadcaster = new SseBroadcaster();
        var m = new MockConversation(new Site() {
            public void setup() {
                Route target = get("/elsewhere", c -> c.print("elsewhere"));
                get("/stream", c -> {
                    c.sse(broadcaster);
                    c.redirect(target);
                });
            }
        });

        var response = m.doRequest("/stream");

        // a committed event stream can't be redirected, the connection is
        // closed and deregistered instead
        assertEquals(0, broadcaster.connectionCount());
        assertEquals("", response.getText());
        assertNull(response.getHeader("Location"));
    }

    @Test
    void testDeferAfterDetachClosesConnection() {
        var broadcaster = new SseBroadcaster();
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/stream", c -> {
                    c.sse(broadcaster);
                    c.defer();
                });
            }
        });

        var response = m.doRequest("/stream");

        // a committed event stream can't be deferred to other filters, the
        // connection is closed and deregistered instead
        assertEquals(0, broadcaster.connectionCount());
        assertEquals("", response.getText());
    }

    @Test
    void testCloseStopsSending() {
        var site = new HolderSite();
        var m = new MockConversation(site);

        var response = m.doRequest("/events");
        var connection = site.holder[0];
        assertEquals(1, site.broadcaster.connectionCount());

        assertEquals(1, site.broadcaster.send("before"));

        connection.close();
        assertFalse(connection.isOpen());
        // closing deregisters the connection from its broadcaster
        assertEquals(0, site.broadcaster.connectionCount());
        assertFalse(connection.send(new ServerSentEvent().data("after")));

        var events = response.getEvents();
        assertEquals(1, events.size());
        assertEquals("before", events.get(0).getData());
    }

    static class WriteFailingResponse extends BrokenResponse {
        WriteFailingResponse(Request request) {
            super(request);
        }

        protected java.io.OutputStream _getOutputStream() {
            return new java.io.OutputStream() {
                public void write(int b)
                throws java.io.IOException {
                    throw new java.io.IOException("write failed");
                }
            };
        }
    }

    @Test
    void testFailedReplayNotRegistered() {
        var broadcaster = new SseBroadcaster().history(5);
        broadcaster.send("buffered before anybody connects");

        // the initial flush succeeds, but replaying the history fails
        var request = new MockRequest().header("Last-Event-ID", cursor(broadcaster, 0));
        var context = new Context("", new Site() {
            public void setup() {
            }
        }, request, new WriteFailingResponse(request), null);

        var connection = context.sse(broadcaster);
        assertFalse(connection.isOpen());
        assertEquals(0, broadcaster.connectionCount());
        // an interrupted replay isn't counted as a served reconnection
        assertEquals(0, broadcaster.historyStats().replays());
    }

    @Test
    void testFailedDirectSendDeregisters() {
        var broadcaster = new SseBroadcaster();
        var request = new MockRequest();
        var context = new Context("", new Site() {
            public void setup() {
            }
        }, request, new WriteFailingResponse(request), null);

        // the initial flush succeeds and the connection registers
        var connection = context.sse(broadcaster);
        assertTrue(connection.isOpen());
        assertEquals(1, broadcaster.connectionCount());

        // a failed direct send closes and deregisters the connection
        assertFalse(connection.send(new ServerSentEvent().data("x")));
        assertFalse(connection.isOpen());
        assertEquals(0, broadcaster.connectionCount());
    }

    @Test
    @Timeout(120)
    void testReplayNotOvertakenByLiveEvents()
    throws Exception {
        // register connections while another thread broadcasts continuously,
        // and verify that every connection receives its events with strictly
        // ascending IDs: the replay always completes before live delivery
        for (var run = 0; run < 30; run++) {
            var site = new DetachedBlockSite();
            site.broadcaster.history(500);
            var m = new MockConversation(site);

            site.broadcaster.send("seed 1");
            site.broadcaster.send("seed 2");

            var stop = new java.util.concurrent.atomic.AtomicBoolean(false);
            var sender = new Thread(() -> {
                while (!stop.get()) {
                    site.broadcaster.send("live");
                }
            });
            sender.start();
            try {
                var response = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 0)));
                stop.set(true);
                sender.join();
                // sequential with the sender's broadcasts, arrives last
                site.broadcaster.send("after registration");

                var previous = 0L;
                for (var event : response.getEvents()) {
                    var id = seq(event.getId());
                    assertTrue(id > previous,
                        "event " + id + " arrived after " + previous);
                    previous = id;
                }
                assertTrue(previous >= 3);
            } finally {
                stop.set(true);
                sender.join();
                site.broadcaster.close();
            }
        }
    }

    static class CountingData implements CharSequence {
        final java.util.concurrent.atomic.AtomicInteger conversions = new java.util.concurrent.atomic.AtomicInteger();
        private final String value_;

        CountingData(String value) {
            value_ = value;
        }

        public String toString() {
            conversions.incrementAndGet();
            return value_;
        }

        public int length() {
            return value_.length();
        }

        public char charAt(int index) {
            return value_.charAt(index);
        }

        public CharSequence subSequence(int start, int end) {
            return value_.subSequence(start, end);
        }
    }

    @Test
    void testDataEventFormattedOncePerBroadcast() {
        var site = new DetachedBlockSite();
        site.broadcaster.history(10);
        var m = new MockConversation(site);

        var first = m.doRequest("/events");
        var second = m.doRequest("/events");
        var third = m.doRequest("/events");

        // with history: one conversion for the snapshot and one for the
        // shared payload, independent of the number of connections
        var data = new CountingData("shared payload");
        assertEquals(3, site.broadcaster.send(new ServerSentEvent().name("tick").data(data)));
        assertEquals(2, data.conversions.get());

        for (var response : java.util.List.of(first, second, third)) {
            var events = response.getEvents();
            assertEquals(1, events.size());
            assertEquals("shared payload", events.get(0).getData());
            assertEquals(1, seq(events.get(0).getId()));
        }

        site.broadcaster.close();

        // without history: a single conversion for the shared payload
        var plain_site = new DetachedBlockSite();
        var plain_m = new MockConversation(plain_site);
        plain_m.doRequest("/events");
        plain_m.doRequest("/events");
        var plain_data = new CountingData("plain payload");
        assertEquals(2, plain_site.broadcaster.send(new ServerSentEvent().data(plain_data)));
        assertEquals(1, plain_data.conversions.get());

        plain_site.broadcaster.close();
    }

    @Test
    void testRestartCursorCountsAsGap() {
        var site = new DetachedBlockSite();
        site.broadcaster.history(10);
        var m = new MockConversation(site);

        site.broadcaster.send("one");
        site.broadcaster.send("two");

        // an ID from another epoch comes from a previous application
        // instance, nothing is replayed and the reconnection is counted as
        // a gap, even when the sequence number would be a plausible
        // position in the new instance's sequence
        var stale = m.doRequest("/events", new MockRequest().header("Last-Event-ID", "12345-1"));
        assertEquals(0, stale.getEvents().size());
        assertEquals(1, site.broadcaster.historyStats().gaps());

        // the same goes for a sequence number beyond the newest in the
        // current epoch
        var overrun = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 99)));
        assertEquals(0, overrun.getEvents().size());
        assertEquals(2, site.broadcaster.historyStats().gaps());

        // an up-to-date client isn't counted as a gap
        var current = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 2)));
        assertEquals(0, current.getEvents().size());
        assertEquals(2, site.broadcaster.historyStats().gaps());

        site.broadcaster.close();
    }

    @Test
    void testRestartEpochsDiffer() {
        // two broadcasters created in immediate succession, as happens
        // during a rapid redeployment, must not share an ID namespace
        var site = new DetachedBlockSite();
        site.broadcaster.history(10);
        var restarted_site = new DetachedBlockSite();
        restarted_site.broadcaster.history(10);

        var m = new MockConversation(site);
        var restarted_m = new MockConversation(restarted_site);

        var first = m.doRequest("/events");
        site.broadcaster.send("one");
        site.broadcaster.send("two");
        var old_cursor = first.getEvents().get(0).getId();

        for (var i = 1; i <= 5; i++) {
            restarted_site.broadcaster.send("new " + i);
        }

        // a cursor from the previous instance is never treated as a valid
        // position in the new instance's sequence, even though its sequence
        // number lies inside the new instance's buffered window
        var reconnected = restarted_m.doRequest("/events",
            new MockRequest().header("Last-Event-ID", old_cursor));
        assertEquals(0, reconnected.getEvents().size());
        assertEquals(1, restarted_site.broadcaster.historyStats().gaps());

        site.broadcaster.close();
        restarted_site.broadcaster.close();
    }

    @Test
    void testBroadcastRejectsNullAndEmptyEvents() {
        var site = new DetachedBlockSite();
        var m = new MockConversation(site);

        var response = m.doRequest("/events");
        assertEquals(1, site.broadcaster.connectionCount());

        assertThrows(IllegalArgumentException.class, () -> site.broadcaster.send((ServerSentEvent) null));
        assertThrows(IllegalArgumentException.class,
            () -> site.broadcaster.send(null, connection -> true));

        // an event without any fields set isn't sent and isn't counted as
        // received
        assertEquals(0, site.broadcaster.send(new ServerSentEvent()));
        assertEquals(0, response.getEvents().size());

        site.broadcaster.close();
    }

    @Test
    void testSseAfterOutputRefused() {
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/print", c -> {
                    c.print("earlier content");
                    assertThrows(rife.engine.exceptions.SseConnectionAfterOutputException.class, c::sse);
                    assertThrows(rife.engine.exceptions.SseConnectionAfterOutputException.class,
                        () -> c.sse(new SseBroadcaster()));
                });
                get("/writer", c -> {
                    try {
                        c.response().getWriter();
                    } catch (java.io.IOException e) {
                        throw new RuntimeException(e);
                    }
                    assertThrows(rife.engine.exceptions.SseConnectionAfterOutputException.class, c::sse);
                });
                get("/closed-stream", c -> {
                    // the output state survives the closing of the stream
                    c.outputStream();
                    c.response().close();
                    assertThrows(rife.engine.exceptions.SseConnectionAfterOutputException.class, c::sse);
                });
            }
        });
        m.doRequest("/print");
        m.doRequest("/writer");
        m.doRequest("/closed-stream");
    }

    @Test
    void testSseTerminalForElementChain() {
        var broadcaster = new SseBroadcaster();
        var m = new MockConversation(new Site() {
            public void setup() {
                after(c -> c.print("footer"));
                get("/element", c -> c.sse().send("element event"));
                get("/detached", c -> c.sse(broadcaster));
            }
        });

        // the after element doesn't run for an element-controlled stream
        var element = m.doRequest("/element");
        assertEquals("data: element event\n\n", element.getText());

        // nor for a detached stream
        var detached = m.doRequest("/detached");
        broadcaster.send("detached event");
        assertEquals("data: detached event\n\n", detached.getText());

        broadcaster.close();
    }

    @Test
    void testPrintAfterSseRefused() {
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/events", c -> {
                    var sse = c.sse();
                    assertThrows(rife.engine.exceptions.SseOutputRefusedException.class, () -> c.print("junk"));
                    assertThrows(rife.engine.exceptions.SseOutputRefusedException.class,
                        () -> c.print(c.template("sse_fragment")));
                    sse.send("only event");
                });
            }
        });

        var response = m.doRequest("/events");
        assertEquals("data: only event\n\n", response.getText());
    }

    @Test
    void testSecondSseConnectionRefused() {
        var broadcaster = new SseBroadcaster();
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/events", c -> {
                    c.sse();
                    assertThrows(rife.engine.exceptions.SseConnectionAlreadyEstablishedException.class, c::sse);
                    assertThrows(rife.engine.exceptions.SseConnectionAlreadyEstablishedException.class, () -> c.sse(broadcaster));
                });
            }
        });

        m.doRequest("/events");
        assertEquals(0, broadcaster.connectionCount());
    }

    @Test
    void testClosedConnectionNotRegistered() {
        var site = new HolderSite();
        var m = new MockConversation(site);
        m.doRequest("/events");

        var connection = site.holder[0];
        connection.close();

        // a connection that was closed before registration completes isn't
        // added, with or without history
        var plain = new SseBroadcaster();
        plain.register(connection);
        assertEquals(0, plain.connectionCount());

        var with_history = new SseBroadcaster().history(5);
        with_history.register(connection);
        assertEquals(0, with_history.connectionCount());
    }

    @Test
    void testEventNameAndIdRejectLineBreaks() {
        assertThrows(IllegalArgumentException.class, () -> new ServerSentEvent().name("multi\nline"));
        assertThrows(IllegalArgumentException.class, () -> new ServerSentEvent().name("multi\rline"));
        assertThrows(IllegalArgumentException.class, () -> new ServerSentEvent().id("multi\nline"));
        assertThrows(IllegalArgumentException.class, () -> new ServerSentEvent().id("multi\rline"));

        // browsers ignore IDs with NUL characters when tracking their last
        // event ID
        assertThrows(IllegalArgumentException.class, () -> new ServerSentEvent().id("nul\0id"));

        // negative retry values would silently be treated as unset
        assertThrows(IllegalArgumentException.class, () -> new ServerSentEvent().retry(-1));

        // single lines are accepted
        new ServerSentEvent().name("single line").id("42");
    }

    @Test
    @Timeout(120)
    void testHeartbeat()
    throws Exception {
        var site = new HolderSite();
        var m = new MockConversation(site);
        var response = m.doRequest("/events");

        assertThrows(IllegalArgumentException.class, () -> site.broadcaster.heartbeat(null));
        assertThrows(IllegalArgumentException.class, () -> site.broadcaster.heartbeat(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> site.broadcaster.heartbeat(Duration.ofMillis(-1)));

        // sub-millisecond intervals are scheduled with their nanosecond
        // precision instead of truncating to zero milliseconds
        site.broadcaster.heartbeat(Duration.ofNanos(500_000));

        site.broadcaster.heartbeat(Duration.ofMillis(25));

        // a keep-alive comment arrives automatically
        var deadline = System.currentTimeMillis() + 10000;
        while (response.getEvents().isEmpty() &&
               System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertFalse(response.getEvents().isEmpty());
        assertEquals(List.of("keep-alive"), response.getEvents().get(0).getComments());

        // the heartbeat reaps connections whose clients are gone
        var request = new MockRequest();
        var context = new Context("", new Site() {
            public void setup() {
            }
        }, request, new WriteFailingResponse(request), null);
        context.sse(site.broadcaster);
        assertEquals(2, site.broadcaster.connectionCount());
        deadline = System.currentTimeMillis() + 10000;
        while (site.broadcaster.connectionCount() > 1 &&
               System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertEquals(1, site.broadcaster.connectionCount());

        // stopping the heartbeat leaves the connection untouched and no
        // further comments arrive
        site.broadcaster.stopHeartbeat();
        Thread.sleep(100);
        assertEquals(1, site.broadcaster.connectionCount());
        var settled = response.getEvents().size();
        Thread.sleep(200);
        assertEquals(settled, response.getEvents().size());

        // closing stops the heartbeat, and it can be established again
        site.broadcaster.heartbeat(Duration.ofMillis(25));
        site.broadcaster.close();
        assertEquals(0, site.broadcaster.connectionCount());

        var reconnected = m.doRequest("/events");
        site.broadcaster.heartbeat(Duration.ofMillis(25));
        deadline = System.currentTimeMillis() + 10000;
        while (reconnected.getEvents().isEmpty() &&
               System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertFalse(reconnected.getEvents().isEmpty());

        site.broadcaster.close();
    }

    @Test
    void testDirectSendBypassesHistory() {
        var site = new HolderSite();
        site.broadcaster.history(10);
        var m = new MockConversation(site);

        var first = m.doRequest("/events");
        site.broadcaster.send("through broadcaster");
        site.holder[0].send("directly to the connection");

        // the direct send carries no ID and isn't buffered
        var events = first.getEvents();
        assertEquals(2, events.size());
        assertEquals(1, seq(events.get(0).getId()));
        assertNull(events.get(1).getId());

        var reconnected = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 0)));
        var replayed = reconnected.getEvents();
        assertEquals(1, replayed.size());
        assertEquals("through broadcaster", replayed.get(0).getData());

        site.broadcaster.close();
    }

    @Test
    @Timeout(120)
    void testConcurrentBroadcasts()
    throws Exception {
        var site = new DetachedBlockSite();
        site.broadcaster.history(1000);
        var m = new MockConversation(site);

        var first = m.doRequest("/events");
        var second = m.doRequest("/events");

        var threads = 8;
        var events_per_thread = 50;
        var executor = java.util.concurrent.Executors.newFixedThreadPool(threads);
        try {
            var latch = new java.util.concurrent.CountDownLatch(threads);
            for (var t = 0; t < threads; t++) {
                executor.submit(() -> {
                    for (var i = 0; i < events_per_thread; i++) {
                        site.broadcaster.send("concurrent");
                    }
                    latch.countDown();
                });
            }
            assertTrue(latch.await(60, java.util.concurrent.TimeUnit.SECONDS));
        } finally {
            executor.shutdown();
        }

        var total = threads * events_per_thread;
        assertEquals(total, seq(site.broadcaster.lastEventId()));
        assertEquals(total, site.broadcaster.historyStats().buffered());

        // every connection received every event exactly once, IDs are
        // unique and complete even though the arrival order can vary
        for (var response : java.util.List.of(first, second)) {
            var events = response.getEvents();
            assertEquals(total, events.size());
            var ids = new java.util.HashSet<String>();
            for (var event : events) {
                assertTrue(ids.add(event.getId()));
            }
            for (var id = 1; id <= total; id++) {
                assertTrue(ids.contains(cursor(site.broadcaster, id)));
            }
        }

        site.broadcaster.close();
    }

    static class AuthSseSite extends Site {
        final rife.authentication.sessionvalidators.MemorySessionValidator validator =
            new rife.authentication.sessionvalidators.MemorySessionValidator();
        final rife.authentication.elements.AuthConfig config =
            new rife.authentication.elements.AuthConfig(validator);
        final SseBroadcaster broadcaster = new SseBroadcaster();

        public AuthSseSite() {
            validator.getCredentialsManager()
                .addRole("admin")
                .addUser("alice", new rife.authentication.credentialsmanagers.RoleUserAttributes("alicepass").role("admin"))
                .addUser("bob", new rife.authentication.credentialsmanagers.RoleUserAttributes("bobpass"));
        }

        final Route login = route("/login", new rife.authentication.elements.Login(config,
            rife.template.TemplateFactory.HTML.get("authentication.login")));
        final Route landing = get("/landing", c -> c.print("Landing"));

        public void setup() {
            config.loginRoute(login).landingRoute(landing);
            group(new Router() {
                public void setup() {
                    before(new rife.authentication.elements.Identified(config));
                    get("/events", c -> c.sse(broadcaster));
                }
            });
        }
    }

    private static MockConversation loginConversation(AuthSseSite site, String login, String password) {
        var conversation = new MockConversation(site);
        var form = conversation.doRequest("/login").getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", login);
        form.setParameter("password", password);
        form.submit();
        return conversation;
    }

    @Test
    void testAuthenticatedTargetedBroadcast() {
        var site = new AuthSseSite();

        var alice_conversation = loginConversation(site, "alice", "alicepass");
        var alice_events = alice_conversation.doRequest("/events");

        var bob_conversation = loginConversation(site, "bob", "bobpass");
        var bob_events = bob_conversation.doRequest("/events");

        assertEquals(2, site.broadcaster.connectionCount());

        // only the authenticated admin identity receives the event
        assertEquals(1, site.broadcaster.send(new ServerSentEvent().name("alert").data("admins only"),
            connection -> {
                var identity = site.config.identityAttribute(connection.context());
                return identity != null && identity.getAttributes().isInRole("admin");
            }));

        assertEquals(1, alice_events.getEvents().size());
        assertEquals("admins only", alice_events.getEvents().get(0).getData());
        assertEquals(0, bob_events.getEvents().size());

        site.broadcaster.close();
    }

    @Test
    void testHistoryReplay() {
        var site = new DetachedBlockSite();
        site.broadcaster.history(10);
        var m = new MockConversation(site);

        var first = m.doRequest("/events");
        assertEquals(1, site.broadcaster.send("one"));
        assertEquals(1, site.broadcaster.send(new ServerSentEvent().name("note").data("two")));
        assertEquals(1, site.broadcaster.send("three"));
        assertEquals(3, seq(site.broadcaster.lastEventId()));

        // application-assigned IDs and history are alternative reconnection
        // strategies that can't be combined
        assertThrows(IllegalArgumentException.class,
            () -> site.broadcaster.send(new ServerSentEvent().id("app-id").data("rejected")));
        assertEquals(3, seq(site.broadcaster.lastEventId()));

        // events are stamped with sequential IDs
        var first_events = first.getEvents();
        assertEquals(3, first_events.size());
        assertEquals(1, seq(first_events.get(0).getId()));
        assertEquals(2, seq(first_events.get(1).getId()));
        assertEquals(3, seq(first_events.get(2).getId()));

        // a client that reconnects with the browser reconnection header
        // receives the events it missed before joining the live stream
        var reconnected = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 1)));
        // both the initial and the reconnected connection are live now
        assertEquals(2, site.broadcaster.send("four"));

        var replayed = reconnected.getEvents();
        assertEquals(3, replayed.size());
        assertEquals(2, seq(replayed.get(0).getId()));
        assertEquals("two", replayed.get(0).getData());
        assertEquals(3, seq(replayed.get(1).getId()));
        assertEquals("three", replayed.get(1).getData());
        assertEquals(4, seq(replayed.get(2).getId()));
        assertEquals("four", replayed.get(2).getData());

        var stats = site.broadcaster.historyStats();
        assertEquals(10, stats.capacity());
        assertEquals(4, stats.buffered());
        assertEquals(1, stats.oldestId());
        assertEquals(4, stats.newestId());
        assertEquals(1, stats.replays());
        assertEquals(0, stats.gaps());
        assertEquals(2, stats.maxMissedEvents());

        site.broadcaster.close();
    }

    @Test
    void testHistoryFirstConnectParameter() {
        var site = new DetachedBlockSite();
        site.broadcaster.history(10);
        var m = new MockConversation(site);

        var first = m.doRequest("/events");
        site.broadcaster.send("one");
        site.broadcaster.send("two");

        // pages can pass the last rendered event ID as a parameter for the
        // initial connection, closing the gap between render and stream
        var connected = m.doRequest("/events?lastEventId=" + cursor(site.broadcaster, 1));
        var events = connected.getEvents();
        assertEquals(1, events.size());
        assertEquals(2, seq(events.get(0).getId()));
        assertEquals("two", events.get(0).getData());

        // an up-to-date client receives nothing
        var current = m.doRequest("/events?lastEventId=" + cursor(site.broadcaster, 2));
        assertEquals(0, current.getEvents().size());

        site.broadcaster.close();
    }

    @Test
    void testHistoryGap() {
        var site = new DetachedBlockSite();
        site.broadcaster.history(2);
        var m = new MockConversation(site);

        var first = m.doRequest("/events");
        for (var i = 1; i <= 5; i++) {
            site.broadcaster.send("event " + i);
        }

        // the events after ID 1 have been evicted, nothing is replayed
        var reconnected = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 1)));
        assertEquals(0, reconnected.getEvents().size());

        var stats = site.broadcaster.historyStats();
        assertEquals(2, stats.capacity());
        assertEquals(2, stats.buffered());
        assertEquals(4, stats.oldestId());
        assertEquals(5, stats.newestId());
        assertEquals(0, stats.replays());
        assertEquals(1, stats.gaps());
        assertEquals(4, stats.maxMissedEvents());

        // a reconnection inside the buffered window is still served
        var served = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 3)));
        assertEquals(2, served.getEvents().size());
        assertEquals(1, site.broadcaster.historyStats().replays());

        site.broadcaster.close();
    }

    @Test
    void testHistoryTemplateReplay() {
        var site = new DetachedBlockSite();
        site.broadcaster.history(10);
        var m = new MockConversation(site);

        var first = m.doRequest("/events");

        var t = rife.template.TemplateFactory.HTML.get("sse_blocks");
        t.setValue("symbol", "ACME");
        t.setValue("price", "42.10");
        site.broadcaster.send(t, "price_row");

        // mutations after broadcasting don't affect the captured history
        t.setValue("price", "99.99");

        var reconnected = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 0)));
        var replayed = reconnected.getEvents();
        assertEquals(1, replayed.size());
        assertEquals(1, seq(replayed.get(0).getId()));
        assertEquals("<p>ACME: 42.10 <a href=\"http://localhost/events\">watch</a></p>", replayed.get(0).getData());
        assertEquals("42.10", replayed.get(0).getTemplate().getValue("price"));

        site.broadcaster.close();
    }

    @Test
    void testHistoryFilteredReplay() {
        var site = new DetachedBlockSite();
        site.broadcaster.history(10);
        var m = new MockConversation(site);

        var first = m.doRequest("/events?user=carol");
        site.broadcaster.send(new ServerSentEvent().data("for alice"),
            connection -> "alice".equals(connection.context().parameter("user")));
        site.broadcaster.send("for everybody");

        // targeted events are only replayed to connections that match
        var alice = m.doRequest("/events?user=alice", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 0)));
        var alice_events = alice.getEvents();
        assertEquals(2, alice_events.size());
        assertEquals("for alice", alice_events.get(0).getData());
        assertEquals("for everybody", alice_events.get(1).getData());

        var bob = m.doRequest("/events?user=bob", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 0)));
        var bob_events = bob.getEvents();
        assertEquals(1, bob_events.size());
        assertEquals("for everybody", bob_events.get(0).getData());

        site.broadcaster.close();
    }

    @Test
    void testHistorySkipsHeartbeats() {
        var site = new DetachedBlockSite();
        site.broadcaster.history(10);
        var m = new MockConversation(site);

        var first = m.doRequest("/events");
        site.broadcaster.send("one");
        site.broadcaster.comment("keep-alive");
        site.broadcaster.send("two");

        // heartbeats receive no IDs and aren't buffered
        var first_events = first.getEvents();
        assertEquals(3, first_events.size());
        assertEquals(1, seq(first_events.get(0).getId()));
        assertNull(first_events.get(1).getId());
        assertEquals(List.of("keep-alive"), first_events.get(1).getComments());
        assertEquals(2, seq(first_events.get(2).getId()));

        var reconnected = m.doRequest("/events", new MockRequest().header("Last-Event-ID", cursor(site.broadcaster, 1)));
        var replayed = reconnected.getEvents();
        assertEquals(1, replayed.size());
        assertEquals("two", replayed.get(0).getData());

        assertThrows(IllegalArgumentException.class, () -> site.broadcaster.history(0));

        site.broadcaster.close();
    }

    @Test
    void testCloseFiltered() {
        var site = new DetachedBlockSite();
        var m = new MockConversation(site);

        var alice = m.doRequest("/events?user=alice");
        var bob = m.doRequest("/events?user=bob");
        assertEquals(2, site.broadcaster.connectionCount());

        assertThrows(IllegalArgumentException.class, () -> site.broadcaster.close(null));

        // only the matching connection is closed, the other one stays live
        assertEquals(1, site.broadcaster.close(
            connection -> "alice".equals(connection.context().parameter("user"))));
        assertEquals(1, site.broadcaster.connectionCount());

        assertEquals(1, site.broadcaster.send("still here"));
        assertEquals(0, alice.getEvents().size());
        assertEquals(1, bob.getEvents().size());

        site.broadcaster.close();
    }

    public static class Stock {
        private String symbol_ = null;
        private double price_ = 0;
        private String note_ = null;

        public void setSymbol(String symbol) {
            symbol_ = symbol;
        }

        public String getSymbol() {
            return symbol_;
        }

        public void setPrice(double price) {
            price_ = price;
        }

        public double getPrice() {
            return price_;
        }

        public void setNote(String note) {
            note_ = note;
        }

        public String getNote() {
            return note_;
        }
    }

    @Test
    void testJsonEventData() {
        var site = new DetachedBlockSite();
        var m = new MockConversation(site);
        var response = m.doRequest("/events");

        var stock = new Stock();
        stock.setSymbol("ACME");
        stock.setPrice(42.1);
        stock.setNote("line one\nline two");
        assertEquals(1, site.broadcaster.send(new ServerSentEvent().name("stock").json(stock)));
        assertEquals(1, site.broadcaster.send(new ServerSentEvent().name("list").json(java.util.List.of(stock))));
        assertEquals(1, site.broadcaster.send(new ServerSentEvent().name("map").json(java.util.Map.of("count", 3))));
        assertEquals(1, site.broadcaster.send(new ServerSentEvent().name("text").json("hello")));

        var events = response.getEvents();
        assertEquals(4, events.size());

        // beans become JSON objects, and since compact JSON contains no
        // line breaks, the payload is a single data field even when the
        // values contain them
        var stock_event = events.get(0);
        assertEquals("stock", stock_event.getName());
        assertFalse(stock_event.getData().contains("\n"));
        var parsed = stock_event.getDataAsJsonObject();
        assertEquals("ACME", parsed.getString("symbol"));
        assertEquals(42.1, parsed.getDouble("price"));
        assertEquals("line one\nline two", parsed.getString("note"));

        var round_tripped = stock_event.getDataAsBean(Stock.class);
        assertEquals("ACME", round_tripped.getSymbol());
        assertEquals(42.1, round_tripped.getPrice());
        assertEquals("line one\nline two", round_tripped.getNote());

        // the elements of collections are converted as well
        assertTrue(events.get(1).getData().startsWith("[{"));
        assertTrue(events.get(1).getData().contains("\"symbol\":\"ACME\""));

        // JSON-native values are transmitted directly
        assertEquals("{\"count\":3}", events.get(2).getData());
        assertEquals("\"hello\"", events.get(3).getData());

        site.broadcaster.close();
    }

    @Test
    void testJsonTemplateEventData() {
        var site = new DetachedBlockSite();
        var m = new MockConversation(site);
        var response = m.doRequest("/events");

        // JSON templates flow through the factory-agnostic template path,
        // with setBean() filling the values through the JSON bean handler
        // and the filtered tags resolved against each connection's context
        var t = rife.template.TemplateFactory.JSON.get("sse_stock");
        var stock = new Stock();
        stock.setSymbol("AC\"ME");
        stock.setPrice(42.1);
        t.setBean(stock);
        assertEquals(1, site.broadcaster.send(new ServerSentEvent().name("stock").template(t)));

        var event = response.getEvents().get(0);
        assertEquals("stock", event.getName());
        var parsed = event.getDataAsJsonObject();
        assertEquals("AC\"ME", parsed.getString("symbol"));
        assertEquals(42.1, parsed.getDouble("price"));
        assertEquals("http://localhost/events", parsed.getString("watch"));

        site.broadcaster.close();
    }

    @Test
    void testTargetedBroadcast() {
        var site = new DetachedBlockSite();
        var m = new MockConversation(site);

        var alice = m.doRequest("/events?user=alice");
        var bob = m.doRequest("/events?user=bob");
        assertEquals(2, site.broadcaster.connectionCount());

        // only the matching connection receives the event
        assertEquals(1, site.broadcaster.send(new ServerSentEvent().name("note").data("for alice"),
            connection -> "alice".equals(connection.context().parameter("user"))));
        // an untargeted send still reaches everybody
        assertEquals(2, site.broadcaster.send(new ServerSentEvent().name("note").data("for all")));

        var alice_events = alice.getEvents();
        assertEquals(2, alice_events.size());
        assertEquals("for alice", alice_events.get(0).getData());
        assertEquals("for all", alice_events.get(1).getData());

        var bob_events = bob.getEvents();
        assertEquals(1, bob_events.size());
        assertEquals("for all", bob_events.get(0).getData());

        assertThrows(IllegalArgumentException.class,
            () -> site.broadcaster.send(new ServerSentEvent().data("x"), null));

        site.broadcaster.close();
    }

    @Test
    void testMockDetachedBroadcastTemplateBlock() {
        var site = new DetachedBlockSite();
        var m = new MockConversation(site);

        var response = m.doRequest("/events");
        assertEquals(1, site.broadcaster.connectionCount());

        var t = rife.template.TemplateFactory.HTML.get("sse_blocks");
        t.setValue("symbol", "ACME");
        t.setValue("price", "42.10");
        assertEquals(1, site.broadcaster.send(t, "price_row"));

        var events = response.getEvents();
        assertEquals(1, events.size());
        assertEquals("<p>ACME: 42.10 <a href=\"http://localhost/events\">watch</a></p>", events.get(0).getData());
        assertEquals("ACME", events.get(0).getTemplate().getValue("symbol"));

        site.broadcaster.close();
    }

    static class ResetBlockSite extends Site {
        Route events = get("/events", c -> {
            var t = c.template("sse_fragment");
            t.setValue("count", 7);
            // template() after templateBlock() sends the whole content again
            c.sse().send(new ServerSentEvent().templateBlock(t, "unused").template(t));
        });
    }

    @Test
    void testTemplateResetsBlock() {
        var m = new MockConversation(new ResetBlockSite());

        var events = m.doRequest("/events").getEvents();
        assertEquals(1, events.size());
        assertEquals("<div>count 7 <a href=\"http://localhost/events\">stream</a></div>", events.get(0).getData());
    }

    private static Site createCountSite() {
        return new Site() {
            public void setup() {
                get("/events", c -> {
                    var sse = c.sse();
                    for (var i = 1; i <= 3; i++) {
                        sse.send(new ServerSentEvent().name("count").id(String.valueOf(i)).data("tick " + i));
                    }
                });
            }
        };
    }

    private static String requestEventStream(int port)
    throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/events"))
            .timeout(Duration.ofSeconds(30)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/event-stream"));
        return response.body();
    }

    private static final String EXPECTED_COUNT_STREAM =
        "event: count\nid: 1\ndata: tick 1\n\n" +
        "event: count\nid: 2\ndata: tick 2\n\n" +
        "event: count\nid: 3\ndata: tick 3\n\n";

    @Test
    @Timeout(120)
    void testEventStream()
    throws Exception {
        try (final var server = new TestServerRunner(createCountSite())) {
            assertEquals(EXPECTED_COUNT_STREAM, requestEventStream(8181));
        }
    }

    @Test
    @Timeout(120)
    void testTomcatEventStream()
    throws Exception {
        try (final var server = new TestTomcatRunner(createCountSite())) {
            assertEquals(EXPECTED_COUNT_STREAM, requestEventStream(8282));
        }
    }

    @Test
    @Timeout(120)
    void testTemplateBlockStream()
    throws Exception {
        try (final var server = new TestServerRunner(new BlockSite())) {
            assertEquals("event: price\ndata: <p>ACME: 42.10 <a href=\"http://localhost:8181/events\">watch</a></p>\n\n" +
                         "data: <p>ACME: 42.35 <a href=\"http://localhost:8181/events\">watch</a></p>\n\n",
                requestEventStream(8181));
        }
    }

    private static void broadcastEventStream(int port, SseBroadcaster broadcaster)
    throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/stream"))
            .timeout(Duration.ofSeconds(30)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        assertEquals(200, response.statusCode());

        // wait for the detached connection to be registered
        var deadline = System.currentTimeMillis() + 30000;
        while (broadcaster.connectionCount() == 0 &&
               System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertEquals(1, broadcaster.connectionCount());

        assertEquals(1, broadcaster.send(new ServerSentEvent().name("tick").id("42").data("hello")));
        assertEquals(1, broadcaster.comment("ping"));
        broadcaster.close();
        assertEquals(0, broadcaster.connectionCount());

        var body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("event: tick\nid: 42\ndata: hello\n\n" +
                     ": ping\n\n", body);
    }

    @Test
    @Timeout(120)
    void testDetachedBroadcast()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/stream", c -> c.sse(broadcaster));
            }
        })) {
            broadcastEventStream(8181, broadcaster);
        }
    }

    @Test
    @Timeout(120)
    void testTomcatDetachedBroadcast()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        try (final var server = new TestTomcatRunner(new Site() {
            public void setup() {
                get("/stream", c -> c.sse(broadcaster));
            }
        })) {
            broadcastEventStream(8282, broadcaster);
        }
    }

    // Stands up a Jetty server with the RIFE2 filter and servlet with async
    // support explicitly disabled, which reproduces the default of a plain war
    // deployment where <async-supported>true</async-supported> hasn't been
    // declared in web.xml. Note that Jetty's programmatic addFilter defaults
    // async support to true, so it has to be turned off explicitly here to
    // match the servlet specification's web.xml default of false.
    private static org.eclipse.jetty.server.Server startNonAsyncServer(int port, Site site)
    throws Exception {
        var server = new org.eclipse.jetty.server.Server();
        var connector = new org.eclipse.jetty.server.ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        var ctx = new org.eclipse.jetty.ee10.servlet.ServletContextHandler();
        ctx.setContextPath("/");

        var rife_filter = new rife.servlet.RifeFilter();
        rife_filter.init(new rife.ioc.HierarchicalProperties(), site);
        var filter_holder = new org.eclipse.jetty.ee10.servlet.FilterHolder(rife_filter);
        filter_holder.setAsyncSupported(false);
        ctx.addFilter(filter_holder, "/*", java.util.EnumSet.of(jakarta.servlet.DispatcherType.REQUEST));
        var servlet_holder = new org.eclipse.jetty.ee10.servlet.ServletHolder("default", org.eclipse.jetty.ee10.servlet.DefaultServlet.class);
        servlet_holder.setAsyncSupported(false);
        ctx.addServlet(servlet_holder, "/*");

        server.setHandler(ctx);
        server.start();
        return server;
    }

    public static class NonAsyncTomcatSite extends Site {
        static final SseBroadcaster BROADCASTER = new SseBroadcaster();

        public void setup() {
            get("/element", c -> c.sse().send(new ServerSentEvent().name("ok").data("hi")));
            get("/broadcast", c -> c.sse(BROADCASTER));
        }
    }

    // Stands up a Tomcat server whose RIFE2 filter is declared through an
    // actual web.xml, like a war deployment, so that the servlet
    // specification's async-supported default of false applies. Tomcat's
    // programmatic registration APIs default async support to true, which
    // is why a web.xml is needed to reproduce the war behavior.
    private static org.apache.catalina.startup.Tomcat startNonAsyncTomcat(int port)
    throws Exception {
        var doc_base = new java.io.File(rife.config.RifeConfig.global().getTempPath(), "rife2.tomcat.nonasync." + port + ".webapp");
        var web_inf = new java.io.File(doc_base, "WEB-INF");
        web_inf.mkdirs();
        java.nio.file.Files.writeString(new java.io.File(web_inf, "web.xml").toPath(), """
            <?xml version="1.0" encoding="UTF-8"?>
            <web-app xmlns="https://jakarta.ee/xml/ns/jakartaee" version="6.0">
              <filter>
                <filter-name>RIFE2</filter-name>
                <filter-class>rife.servlet.RifeFilter</filter-class>
                <init-param>
                  <param-name>rifeSiteClass</param-name>
                  <param-value>rife.engine.TestSse$NonAsyncTomcatSite</param-value>
                </init-param>
              </filter>
              <filter-mapping>
                <filter-name>RIFE2</filter-name>
                <url-pattern>/*</url-pattern>
              </filter-mapping>
            </web-app>
            """);

        var tomcat = new org.apache.catalina.startup.Tomcat();
        var base_dir = new java.io.File(rife.config.RifeConfig.global().getTempPath(), "rife2.tomcat.nonasync." + port);
        tomcat.setBaseDir(base_dir.getAbsolutePath());
        var ctx = tomcat.addWebapp("", doc_base.getAbsolutePath());
        var jar_scanner = new org.apache.tomcat.util.scan.StandardJarScanner();
        jar_scanner.setScanManifest(false);
        ctx.setJarScanner(jar_scanner);

        tomcat.setPort(port);
        tomcat.getConnector();
        tomcat.start();
        return tomcat;
    }

    @Test
    @Timeout(120)
    void testTomcatNonAsyncDeploymentDiagnostic()
    throws Exception {
        var pretty = RifeConfig.engine().getPrettyEngineExceptions();
        RifeConfig.engine().setPrettyEngineExceptions(false);

        var broadcaster = NonAsyncTomcatSite.BROADCASTER;
        var tomcat = startNonAsyncTomcat(8384);
        try {
            var client = HttpClient.newHttpClient();

            // element-controlled streaming works without async support
            var element = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:8384/element")).timeout(Duration.ofSeconds(15)).build(),
                HttpResponse.BodyHandlers.ofString());
            assertEquals(200, element.statusCode());
            assertEquals("event: ok\ndata: hi\n\n", element.body());

            // detached broadcaster mode fails loudly with the diagnostic
            var broadcast = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:8384/broadcast")).timeout(Duration.ofSeconds(15)).build(),
                HttpResponse.BodyHandlers.ofString());
            assertEquals(500, broadcast.statusCode());
            assertEquals(0, broadcaster.connectionCount());
        } finally {
            tomcat.stop();
            tomcat.destroy();
            RifeConfig.engine().setPrettyEngineExceptions(pretty);
        }
    }

    @Test
    @Timeout(120)
    void testNonAsyncDeploymentDiagnostic()
    throws Exception {
        // surface the raw exception as a 500 instead of a pretty error page
        var pretty = RifeConfig.engine().getPrettyEngineExceptions();
        RifeConfig.engine().setPrettyEngineExceptions(false);

        var broadcaster = new SseBroadcaster();
        var server = startNonAsyncServer(8383, new Site() {
            public void setup() {
                // element-controlled streaming doesn't use async support
                get("/element", c -> c.sse().send(new ServerSentEvent().name("ok").data("hi")));
                // detached broadcaster mode requires async support, which
                // the filter of a plain war deployment doesn't declare
                get("/broadcast", c -> c.sse(broadcaster));
            }
        });
        try {
            var client = HttpClient.newHttpClient();

            // element-controlled streaming works without async support
            var element = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:8383/element")).timeout(Duration.ofSeconds(15)).build(),
                HttpResponse.BodyHandlers.ofString());
            assertEquals(200, element.statusCode());
            assertEquals("event: ok\ndata: hi\n\n", element.body());

            // detached broadcaster mode fails loudly with the diagnostic
            // rather than hanging
            var broadcast = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:8383/broadcast")).timeout(Duration.ofSeconds(15)).build(),
                HttpResponse.BodyHandlers.ofString());
            assertEquals(500, broadcast.statusCode());
            assertEquals(0, broadcaster.connectionCount());
        } finally {
            server.stop();
            RifeConfig.engine().setPrettyEngineExceptions(pretty);
        }
    }

    @Test
    @Timeout(120)
    void testNonAsyncCustomExceptionRoute()
    throws Exception {
        var broadcaster = new SseBroadcaster();
        var server = startNonAsyncServer(8386, new Site() {
            public void setup() {
                get("/broadcast", c -> c.sse(broadcaster));
                exception(c -> c.print("custom error"));
            }
        });
        try {
            var client = HttpClient.newHttpClient();

            // the failed detachment leaves the response pristine, so the
            // custom exception route renders a normal response instead of
            // an event stream
            var broadcast = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:8386/broadcast")).timeout(Duration.ofSeconds(15)).build(),
                HttpResponse.BodyHandlers.ofString());
            assertTrue(broadcast.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
            assertEquals("custom error", broadcast.body());
            assertEquals(0, broadcaster.connectionCount());
        } finally {
            server.stop();
        }
    }
}
