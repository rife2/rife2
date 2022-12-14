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
        assertEquals("Hello World", m.doRequest("/link")
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
        assertEquals("Hello World", m.doRequest("/link")
            .getParsedHtml().getLinkWithText("Hello")
            .follow().getText());
    }

    @Test void verifyHelloForm() {
        var m = new MockConversation(new HelloForm());
        var r = m.doRequest("/hello").getParsedHtml()
            .getFormWithName("hello").submit();
        assertEquals("Hello World", r.getParsedHtml()
            .getDocument().body()
            .getElementById("greeting").text());
    }
}