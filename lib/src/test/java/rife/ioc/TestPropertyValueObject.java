/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPropertyValueObject {
    @Test
    void testInstantiation() {
        Integer value = 25;

        var object = new PropertyValueObject(value);
        assertNotNull(object);
        assertTrue(object.isStatic());
    }

    @Test
    void testGetValue() {
        Integer value = 25;

        var object = new PropertyValueObject(value);
        assertSame(value, object.getValue());
    }

    @Test
    void testGetValueString() {
        Integer value = 25;

        var object = new PropertyValueObject(value);
        assertEquals("25", object.getValueString());
    }

    @Test
    void testToString() {
        Integer value = 25;

        var object = new PropertyValueObject(value);
        assertEquals("25", object.toString());
    }

    @Test
    void testisNegligible() {
        assertFalse(new PropertyValueObject("lhkjkj").isNegligible());
        assertTrue(new PropertyValueObject("   	 ").isNegligible());
        assertTrue(new PropertyValueObject("").isNegligible());
        assertTrue(new PropertyValueObject(null).isNegligible());
    }
}
