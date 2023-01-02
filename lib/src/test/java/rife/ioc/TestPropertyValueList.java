/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPropertyValueList {
    @Test
    void testInstantiation() {
        PropertyValueList list = new PropertyValueList();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    void testSingleNonNegligible() {
        var list = new PropertyValueList();
        var value = new PropertyValueObject("Not Negligible");
        list.add(value);
        assertEquals(1, list.size());
        assertSame(value, list.makePropertyValue());
    }

    @Test
    void testOneNoneNegligibleOtherNegligibles() {
        var value = new PropertyValueObject("Not Negligible");
        var list = new PropertyValueList();
        list.add(new PropertyValueObject("  "));
        list.add(value);
        list.add(new PropertyValueObject("    "));
        list.add(new PropertyValueObject(""));
        assertEquals(4, list.size());
        assertSame(value, list.makePropertyValue());
    }

    @Test
    void testOneNoneNegligibleOtherNonNegligible() {
        var value = new PropertyValueObject("Not Negligible");
        var list = new PropertyValueList();
        list.add(new PropertyValueObject("  "));
        list.add(value);
        list.add(new PropertyValueObject("    "));
        list.add(new PropertyValueObject("testing"));
        assertEquals(4, list.size());

        PropertyValue result = list.makePropertyValue();
        assertNotSame(value, result);
        assertEquals(value + "    testing", result.getValueString());
    }
}
