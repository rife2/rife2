/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import rife.engine.PathInfoHandling;
import rife.engine.Site;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public class TestEngineMocks {
    @Test
    public void testSimplePlain() {
        MockConversation conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/plain", c -> {
                    c.contentType("text/plain");
                    c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                });
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/plain");
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());
    }

    @Test
    public void testSimpleHtml() {
        MockConversation conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/html", c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                });
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/html");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());
    }

    @Test
    public void testSimplePathInfo() {
        MockConversation conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/pathinfo", PathInfoHandling.CAPTURE, c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                });
            }
        });

        MockResponse response;

        response = conversation.doRequest("http://localhost/simple/pathinfo/some/path");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:some/path", response.getText());

        response = conversation.doRequest("http://localhost/simple/pathinfo/");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());

        response = conversation.doRequest("http://localhost/simple/pathinfo");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());

        response = conversation.doRequest("http://localhost/simple/pathinfo/another_path_info");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:another_path_info", response.getText());

        response = conversation.doRequest("http://localhost/simple/pathinfoddd");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testHeaders() {
        MockConversation conversation = new MockConversation(new Site() {
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
        });

        MockResponse response = conversation.doRequest("http://localhost/headers");
        assertEquals(200, response.getStatus());

        assertTrue(response.getHeaderNames().size() > 3);
        assertEquals("attachment; filename=thefile.zip", response.getHeader("Content-Disposition"));
        assertEquals("Fri, 25 Oct 2002 19:20:58 GMT", response.getHeader("DateHeader"));
        assertEquals("1212", response.getHeader("IntHeader"));
    }

    @Test
    public void testWrongServerRootUrl() {
        MockConversation conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/html", c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.remoteHost() + ":" + c.pathInfo());
                });
            }
        });

        assertNull(conversation.doRequest("http://10.0.0.1/simple/html"));
    }

    @Test
    public void testCookies() {
        MockConversation conversation = new MockConversation(new Site() {
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
        });
        conversation
            .cookie("cookie1", "this is the first cookie")
            .cookie("cookie2", "this is the second cookie")
            .cookie("cookie3", "this is the third cookie");

        MockResponse response = conversation.doRequest("/cookies1");

        // check if the correct cookies were returned
        assertEquals(conversation.getCookie("cookie3").getValue(), "this is the first cookie");
        assertEquals(conversation.getCookie("cookie4").getValue(), "this is the second cookie");

        // new page with cookie context
        conversation.cookie("cookie4", "this is the fourth cookie");
        response = conversation.doRequest("/cookies2");
        assertEquals("this is the second cookie,this is the first cookie,this is the fourth cookie", response.getText());
    }

}
