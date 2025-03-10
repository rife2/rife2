/*
 * Copyright 2001-2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

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
    void testSimplePlain() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/plain", c -> {
                    c.setContentType("text/plain");
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
    void testSimpleHtml() {
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
    void testSimpleHead() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = head("/simple/plain", c -> c.setContentType("text/plain"));
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/plain",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("", response.getText());

        response = conversation.doRequest("http://localhost/simple/plain");
        assertEquals(404, response.getStatus());
    }

    @Test
    void testSimpleHeadGet() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headGet("/simple/plain", c -> {
                    c.setContentType("text/plain");
                    if (c.request().getMethod() != RequestMethod.HEAD) {
                        c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                    }
                });
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/plain");
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());

        response = conversation.doRequest("http://localhost/simple/plain",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("", response.getText());
    }

    @Test
    void testSimpleHeadPost() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headPost("/simple/plain", c -> {
                    c.setContentType("text/plain");
                    if (c.request().getMethod() != RequestMethod.HEAD) {
                        c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                    }
                });
            }
        });

        var response = conversation.doRequest("http://localhost/simple/plain",
            new MockRequest().method(RequestMethod.POST));
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());

        response = conversation.doRequest("http://localhost/simple/plain",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("", response.getText());
    }

    @Test
    void testSimpleHeadGetPost() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headGetPost("/simple/plain", c -> {
                    c.setContentType("text/plain");
                    if (c.request().getMethod() != RequestMethod.HEAD) {
                        c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                    }
                });
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/plain");
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());

        response = conversation.doRequest("http://localhost/simple/plain",
            new MockRequest().method(RequestMethod.POST));
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:", response.getText());

        response = conversation.doRequest("http://localhost/simple/plain",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals("", response.getText());
    }

    @Test
    void testSimplePathInfo() {
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
    void testSimpleHeadGetPathInfo() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headGet("/simple/pathinfo", PathInfoHandling.CAPTURE,
                    c -> {
                        if (c.request().getMethod() != RequestMethod.HEAD) {
                            c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                        }
                    });
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/pathinfo/some/path");
        assertEquals(200, response.getStatus());
        assertEquals("Just some text 127.0.0.1:some/path", response.getText());

        response = conversation.doRequest("http://localhost/simple/pathinfo/some/path",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("", response.getText());
    }

    @Test
    void testSimpleHeadPostPathInfo() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headPost("/simple/pathinfo", PathInfoHandling.CAPTURE,
                    c -> {
                        if (c.request().getMethod() != RequestMethod.HEAD) {
                            c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                        }
                    });
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/pathinfo/some/path",
            new MockRequest().method(RequestMethod.POST));
        assertEquals(200, response.getStatus());
        assertEquals("Just some text 127.0.0.1:some/path", response.getText());

        response = conversation.doRequest("http://localhost/simple/pathinfo/some/path",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("", response.getText());
    }

    @Test
    void testSimpleHeadGetPostPathInfo() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headGetPost("/simple/pathinfo", PathInfoHandling.CAPTURE,
                    c -> {
                        if (c.request().getMethod() != RequestMethod.HEAD) {
                            c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                        }
                    });
            }
        });

        MockResponse response = conversation.doRequest("http://localhost/simple/pathinfo/some/path");
        assertEquals(200, response.getStatus());
        assertEquals("Just some text 127.0.0.1:some/path", response.getText());

        response = conversation.doRequest("http://localhost/simple/pathinfo/some/path",
            new MockRequest().method(RequestMethod.POST));
        assertEquals(200, response.getStatus());
        assertEquals("Just some text 127.0.0.1:some/path", response.getText());

        response = conversation.doRequest("http://localhost/simple/pathinfo/some/path",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("", response.getText());
    }

    @Test
    void testPathInfoMapping() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/pathinfo/map", PathInfoHandling.MAP(m -> m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")), c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                    c.print(":" + c.parameter("param1"));
                    c.print(":" + c.parameter("param2"));
                });
            }
        });

        MockResponse response;

        response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:text/val1/x4321:val1:4321", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/ddd");
        assertEquals(404, response.getStatus());
    }

    @Test
    void testHeadGetPathInfoMapping() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headGet("/pathinfo/map", PathInfoHandling.MAP(m ->
                        m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")),
                    c -> {
                        c.setContentType("text/html");
                        if (c.request().getMethod() != RequestMethod.HEAD) {
                            c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                            c.print(":" + c.parameter("param1"));
                            c.print(":" + c.parameter("param2"));
                        }
                    });
            }
        });

        var response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:text/val1/x4321:val1:4321", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/ddd");
        assertEquals(404, response.getStatus());
    }

    @Test
    void testHeadPostPathInfoMapping() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headGetPost("/pathinfo/map", PathInfoHandling.MAP(m ->
                        m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")),
                    c -> {
                        c.setContentType("text/html");
                        if (c.request().getMethod() != RequestMethod.HEAD) {
                            c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                            c.print(":" + c.parameter("param1"));
                            c.print(":" + c.parameter("param2"));
                        }
                    });
            }
        });

        var response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321",
            new MockRequest().method(RequestMethod.POST));
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:text/val1/x4321:val1:4321", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/ddd");
        assertEquals(404, response.getStatus());
    }

    @Test
    void testHeadGetPostPathInfoMapping() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var ignore = headGetPost("/pathinfo/map", PathInfoHandling.MAP(
                        m -> m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")),
                    c -> {
                        if (c.request().getMethod() != RequestMethod.HEAD) {
                            c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                            c.print(":" + c.parameter("param1"));
                            c.print(":" + c.parameter("param2"));
                        }
                    });
            }
        });

        var response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:text/val1/x4321:val1:4321", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321",
            new MockRequest().method(RequestMethod.POST));
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:text/val1/x4321:val1:4321", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321",
            new MockRequest().method(RequestMethod.HEAD));
        assertEquals(200, response.getStatus());
        assertEquals("", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/ddd");
        assertEquals(404, response.getStatus());
    }

    @Test
    void testPathInfoMappingMultiple() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/pathinfo/map",
                    PathInfoHandling.MAP(
                        m -> m.t("text").s().p("param1"),
                        m -> m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")
                    ), c -> {
                        c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                        c.print(":" + c.parameter("param1"));
                        c.print(":" + c.parameter("param2"));
                    });
            }
        });

        MockResponse response;

        response = conversation.doRequest("http://localhost/pathinfo/map/text/val1/x4321");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:text/val1/x4321:val1:4321", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/text/val1");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:text/val1:val1:null", response.getText());

        response = conversation.doRequest("http://localhost/pathinfo/map/ddd");
        assertEquals(404, response.getStatus());
    }

    @Test
    void testPathInfoMappingUrlGeneration() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                var path_info = get("/pathinfo/map", PathInfoHandling.MAP(m -> m.t("text").s().p("param1").s().t("x").p("param2", "\\d+")), c -> {
                    c.print("Just some text " + c.remoteAddr() + ":" + c.pathInfo());
                    c.print(":" + c.parameter("param1"));
                    c.print(":" + c.parameter("param2"));
                });
                get("/", c ->
                    c.print(c.urlFor(path_info).param("param1", "v1").param("param2", "412"))
                );
            }
        });

        MockResponse response;

        response = conversation.doRequest("http://localhost/");
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("http://localhost/pathinfo/map/text/v1/x412", response.getText());

        response = conversation.doRequest(response.getText());
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals("Just some text 127.0.0.1:text/v1/x412:v1:412", response.getText());
    }

    @Test
    void testHeaders() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/headers", c -> {
                    c.addHeader("Content-Disposition", "attachment; filename=thefile.zip");
                    var cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    cal.set(2002, Calendar.OCTOBER, 25, 19, 20, 58);
                    c.addDateHeader("DateHeader", cal.getTimeInMillis());
                    c.addHeader("IntHeader", 1212);

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
    void testWrongServerRootUrl() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/simple/html", c -> c.print("Just some text " + c.remoteAddr() + ":" + c.remoteHost() + ":" + c.pathInfo()));
            }
        });

        assertNull(conversation.doRequest("http://10.0.0.1/simple/html"));
    }

    @Test
    void testCookies() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/cookies1", c -> {
                    if (c.hasCookie("cookie1") &&
                        c.hasCookie("cookie2") &&
                        c.hasCookie("cookie3")) {
                        c.addCookie(new CookieBuilder("cookie3", c.cookieValue("cookie1")));
                        c.addCookie(new CookieBuilder("cookie4", c.cookieValue("cookie2")));
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
        assertEquals("this is the first cookie", conversation.getCookie("cookie3").getValue());
        assertEquals("this is the second cookie", conversation.getCookie("cookie4").getValue());

        // new page with cookie context
        conversation.cookie("cookie4", "this is the fourth cookie");
        response = conversation.doRequest("/cookies2");
        assertEquals("this is the second cookie,this is the first cookie,this is the fourth cookie", response.getText());
    }

    @Test
    void testContentlength() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/contentlength", c -> {
                    var out = "this goes out";
                    c.setContentLength(out.length());
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
    void testDynamicContenttype() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/dynamiccontenttype", c -> {
                    switch (c.parameter("switch")) {
                        case "text" -> c.setContentType("text/plain");
                        case "html" -> c.setContentType("text/html");
                    }
                });
            }
        });

        assertEquals("text/plain; charset=UTF-8", conversation.doRequest("/dynamiccontenttype?switch=text").getContentType());
        assertEquals("text/html; charset=UTF-8", conversation.doRequest("/dynamiccontenttype?switch=html").getContentType());
    }

    @Test
    void testBinary() {
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
    void testPrintAndWriteBuffer() {
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
    void testPrintAndWriteNoBuffer() {
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
    void testGenerateForm() {
        var conversation = new MockConversation(new GenerateFormSite());
        assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_values").getContent(), conversation.doRequest("/form").getText());
        assertEquals(TemplateFactory.HTML.get("formbuilder_fields").getContent(), conversation.doRequest("/form?remove=1").getText());
    }

    @Test
    void testGenerateFormPrefix() {
        var conversation = new MockConversation(new GenerateFormSite());
        assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_values").getContent(), conversation.doRequest("/form?prefix=1").getText());
        assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix").getContent(), conversation.doRequest("/form?prefix=1&remove=1").getText());
    }

    @Test
    void testGenerateEmptyForm() {
        var conversation = new MockConversation(new GenerateEmptyFormSite());
        assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_empty").getContent(), conversation.doRequest("/form_empty").getText());
        assertEquals(TemplateFactory.HTML.get("formbuilder_fields").getContent(), conversation.doRequest("/form_empty?remove=1").getText());
    }

    @Test
    void testGenerateEmptyFormPrefix() {
        var conversation = new MockConversation(new GenerateEmptyFormSite());
        assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_empty").getContent(), conversation.doRequest("/form_empty?prefix=1").getText());
        assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix").getContent(), conversation.doRequest("/form_empty?prefix=1&remove=1").getText());
    }


    @Test
    void testFallbacks() {
        var conversation = new MockConversation(new FallbacksSite());

        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());
        assertEquals("fallback1", conversation.doRequest("/ones").getText());
        assertEquals("/two", conversation.doRequest("/two/info").getText());
        assertEquals("fallback1", conversation.doRequest("/twos").getText());

        assertEquals("fallback1", conversation.doRequest("/prefix1").getText());
        assertEquals("/prefix1/three", conversation.doRequest("/prefix1/three").getText());
        assertEquals("fallback1", conversation.doRequest("/prefix1/threes").getText());

        assertEquals("fallback2", conversation.doRequest("/prefix1/prefix2").getText());
        assertEquals("/prefix1/prefix2/four", conversation.doRequest("/prefix1/prefix2/four").getText());
        assertEquals("fallback2", conversation.doRequest("/prefix1/prefix2/fours").getText());

        assertEquals("fallback4", conversation.doRequest("/prefix1/prefix2/prefix3").getText());
        assertEquals("/prefix1/prefix2/prefix3/five", conversation.doRequest("/prefix1/prefix2/prefix3/five").getText());
        assertEquals("/prefix1/prefix2/prefix3/five", conversation.doRequest("/prefix1/prefix2/prefix3/five/info").getText());
        assertEquals("fallback4", conversation.doRequest("/prefix1/prefix2/prefix3/fives").getText());

        assertEquals("/prefix1/prefix2/six", conversation.doRequest("/prefix1/prefix2/six").getText());
        assertEquals("fallback2", conversation.doRequest("/prefix1/prefix2/sixs").getText());

        assertEquals("/seven", conversation.doRequest("/seven").getText());
        assertEquals("fallback1", conversation.doRequest("/sevens").getText());
    }
}
