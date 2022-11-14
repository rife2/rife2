/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestRouting {
    public static class GetElement implements Element {
        public void process(Context c) {
            c.print("class GetElement");
        }
    }

    public static class GetPathInfoElement implements Element {
        public void process(Context c) {
            c.print("class GetPathInfoElement:" + c.pathInfo());
        }
    }

    @Test
    public void testRoutingGet()
    throws IOException {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get(GetElement.class);
                get(PathInfoHandling.CAPTURE, GetPathInfoElement.class);
                get("/get3", GetElement.class);
                get("/get4", PathInfoHandling.CAPTURE, GetPathInfoElement.class);
                get("/get5", c -> c.print("get element"));
                get("/get6", PathInfoHandling.CAPTURE, c -> c.print("get element path info:" + c.pathInfo()));
            }
        })) {
            try (final WebClient webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertEquals("class GetElement", webClient.getPage("http://localhost:8181/testRouting_GetElement").getWebResponse().getContentAsString());
                assertEquals("class GetPathInfoElement:pathinfo", webClient.getPage("http://localhost:8181/testRouting_GetPathInfoElement/pathinfo").getWebResponse().getContentAsString());
                assertEquals("class GetElement", webClient.getPage("http://localhost:8181/get3").getWebResponse().getContentAsString());
                assertEquals("class GetPathInfoElement:otherpathinfo", webClient.getPage("http://localhost:8181/get4/otherpathinfo").getWebResponse().getContentAsString());
                assertEquals("get element", webClient.getPage("http://localhost:8181/get5").getWebResponse().getContentAsString());
                assertEquals("get element path info:differentpathinfo", webClient.getPage("http://localhost:8181/get6/differentpathinfo").getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetElement"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetPathInfoElement/pathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetElement"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetPathInfoElement/pathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetElement"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetPathInfoElement/pathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetElement"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetPathInfoElement/pathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());

                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetElement"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetPathInfoElement/pathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetElement"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetPathInfoElement/pathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetElement"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/testRouting_GetPathInfoElement/pathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
            }
        }
    }
}
