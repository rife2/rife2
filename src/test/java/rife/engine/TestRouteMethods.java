/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.test.MockConversation;
import rife.test.MockRequest;

import static org.junit.jupiter.api.Assertions.*;
import static rife.engine.RequestMethod.*;

public class TestRouteMethods {
    public static class MethodElement implements Element {
        public void process(Context c) {
            c.print("element " + c.method() + ":" + c.pathInfo());
        }
    }

    static class MethodsSite extends Site {
        public void setup() {
            route("/lambda", new RequestMethod[]{PUT, OPTIONS}, c -> c.print("lambda " + c.method()));
            route("/lambda-info", PathInfoHandling.CAPTURE, new RequestMethod[]{OPTIONS}, c -> c.print("lambda " + c.method() + ":" + c.pathInfo()));
            route("/class", new RequestMethod[]{PUT}, MethodElement.class);
            route("/class-info", PathInfoHandling.CAPTURE, new RequestMethod[]{PUT}, MethodElement.class);
            route("/supplier", new RequestMethod[]{PUT}, MethodElement::new);
            route("/supplier-info", PathInfoHandling.CAPTURE, new RequestMethod[]{PUT}, MethodElement::new);
        }
    }

    private static String text(MockConversation conversation, String url, RequestMethod method) {
        var response = conversation.doRequest(url, new MockRequest().method(method));
        return response.getStatus() + " " + response.getText();
    }

    @Test
    void testLambdaElements() {
        var conversation = new MockConversation(new MethodsSite());
        assertEquals("200 lambda PUT", text(conversation, "/lambda", PUT));
        assertEquals("200 lambda OPTIONS", text(conversation, "/lambda", OPTIONS));
        assertEquals(404, conversation.doRequest("/lambda").getStatus());
        assertEquals("200 lambda OPTIONS:a/b", text(conversation, "/lambda-info/a/b", OPTIONS));
        assertEquals(404, conversation.doRequest("/lambda-info/a/b", new MockRequest().method(PUT)).getStatus());
    }

    @Test
    void testClassAndSupplierElements() {
        var conversation = new MockConversation(new MethodsSite());
        for (var prefix : new String[]{"/class", "/supplier"}) {
            assertEquals("200 element PUT:", text(conversation, prefix, PUT));
            assertEquals(404, conversation.doRequest(prefix).getStatus());
            assertEquals("200 element PUT:x", text(conversation, prefix + "-info/x", PUT));
        }
    }
}
