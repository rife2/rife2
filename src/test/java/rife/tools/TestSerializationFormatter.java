/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

public class TestSerializationFormatter {
    @Test
    public void testFormatSerializable()
    throws Exception {
        var type = new BeanImpl3.SerializableType(25, "Some String");
        var result = new SerializationFormatter().format(type);
        assertFalse(result.isEmpty());
        assertEquals(SerializationUtils.serializeToString(type), result);
    }

    @Test
    public void testFormatNotSerializable() {
        var type = new BeanImpl3();
        try {
            var result = new SerializationFormatter().format(type);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("doesn't implement Serializable"));
        }
    }

    @Test
    public void testParseSerializable()
    throws Exception {
        var type = new BeanImpl3.SerializableType(25, "Some String");
        var serialized = SerializationUtils.serializeToString(type);
        var result = new SerializationFormatter().parseObject(serialized);
        assertNotNull(result);
        assertEquals(type, result);
    }

    @Test
    public void testParseNotDeserializable() {
        try {
            var result = new SerializationFormatter().parseObject("junk");
            fail();
        }
        catch (ParseException e) {
            assertNotNull(e);
        }
    }
}
