/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import org.junit.jupiter.api.Test;
import rife.engine.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestMocksRouting {
    @Test
    void testRoutingGet() {
        var conversation = new MockConversation(new RoutingGetSite());
        
        assertEquals("class GetElement",                        conversation.doRequest("/routingGetSite_GetElement", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetPathInfoElement:pathinfo",       conversation.doRequest("/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetElement",                        conversation.doRequest("/get3", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetPathInfoElement:otherpathinfo",  conversation.doRequest("/get4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("get element",                             conversation.doRequest("/get5", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("get element path info:differentpathinfo", conversation.doRequest("/get6/differentpathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetElement",                        conversation.doRequest("/supplier/routingGetSite_GetElement", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetPathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetElement",                        conversation.doRequest("/supplier/get3", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetPathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/get4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getText());

        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get5", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get6/differentpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetElement", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get3", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get5", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get6/differentpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetElement", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get3", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetElement", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get3", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get5", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get6/differentpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetElement", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get3", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetElement", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get3", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get5", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get6/differentpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetElement", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get3", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get5", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get6/differentpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get5", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get6/differentpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetElement", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get3", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get5", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/get6/differentpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetElement", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingGetSite_GetPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get3", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/get4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
    }


    @Test
    void testRoutingPost() {
        var conversation = new MockConversation(new RoutingPostSite());

        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostElement", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post3", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post5", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post6/differentpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostElement", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post3", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post5", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post6/differentpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());

        assertEquals("class PostElement",                        conversation.doRequest("/routingPostSite_PostElement", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostPathInfoElement:pathinfo",       conversation.doRequest("/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostElement",                        conversation.doRequest("/post3", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostPathInfoElement:otherpathinfo",  conversation.doRequest("/post4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("post element",                             conversation.doRequest("/post5", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("post element path info:differentpathinfo", conversation.doRequest("/post6/differentpathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostElement",                        conversation.doRequest("/supplier/routingPostSite_PostElement", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostPathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostElement",                        conversation.doRequest("/supplier/post3", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostPathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/post4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getText());

        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostElement", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post3", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post5", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post6/differentpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostElement", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post3", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostElement", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post3", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post5", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post6/differentpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostElement", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post3", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post5", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post6/differentpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post5", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post6/differentpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostElement", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post3", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post5", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/post6/differentpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostElement", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPostSite_PostPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post3", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/post4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
    }

    @Test
    void testRoutingPut() {
        var conversation = new MockConversation(new RoutingPutSite());

        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutElement", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put3", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put5", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put6/differentpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutElement", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put3", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put5", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put6/differentpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutElement", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put3", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put5", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put6/differentpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutElement", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put3", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());

        assertEquals("class PutElement",                        conversation.doRequest("/routingPutSite_PutElement", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutPathInfoElement:pathinfo",       conversation.doRequest("/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutElement",                        conversation.doRequest("/put3", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutPathInfoElement:otherpathinfo",  conversation.doRequest("/put4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("put element",                             conversation.doRequest("/put5", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("put element path info:differentpathinfo", conversation.doRequest("/put6/differentpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutElement",                        conversation.doRequest("/supplier/routingPutSite_PutElement", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutPathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutElement",                        conversation.doRequest("/supplier/put3", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutPathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/put4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());

        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutElement", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put3", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put5", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put6/differentpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutElement", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put3", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put5", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put6/differentpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put5", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put6/differentpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutElement", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put3", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put5", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/put6/differentpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutElement", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPutSite_PutPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put3", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/put4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
    }

    @Test
    void testRoutingDelete() {
        var conversation = new MockConversation(new RoutingDeleteSite());

        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete3", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete5", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete6/differentpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete3", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete5", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete6/differentpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete3", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete5", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete6/differentpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete3", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete3", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete5", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete6/differentpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete3", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());

        assertEquals("class DeleteElement",                        conversation.doRequest("/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeletePathInfoElement:pathinfo",       conversation.doRequest("/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeleteElement",                        conversation.doRequest("/delete3", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeletePathInfoElement:otherpathinfo",  conversation.doRequest("/delete4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("delete element",                             conversation.doRequest("/delete5", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("delete element path info:differentpathinfo", conversation.doRequest("/delete6/differentpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeleteElement",                        conversation.doRequest("/supplier/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeletePathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeleteElement",                        conversation.doRequest("/supplier/delete3", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeletePathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/delete4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());

        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete5", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete6/differentpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete5", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete6/differentpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete3", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete5", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/delete6/differentpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeleteElement", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingDeleteSite_DeletePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete3", new MockRequest().method(RequestMethod.PATCH)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/delete4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getStatus());
    }

    @Test
    void testRoutingPatch() {
        var conversation = new MockConversation(new RoutingPatchSite());

        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch3", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch5", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch6/differentpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch3", new MockRequest().method(RequestMethod.GET)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch5", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch6/differentpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch3", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch5", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch6/differentpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch3", new MockRequest().method(RequestMethod.POST)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch3", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch5", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch6/differentpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch3", new MockRequest().method(RequestMethod.PUT)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch3", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch5", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch6/differentpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch3", new MockRequest().method(RequestMethod.DELETE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch5", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch6/differentpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch3", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getStatus());

        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch5", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/patch6/differentpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/supplier/patch4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());

        assertEquals("class PatchElement",                        conversation.doRequest("/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchPathInfoElement:pathinfo",       conversation.doRequest("/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchElement",                        conversation.doRequest("/patch3", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchPathInfoElement:otherpathinfo",  conversation.doRequest("/patch4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("patch element",                             conversation.doRequest("/patch5", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("patch element path info:differentpathinfo", conversation.doRequest("/patch6/differentpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchElement",                        conversation.doRequest("/supplier/routingPatchSite_PatchElement", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchPathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingPatchSite_PatchPathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchElement",                        conversation.doRequest("/supplier/patch3", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchPathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/patch4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
    }

    @Test
    void testRoutingRoute() {
        var conversation = new MockConversation(new RoutingRouteSite());

        assertEquals("class RouteElement",                        conversation.doRequest("/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/route3", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/route4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("route element",                             conversation.doRequest("/route5", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("route element path info:differentpathinfo", conversation.doRequest("/route6/differentpathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/route3", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/route4/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getText());

        assertEquals(200, conversation.doRequest("/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/route3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/route4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/route5", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/route6/differentpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/supplier/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/supplier/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/supplier/route3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertEquals(200, conversation.doRequest("/supplier/route4/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());

        assertEquals("class RouteElement",                        conversation.doRequest("/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/route3", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/route4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("route element",                             conversation.doRequest("/route5", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("route element path info:differentpathinfo", conversation.doRequest("/route6/differentpathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/route3", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/route4/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getText());

        assertEquals("class RouteElement",                        conversation.doRequest("/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/route3", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/route4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("route element",                             conversation.doRequest("/route5", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("route element path info:differentpathinfo", conversation.doRequest("/route6/differentpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/route3", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/route4/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());

        assertEquals("class RouteElement",                        conversation.doRequest("/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/route3", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/route4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("route element",                             conversation.doRequest("/route5", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("route element path info:differentpathinfo", conversation.doRequest("/route6/differentpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/route3", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/route4/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());

        assertEquals("class RouteElement",                        conversation.doRequest("/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/route3", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/route4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("route element",                             conversation.doRequest("/route5", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("route element path info:differentpathinfo", conversation.doRequest("/route6/differentpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/route3", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/route4/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getText());

        assertEquals("class RouteElement",                        conversation.doRequest("/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/route3", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/route4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("route element",                             conversation.doRequest("/route5", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("route element path info:differentpathinfo", conversation.doRequest("/route6/differentpathinfo", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/route3", new MockRequest().method(RequestMethod.TRACE)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/route4/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getText());

        assertEquals("class RouteElement",                        conversation.doRequest("/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/route3", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/route4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("route element",                             conversation.doRequest("/route5", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("route element path info:differentpathinfo", conversation.doRequest("/route6/differentpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/routingRouteSite_RouteElement", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class RoutePathInfoElement:pathinfo",       conversation.doRequest("/supplier/routingRouteSite_RoutePathInfoElement/pathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class RouteElement",                        conversation.doRequest("/supplier/route3", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class RoutePathInfoElement:otherpathinfo",  conversation.doRequest("/supplier/route4/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
    }

    @Test
    void testRoutingCombo() {
        var conversation = new MockConversation(new RoutingComboSite());

        assertEquals("class GetElement",                        conversation.doRequest("/combo1", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetPathInfoElement:otherpathinfo",  conversation.doRequest("/combo2/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("get element",                             conversation.doRequest("/combo3", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("get element path info:differentpathinfo", conversation.doRequest("/combo4/differentpathinfo", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetElement",                        conversation.doRequest("/combo5", new MockRequest().method(RequestMethod.GET)).getText());
        assertEquals("class GetPathInfoElement:otherpathinfo",  conversation.doRequest("/combo6/otherpathinfo", new MockRequest().method(RequestMethod.GET)).getText());

        assertNotEquals(200, conversation.doRequest("/combo1", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo2/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo3", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo4/differentpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo5", new MockRequest().method(RequestMethod.HEAD)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo6/otherpathinfo", new MockRequest().method(RequestMethod.HEAD)).getStatus());

        assertEquals("class PostElement",                        conversation.doRequest("/combo1", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostPathInfoElement:otherpathinfo",  conversation.doRequest("/combo2/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("post element",                             conversation.doRequest("/combo3", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("post element path info:differentpathinfo", conversation.doRequest("/combo4/differentpathinfo", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostElement",                        conversation.doRequest("/combo5", new MockRequest().method(RequestMethod.POST)).getText());
        assertEquals("class PostPathInfoElement:otherpathinfo",  conversation.doRequest("/combo6/otherpathinfo", new MockRequest().method(RequestMethod.POST)).getText());

        assertEquals("class PutElement",                        conversation.doRequest("/combo1", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutPathInfoElement:otherpathinfo",  conversation.doRequest("/combo2/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("put element",                             conversation.doRequest("/combo3", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("put element path info:differentpathinfo", conversation.doRequest("/combo4/differentpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutElement",                        conversation.doRequest("/combo5", new MockRequest().method(RequestMethod.PUT)).getText());
        assertEquals("class PutPathInfoElement:otherpathinfo",  conversation.doRequest("/combo6/otherpathinfo", new MockRequest().method(RequestMethod.PUT)).getText());

        assertEquals("class DeleteElement",                        conversation.doRequest("/combo1", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeletePathInfoElement:otherpathinfo",  conversation.doRequest("/combo2/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("delete element",                             conversation.doRequest("/combo3", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("delete element path info:differentpathinfo", conversation.doRequest("/combo4/differentpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeleteElement",                        conversation.doRequest("/combo5", new MockRequest().method(RequestMethod.DELETE)).getText());
        assertEquals("class DeletePathInfoElement:otherpathinfo",  conversation.doRequest("/combo6/otherpathinfo", new MockRequest().method(RequestMethod.DELETE)).getText());

        assertNotEquals(200, conversation.doRequest("/combo1", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertNotEquals(200, conversation.doRequest("/combo2/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertNotEquals(200, conversation.doRequest("/combo3", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertNotEquals(200, conversation.doRequest("/combo4/differentpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertNotEquals(200, conversation.doRequest("/combo5", new MockRequest().method(RequestMethod.OPTIONS)).getText());
        assertNotEquals(200, conversation.doRequest("/combo6/otherpathinfo", new MockRequest().method(RequestMethod.OPTIONS)).getText());

        assertNotEquals(200, conversation.doRequest("/combo1", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo2/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo3", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo4/differentpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo5", new MockRequest().method(RequestMethod.TRACE)).getStatus());
        assertNotEquals(200, conversation.doRequest("/combo6/otherpathinfo", new MockRequest().method(RequestMethod.TRACE)).getStatus());

        assertEquals("class PatchElement",                        conversation.doRequest("/combo1", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchPathInfoElement:otherpathinfo",  conversation.doRequest("/combo2/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("patch element",                             conversation.doRequest("/combo3", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("patch element path info:differentpathinfo", conversation.doRequest("/combo4/differentpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchElement",                        conversation.doRequest("/combo5", new MockRequest().method(RequestMethod.PATCH)).getText());
        assertEquals("class PatchPathInfoElement:otherpathinfo",  conversation.doRequest("/combo6/otherpathinfo", new MockRequest().method(RequestMethod.PATCH)).getText());
    }
}
