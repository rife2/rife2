/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.test.MockConversation;
import rife.test.MockRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestOptionalBeanParameters {
    // the idiomatic Optional shape: Optional getters, plain-typed setters
    public static class Book {
        private String title_;
        private Integer year_;

        public Optional<String> getTitle() { return Optional.ofNullable(title_); }
        public void setTitle(String title) { title_ = title; }

        public Optional<Integer> getYear() { return Optional.ofNullable(year_); }
        public void setYear(Integer year) { year_ = year; }
    }

    static class BookSite extends Site {
        public void setup() {
            get("/book", c -> {
                // the request parameters populate the bean transparently,
                // the element does nothing Optional-specific to receive them
                var book = c.parametersBean(Book.class);
                c.print(book.getTitle().orElse("-") + ":" +
                        book.getYear().map(String::valueOf).orElse("-"));
            });
        }
    }

    @Test
    void testParametersPopulateOptionalBean() {
        var m = new MockConversation(new BookSite());
        var response = m.doRequest("/book", new MockRequest()
            .parameter("title", "Refactoring")
            .parameter("year", "1999"));
        assertEquals("Refactoring:1999", response.getText());
    }

    @Test
    void testAbsentParametersLeaveOptionalsEmpty() {
        var m = new MockConversation(new BookSite());
        assertEquals("-:-", m.doRequest("/book").getText(),
            "absent parameters read back as empty Optionals");
    }

    @Test
    void testEmptyParameterLeavesOptionalEmpty() {
        var m = new MockConversation(new BookSite());
        var response = m.doRequest("/book", new MockRequest()
            .parameter("title", "")
            .parameter("year", "1999"));
        assertEquals("-:1999", response.getText(),
            "an empty submission doesn't populate the property");
    }
}
