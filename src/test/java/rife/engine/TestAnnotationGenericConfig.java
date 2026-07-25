/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.engine.annotations.Config;
import rife.test.MockConversation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestAnnotationGenericConfig {
    public static class GenericConfigElement<T> implements Element {
        @Config("numbers") List<T> numbers = null;

        public void process(Context c) {
            var output = new StringBuilder();
            if (numbers != null) {
                for (var number : numbers) {
                    output.append(number.getClass().getSimpleName()).append(':').append(number).append(' ');
                }
            }
            c.print(output.toString().trim());
        }
    }

    public static class IntegerConfigElement extends GenericConfigElement<Integer> {}

    @Test
    void testInheritedGenericConfigListResolves() {
        var m = new MockConversation(new Site() {
            public void setup() {
                config().putItem("numbers", 11);
                config().putItem("numbers", 22);
                get("/config", IntegerConfigElement.class);
            }
        });

        // the element type of the inherited generic list resolves against
        // the concrete element class, so the items convert to Integer
        // instead of being injected as raw strings
        assertEquals("Integer:11 Integer:22", m.doRequest("/config").getText());
    }
}
