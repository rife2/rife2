/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import org.junit.jupiter.api.Test;
import rife.test.MockConversation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloTest {
    @Test void verifyHelloWorld() {
        var m = new MockConversation(new HelloWorld());
        assertEquals("Hello World", m.doRequest("/hello").getText());
    }

    @Test void verifyHelloLink() {
        var m = new MockConversation(new HelloLink());
        assertEquals("Hello World Link", m.doRequest("/link")
            .getParsedHtml().getLinkWithText("Hello")
            .follow().getText());
    }

    @Test void verifyHelloGroup() {
        var m = new MockConversation(new HelloGroup());
        assertEquals("before hello inside after", m.doRequest("/group/hello").getText());
        assertEquals("before bonjour inside after", m.doRequest("/group/bonjour").getText());
    }

    @Test void verifyHelloTemplate() {
        var m = new MockConversation(new HelloTemplate());
        var t = m.doRequest("/template").getTemplate();
        var link = t.getValue("route:templateHello");
        assertEquals("Hello World Template", m.doRequest(link).getText());
    }

    @Test void verifyHelloForm() {
        var m = new MockConversation(new HelloForm());
        var r = m.doRequest("/form").getParsedHtml()
            .getFormWithName("hello").parameter("name", "John").submit();
        assertEquals("Hello John", r.getParsedHtml()
            .getDocument().body()
            .getElementById("greeting").text());
    }

    @Test void verifyHelloErrors() {
        var m = new MockConversation(new HelloErrors());
        assertEquals("It's not here!", m.doRequest("/treasure").getText());
        assertEquals("Oh no: the error", m.doRequest("/error").getText());
    }

    @Test void verifyHelloPathInfoMapping() {
        var m = new MockConversation(new HelloPathInfoMapping());
        assertEquals("Jimmy", m.doRequest("/mapping/Jimmy").getText());
        assertEquals("Jimmy Joe", m.doRequest("/mapping/Jimmy/Joe").getText());
    }
}