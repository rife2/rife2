/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestProcessedTemplates {
    @Test
    void testTemplatePropertiesHtml()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                properties().put("prop1", "value1");
                get("/template/html", c -> c.print(c.template("filtered_tags_property")));
            }
        })) {
            server.properties().put("another_property", "value2");
            try (final var webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/template/html");
                var response = page.getWebResponse();
                assertEquals("text/html", response.getContentType());
                assertEquals("This is a property value 'value1'.\nThis is another property value 'value2'.\n", response.getContentAsString());
            }
        }
    }

    @Test
    void testTemplatePropertiesTxt()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                properties().put("prop1", "value1");
                get("/template/txt", c -> c.print(c.templateTxt("filtered_tags_property")));
            }
        })) {
            server.properties().put("another_property", "value2");
            try (final var webClient = new WebClient()) {
                final TextPage page = webClient.getPage("http://localhost:8181/template/txt");
                var response = page.getWebResponse();
                assertEquals("text/plain", response.getContentType());
                assertEquals("This is a property value 'value1'.\nThis is another property value 'value2'.\n", response.getContentAsString());
            }
        }
    }

    @Test
    void testTemplateAttributesHtml()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                before(c -> {
                    c.setAttribute("attribute1", "value1");
                    c.setAttribute("another_attribute", "value2");
                });
                get("/template/html", c -> c.print(c.template("filtered_tags_attribute")));
            }
        })) {
            try (final var webClient = new WebClient()) {
                final HtmlPage page = webClient.getPage("http://localhost:8181/template/html");
                var response = page.getWebResponse();
                assertEquals("text/html", response.getContentType());
                assertEquals("This is an attribute value 'value1'.\n" +
                             "This is another attribute value 'value2'.\n", response.getContentAsString());
            }
        }
    }

    @Test
    void testTemplateAttributesTxt()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                before(c -> {
                    c.setAttribute("attribute1", "value1");
                    c.setAttribute("another_attribute", "value2");
                });
                get("/template/txt", c -> c.print(c.templateTxt("filtered_tags_attribute")));
            }
        })) {
            try (final var webClient = new WebClient()) {
                final TextPage page = webClient.getPage("http://localhost:8181/template/txt");
                var response = page.getWebResponse();
                assertEquals("text/plain", response.getContentType());
                assertEquals("This is an attribute value 'value1'.\n" +
                             "This is another attribute value 'value2'.\n", response.getContentAsString());
            }
        }
    }
}
