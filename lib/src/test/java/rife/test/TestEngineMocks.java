/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import rife.engine.Site;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestEngineMocks {
    @Test
    public void testSimpleHtml() {
        MockConversation conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/html", c -> {
                    c.print("Just some text " + c.request().getRemoteAddr() + ":" + c.request().getRemoteHost() + ":" + c.pathInfo());
                });
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/html");
        assertEquals(200, response.getStatus());

        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:localhost:", response.getText());
    }

    @Test
    public void testWrongServerRootUrl() {
        MockConversation conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/html", c -> {
                    c.print("Just some text " + c.request().getRemoteAddr() + ":" + c.request().getRemoteHost() + ":" + c.pathInfo());
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
                    if (c.request().hasCookie("cookie1") &&
                        c.request().hasCookie("cookie2") &&
                        c.request().hasCookie("cookie3")) {
                        Cookie cookie1 = c.request().getCookie("cookie1");
                        Cookie cookie2 = c.request().getCookie("cookie2");
                        Cookie cookie3 = new Cookie("cookie3", cookie1.getValue());
                        Cookie cookie4 = new Cookie("cookie4", cookie2.getValue());

                        c.response().addCookie(cookie3);
                        c.response().addCookie(cookie4);
                    }

                    c.print("source");
                });

                get("/cookies2", c -> {
                    Cookie cookie2 = c.request().getCookie("cookie2");
                    Cookie cookie3 = c.request().getCookie("cookie3");
                    Cookie cookie4 = c.request().getCookie("cookie4");

                    c.print(cookie2.getValue() + "," + cookie3.getValue() + "," + cookie4.getValue());
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
