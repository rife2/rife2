/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTextualIdentifierGenerator {
    @Test
    public void testGenerate() {
        TextualIdentifierGenerator<InitializedBeanImpl> identifier = new AbstractTextualIdentifierGenerator<>() {
            public String generateIdentifier() {
                return bean_.getString() + ":" + bean_.getChar();
            }
        };

        var bean = new InitializedBeanImpl();
        identifier.setBean(bean);
        assertEquals("default:i", identifier.generateIdentifier());
        bean.setString("override");
        bean.setChar('z');
        assertEquals("override:z", identifier.generateIdentifier());

        identifier.setBean(new InitializedBeanImpl());
        assertEquals("default:i", identifier.generateIdentifier());
    }
}
