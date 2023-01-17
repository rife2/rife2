/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import org.junit.jupiter.api.Test;
import rife.test.MockConversation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloTest {
    @Test
    void verifyHelloWorld() {
        var m = new MockConversation(new HelloWorld());
        assertEquals("Hello World", m.doRequest("/hello").getText());
    }

    @Test
    void verifyHelloLink() {
        var m = new MockConversation(new HelloLink());
        assertEquals("Hello World Link", m.doRequest("/link")
            .getParsedHtml().getLinkWithText("Hello")
            .follow().getText());
    }

    @Test
    void verifyHelloGroup() {
        var m = new MockConversation(new HelloGroup());
        assertEquals("before hello inside after", m.doRequest("/group/hello").getText());
        assertEquals("before bonjour inside after", m.doRequest("/group/bonjour").getText());
    }

    @Test
    void verifyHelloTemplate() {
        var m = new MockConversation(new HelloTemplate());
        var t = m.doRequest("/template").getTemplate();
        var link = t.getValue("route:templateHello");
        assertEquals("Hello World Template", m.doRequest(link).getText());
    }

    @Test
    void verifyHelloForm() {
        var m = new MockConversation(new HelloForm());
        var r = m.doRequest("/form").getParsedHtml()
            .getFormWithName("hello").parameter("name", "John").submit();
        assertEquals("Hello John", r.getParsedHtml()
            .getDocument().body()
            .getElementById("greeting").text());
    }

    @Test
    void verifyHelloErrors() {
        var m = new MockConversation(new HelloErrors());
        assertEquals("It's not here!", m.doRequest("/treasure").getText());
        assertEquals("Oh no: the error", m.doRequest("/error").getText());
    }

    @Test
    void verifyHelloPathInfoMapping() {
        var m = new MockConversation(new HelloPathInfoMapping());
        assertEquals("Jimmy", m.doRequest("/mapping/Jimmy").getText());
        assertEquals("Jimmy Joe", m.doRequest("/mapping/Jimmy/Joe").getText());
    }

    @Test
    void verifyHelloFormGeneration() {
        var m = new MockConversation(new HelloFormGeneration());
        var r = m.doRequest("/form").getParsedHtml()
            .getFormWithName("hello").submit();
        assertEquals("mandatory:title<br>mandatory:email<br>mandatory:body<br>", r.getTemplate().getValue("errors:*"));
        assertEquals("""
            <label>Title<br>
                  <input type="text" name="title" size="50" minlength="1" maxlength="120" required="required" />
                </label><br>
                <label>Email<br>
                  <input type="email" name="email" size="50" minlength="1" maxlength="100" required="required" />
                </label><br>
                <label>Body<br>
                  <textarea name="body" cols="50" required="required"></textarea>
                </label><br>""", r.getTemplate().getValue("form-fields").trim());
        var form = r.getParsedHtml().getFormWithName("hello");
        form.setParameter("title", "some title");
        form.setParameter("email", "some@email");
        form.setParameter("body", "some body");
        r = form.submit();
        assertEquals("invalid:email<br>", r.getTemplate().getValue("errors:*"));
        assertEquals("""
            <label>Title<br>
                  <input type="text" name="title" size="50" value="some title" minlength="1" maxlength="120" required="required" />
                </label><br>
                <label>Email<br>
                  <input type="email" name="email" size="50" value="some@email" minlength="1" maxlength="100" required="required" />
                </label><br>
                <label>Body<br>
                  <textarea name="body" cols="50" required="required">some body</textarea>
                </label><br>""", r.getTemplate().getValue("form-fields").trim());
        form = r.getParsedHtml().getFormWithName("hello");
        form.setParameter("email", "some@email.com");
        r = form.submit();
        assertEquals("""
            <h2>Title</h2>
              <p>some title</p>
              <h2>Email</h2>
              <p>some@email.com</p>
              <h2>Body</h2>
              <p>some body</p>""", r.getTemplate().getValue("content").trim());
    }
}