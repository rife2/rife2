/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.template.TemplateFactory;
import rife.test.MockConversation;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestProcessTemplate {
    static class EmbeddingSite extends Site {
        Route edit;

        public void setup() {
            edit = get("/edit", c -> c.print("edit"));
            get("/board", c -> {
                var board = c.template("embedded_board");
                var card = c.template("embedded_card");

                card.setValue("title", "single");
                board.setValue("single", c.processTemplate(card));

                for (var title : List.of("first", "second")) {
                    card.setValue("title", title);
                    board.appendValue("cards", c.processTemplate(card));
                }

                c.print(board);
            });
            get("/content-type", c -> {
                c.processTemplate(TemplateFactory.TXT.get("filtered_tags_attribute"));
                c.print("processed");
            });
            get("/null", c -> {
                assertNull(c.processTemplate(null));
                c.print(null);
                c.printBlock(null, "block");
                c.print("nothing before");
            });
        }
    }

    private static void assertEmbeddedLinks(String text) {
        assertFalse(text.contains("route:"), text);
        var links = Pattern.compile("<a href=\"[^\"]*/edit\">(\\w+)</a>").matcher(text);
        var titles = links.results().map(m -> m.group(1)).toList();
        assertEquals(List.of("single", "first", "second"), titles, text);
    }

    @Test
    void testEmbeddedTemplateValues() {
        var conversation = new MockConversation(new EmbeddingSite());
        assertEmbeddedLinks(conversation.doRequest("/board").getText());
    }

    @Test
    void testEmbeddedTemplateValuesDeployed()
    throws Exception {
        try (final var server = new TestServerRunner(new EmbeddingSite())) {
            var response = java.net.http.HttpClient.newHttpClient().send(
                java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:8181/board")).build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertEmbeddedLinks(response.body());
        }
    }

    @Test
    void testProcessingDoesntSetTheContentType() {
        var response = new MockConversation(new EmbeddingSite()).doRequest("/content-type");
        assertEquals("processed", response.getText());
        assertTrue(response.getContentType().startsWith("text/html"), response.getContentType());
    }

    @Test
    void testPrintingNothing() {
        var response = new MockConversation(new EmbeddingSite()).doRequest("/null");
        assertEquals(200, response.getStatus());
        assertEquals("nothing before", response.getText());
    }
}
