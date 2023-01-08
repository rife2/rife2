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
    @Test
    void testRoutingGet()
    throws IOException {
        try (final var server = new TestServerRunner(new RoutingGetSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertEquals("class GetElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetElement"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class GetPathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetPathInfoElement/pathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class GetElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class GetPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("get element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("get element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetElement"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetPathInfoElement/pathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetElement"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetPathInfoElement/pathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetElement"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetPathInfoElement/pathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetElement"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetPathInfoElement/pathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());

                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetElement"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetPathInfoElement/pathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetElement"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetPathInfoElement/pathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetElement"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetSite_GetPathInfoElement/pathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get3"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get4/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get5"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get6/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
            }
        }
    }

    @Test
    void testRoutingPost()
    throws IOException {
        try (final var server = new TestServerRunner(new RoutingPostSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostElement"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostPathInfoElement/pathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post3"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post4/otherpathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post5"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post6/differentpathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostElement"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostPathInfoElement/pathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post4/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post5"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post6/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertEquals("class PostElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostElement"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class PostPathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostPathInfoElement/pathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class PostElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/post3"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class PostPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/post4/otherpathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("post element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/post5"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("post element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/post6/differentpathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostElement"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostPathInfoElement/pathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post3"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post4/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post5"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post6/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostElement"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostPathInfoElement/pathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post3"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post4/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post5"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post6/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());

                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostElement"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostPathInfoElement/pathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/post3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/post4/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/post5"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/post6/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostElement"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostPathInfoElement/pathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post3"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post4/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post5"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post6/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostElement"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPostSite_PostPathInfoElement/pathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post3"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post4/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post5"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/post6/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
            }
        }
    }

    @Test
    void testRoutingGetPost()
    throws IOException {
        try (final var server = new TestServerRunner(new RoutingGetPostSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertEquals("class GetPostElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostElement"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class GetPostPathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostPathInfoElement/pathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class GetPostElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost3"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class GetPostPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost4/otherpathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("getPost element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost5"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("getPost element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost6/differentpathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostElement"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostPathInfoElement/pathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost4/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost5"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost6/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertEquals("class GetPostElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostElement"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class GetPostPathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostPathInfoElement/pathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class GetPostElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost3"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class GetPostPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost4/otherpathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("getPost element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost5"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("getPost element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost6/differentpathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostElement"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostPathInfoElement/pathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost3"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost4/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost5"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost6/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostElement"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostPathInfoElement/pathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost3"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost4/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost5"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost6/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());

                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostElement"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostPathInfoElement/pathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost4/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost5"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost6/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostElement"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostPathInfoElement/pathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost3"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost4/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost5"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost6/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostElement"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingGetPostSite_GetPostPathInfoElement/pathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost3"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost4/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost5"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/getPost6/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
            }
        }
    }

    @Test
    void testRoutingPut()
    throws IOException {
        try (final var server = new TestServerRunner(new RoutingPutSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutElement"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutPathInfoElement/pathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put3"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put4/otherpathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put5"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put6/differentpathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutElement"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutPathInfoElement/pathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put4/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put5"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put6/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutElement"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutPathInfoElement/pathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put3"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put4/otherpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put5"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put6/differentpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());

                assertEquals("class PutElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutElement"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("class PutPathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutPathInfoElement/pathinfo"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("class PutElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/put3"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("class PutPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/put4/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("put element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/put5"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("put element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/put6/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutElement"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutPathInfoElement/pathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put3"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put4/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put5"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put6/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());

                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutElement"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutPathInfoElement/pathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/put3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/put4/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/put5"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/put6/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutElement"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutPathInfoElement/pathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put3"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put4/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put5"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put6/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutElement"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPutSite_PutPathInfoElement/pathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put3"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put4/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put5"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/put6/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
            }
        }
    }

    @Test
    void testRoutingDelete()
    throws IOException {
        try (final var server = new TestServerRunner(new RoutingDeleteSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeleteElement"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeletePathInfoElement/pathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete3"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete4/otherpathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete5"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete6/differentpathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeleteElement"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeletePathInfoElement/pathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete4/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete5"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete6/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeleteElement"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeletePathInfoElement/pathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete3"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete4/otherpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete5"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete6/differentpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeleteElement"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeletePathInfoElement/pathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete3"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete4/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete5"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete6/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());

                assertEquals("class DeleteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeleteElement"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("class DeletePathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeletePathInfoElement/pathinfo"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("class DeleteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete3"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("class DeletePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete4/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("delete element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete5"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("delete element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete6/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getContentAsString());

                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeleteElement"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeletePathInfoElement/pathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete4/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete5"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete6/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeleteElement"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeletePathInfoElement/pathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete3"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete4/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete5"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete6/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeleteElement"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingDeleteSite_DeletePathInfoElement/pathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete3"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete4/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete5"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/delete6/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getStatusCode());
            }
        }
    }

    @Test
    void testRoutingPatch()
    throws IOException {
        try (final var server = new TestServerRunner(new RoutingPatchSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchElement"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchPathInfoElement/pathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch3"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch4/otherpathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch5"), HttpMethod.GET)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch6/differentpathinfo"), HttpMethod.GET)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchElement"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchPathInfoElement/pathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch4/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch5"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch6/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchElement"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchPathInfoElement/pathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch3"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch4/otherpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch5"), HttpMethod.POST)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch6/differentpathinfo"), HttpMethod.POST)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchElement"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchPathInfoElement/pathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch3"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch4/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch5"), HttpMethod.PUT)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch6/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getStatusCode());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchElement"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchPathInfoElement/pathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch3"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch4/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch5"), HttpMethod.DELETE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch6/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getStatusCode());

                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchElement"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchPathInfoElement/pathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch4/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch5"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch6/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchElement"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchPathInfoElement/pathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch3"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch4/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch5"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch6/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());

                assertEquals("class PatchElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchElement"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("class PatchPathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingPatchSite_PatchPathInfoElement/pathinfo"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("class PatchElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch3"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("class PatchPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch4/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("patch element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch5"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("patch element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/patch6/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testRoutingRoute()
    throws IOException {
        try (final var server = new TestServerRunner(new RoutingRouteSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RouteElement"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RoutePathInfoElement/pathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/route3"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/route4/otherpathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("route element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/route5"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("route element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/route6/differentpathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());

                assertEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RouteElement"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RoutePathInfoElement/pathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/route3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/route4/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/route5"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/route6/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RouteElement"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RoutePathInfoElement/pathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/route3"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/route4/otherpathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("route element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/route5"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("route element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/route6/differentpathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());

                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RouteElement"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RoutePathInfoElement/pathinfo"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/route3"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/route4/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("route element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/route5"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("route element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/route6/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getContentAsString());

                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RouteElement"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RoutePathInfoElement/pathinfo"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/route3"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/route4/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("route element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/route5"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("route element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/route6/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getContentAsString());

                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RouteElement"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RoutePathInfoElement/pathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/route3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/route4/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("route element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/route5"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("route element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/route6/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RouteElement"), HttpMethod.TRACE)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RoutePathInfoElement/pathinfo"), HttpMethod.TRACE)).getWebResponse().getContentAsString());
                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/route3"), HttpMethod.TRACE)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/route4/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getContentAsString());
                assertEquals("route element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/route5"), HttpMethod.TRACE)).getWebResponse().getContentAsString());
                assertEquals("route element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/route6/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getContentAsString());

                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RouteElement"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:pathinfo",       webClient.getPage(new WebRequest(new URL("http://localhost:8181/routingRouteSite_RoutePathInfoElement/pathinfo"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("class RouteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/route3"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("class RoutePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/route4/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("route element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/route5"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("route element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/route6/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testRoutingCombo()
    throws IOException {
        try (final var server = new TestServerRunner(new RoutingComboSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);

                assertEquals("class GetElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo1"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("class GetPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo2/otherpathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("get element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo3"), HttpMethod.GET)).getWebResponse().getContentAsString());
                assertEquals("get element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo4/differentpathinfo"), HttpMethod.GET)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo1"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo2/otherpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo3"), HttpMethod.HEAD)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo4/differentpathinfo"), HttpMethod.HEAD)).getWebResponse().getStatusCode());

                assertEquals("class PostElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo1"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("class PostPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo2/otherpathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("post element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo3"), HttpMethod.POST)).getWebResponse().getContentAsString());
                assertEquals("post element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo4/differentpathinfo"), HttpMethod.POST)).getWebResponse().getContentAsString());

                assertEquals("class PutElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo1"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("class PutPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo2/otherpathinfo"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("put element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo3"), HttpMethod.PUT)).getWebResponse().getContentAsString());
                assertEquals("put element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo4/differentpathinfo"), HttpMethod.PUT)).getWebResponse().getContentAsString());

                assertEquals("class DeleteElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo1"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("class DeletePathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo2/otherpathinfo"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("delete element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo3"), HttpMethod.DELETE)).getWebResponse().getContentAsString());
                assertEquals("delete element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo4/differentpathinfo"), HttpMethod.DELETE)).getWebResponse().getContentAsString());

                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo1"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo2/otherpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo3"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());
                assertEquals("", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo4/differentpathinfo"), HttpMethod.OPTIONS)).getWebResponse().getContentAsString());

                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo1"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo2/otherpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo3"), HttpMethod.TRACE)).getWebResponse().getStatusCode());
                assertNotEquals(200, webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo4/differentpathinfo"), HttpMethod.TRACE)).getWebResponse().getStatusCode());

                assertEquals("class PatchElement",                        webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo1"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("class PatchPathInfoElement:otherpathinfo",  webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo2/otherpathinfo"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("patch element",                             webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo3"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
                assertEquals("patch element path info:differentpathinfo", webClient.getPage(new WebRequest(new URL("http://localhost:8181/combo4/differentpathinfo"), HttpMethod.PATCH)).getWebResponse().getContentAsString());
            }
        }
    }
}
