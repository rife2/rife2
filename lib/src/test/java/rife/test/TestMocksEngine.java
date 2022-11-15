/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import rife.engine.*;
import rife.template.TemplateFactory;
import rife.tools.IntegerUtils;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public class TestMocksEngine {
    @Test
    public void testSimplePlain() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/plain", c -> {
                    c.contentType("text/plain");
                    c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                });
            }
        });

        var response = conversation.doRequest("http://localhost/simple/plain");
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());
    }

    @Test
    public void testSimpleHtml() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/html", c -> c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo()));
            }
        });

        var response = conversation.doRequest("http://localhost/simple/html");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());
    }

    @Test
    public void testSimplePathInfo() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/pathinfo", PathInfoHandling.CAPTURE, c -> c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo()));
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
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/headers", c -> {
                    c.header("Content-Disposition", "attachment; filename=thefile.zip");
                    var cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    cal.set(2002, Calendar.OCTOBER, 25, 19, 20, 58);
                    c.header("DateHeader", cal.getTimeInMillis());
                    c.header("IntHeader", 1212);

                    c.print("headers");
                });
            }
        });

        var response = conversation.doRequest("http://localhost/headers");
        assertEquals(200, response.getStatus());

        assertTrue(response.getHeaderNames().size() > 3);
        assertEquals("attachment; filename=thefile.zip", response.getHeader("Content-Disposition"));
        assertEquals("Fri, 25 Oct 2002 19:20:58 GMT", response.getHeader("DateHeader"));
        assertEquals("1212", response.getHeader("IntHeader"));
    }

    @Test
    public void testWrongServerRootUrl() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/html", c -> c.print("Just some text " + c.remoteAddr() + ":" + c.remoteHost() + ":" + c.pathInfo()));
            }
        });

        assertNull(conversation.doRequest("http://10.0.0.1/simple/html"));
    }

    @Test
    public void testCookies() {
        var conversation = new MockConversation(new Site() {
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

                get("/cookies2", c -> c.print(c.cookieValue("cookie2") + "," + c.cookieValue("cookie3") + "," + c.cookieValue("cookie4")));
            }
        });
        conversation
            .cookie("cookie1", "this is the first cookie")
            .cookie("cookie2", "this is the second cookie")
            .cookie("cookie3", "this is the third cookie");

        var response = conversation.doRequest("/cookies1");

        // check if the correct cookies were returned
        assertEquals(conversation.getCookie("cookie3").getValue(), "this is the first cookie");
        assertEquals(conversation.getCookie("cookie4").getValue(), "this is the second cookie");

        // new page with cookie context
        conversation.cookie("cookie4", "this is the fourth cookie");
        response = conversation.doRequest("/cookies2");
        assertEquals("this is the second cookie,this is the first cookie,this is the fourth cookie", response.getText());
    }

    @Test
    public void testContentlength() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/contentlength", c -> {
                    var out = "this goes out";
                    c.contentLength(out.length());
                    var outputstream = c.outputStream();
                    outputstream.write(out.getBytes(StandardCharsets.ISO_8859_1));
                });
            }
        });

        var response = conversation.doRequest("/contentlength");
        assertEquals(13, response.getContentLength());
        assertEquals("this goes out", response.getText());
    }

    @Test
    public void testDynamicContenttype() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/dynamiccontenttype", c -> {
                    switch (c.parameter("switch")) {
                        case "text" -> c.contentType("text/plain");
                        case "html" -> c.contentType("text/html");
                    }
                });
            }
        });

        assertEquals(conversation.doRequest("/dynamiccontenttype?switch=text").getContentType(), "text/plain; charset=UTF-8");
        assertEquals(conversation.doRequest("/dynamiccontenttype?switch=html").getContentType(), "text/html; charset=UTF-8");
    }

    @Test
    public void testBinary() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/binary", c -> c.outputStream().write(IntegerUtils.intToBytes(87634675)));
            }
        });

        var response = conversation.doRequest("/binary");
        var integer_bytes = response.getBytes();
        assertEquals(87634675, IntegerUtils.bytesToInt(integer_bytes));
    }

    @Test
    public void testPrintAndWriteBuffer() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/printandwrite_buffer", c -> {
                    c.enableTextBuffer(true);

                    c.print("print1");
                    c.outputStream().write("write2".getBytes(c.response().getCharacterEncoding()));
                    c.print("print3");
                    c.outputStream().write("write4".getBytes(c.response().getCharacterEncoding()));
                });
            }
        });

        assertEquals("write2write4print1print3", conversation.doRequest("/printandwrite_buffer").getText());
    }

    @Test
    public void testPrintAndWriteNoBuffer() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/printandwrite_nobuffer", c -> {
                    c.enableTextBuffer(false);

                    c.print("print1");
                    c.outputStream().write("write2".getBytes(c.response().getCharacterEncoding()));
                    c.print("print3");
                    c.outputStream().write("write4".getBytes(c.response().getCharacterEncoding()));
                });
            }
        });

        assertEquals("print1write2print3write4", conversation.doRequest("/printandwrite_nobuffer").getText());
    }

    @Test
    public void testGenerateForm() {
        var conversation = new MockConversation(new GenerateFormSite());
        assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_values").getContent(), conversation.doRequest("/form").getText());
        assertEquals(TemplateFactory.HTML.get("formbuilder_fields").getContent(), conversation.doRequest("/form?remove=1").getText());
    }

    @Test
    public void testGenerateFormPrefix() {
        var conversation = new MockConversation(new GenerateFormSite());
        assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_values").getContent(), conversation.doRequest("/form?prefix=1").getText());
        assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix").getContent(), conversation.doRequest("/form?prefix=1&remove=1").getText());
    }

    @Test
    public void testGenerateEmptyForm() {
        var conversation = new MockConversation(new GenerateEmptyFormSite());
        assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_empty").getContent(), conversation.doRequest("/form_empty").getText());
        assertEquals(TemplateFactory.HTML.get("formbuilder_fields").getContent(), conversation.doRequest("/form_empty?remove=1").getText());
    }

    @Test
    public void testGenerateEmptyFormPrefix() {
        var conversation = new MockConversation(new GenerateEmptyFormSite());
        assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_empty").getContent(), conversation.doRequest("/form_empty?prefix=1").getText());
        assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix").getContent(), conversation.doRequest("/form_empty?prefix=1&remove=1").getText());
    }
}
