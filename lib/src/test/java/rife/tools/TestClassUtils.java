/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestClassUtils {
    @Test
    public void testIsNumeric() {
        assertTrue(ClassUtils.isNumeric(Byte.class));
        assertTrue(ClassUtils.isNumeric(Short.class));
        assertTrue(ClassUtils.isNumeric(Integer.class));
        assertTrue(ClassUtils.isNumeric(Long.class));
        assertTrue(ClassUtils.isNumeric(Float.class));
        assertTrue(ClassUtils.isNumeric(Double.class));
        assertTrue(ClassUtils.isNumeric(BigDecimal.class));
        assertTrue(ClassUtils.isNumeric(BigInteger.class));
        assertTrue(ClassUtils.isNumeric(byte.class));
        assertTrue(ClassUtils.isNumeric(short.class));
        assertTrue(ClassUtils.isNumeric(int.class));
        assertTrue(ClassUtils.isNumeric(long.class));
        assertTrue(ClassUtils.isNumeric(float.class));
        assertTrue(ClassUtils.isNumeric(double.class));
        assertFalse(ClassUtils.isNumeric(Character.class));
        assertFalse(ClassUtils.isNumeric(String.class));
        assertFalse(ClassUtils.isNumeric(StringBuffer.class));
        assertFalse(ClassUtils.isNumeric(char.class));
        assertFalse(ClassUtils.isNumeric(Date.class));
    }

    @Test
    public void testIsText() {
        assertFalse(ClassUtils.isText(Byte.class));
        assertFalse(ClassUtils.isText(Short.class));
        assertFalse(ClassUtils.isText(Integer.class));
        assertFalse(ClassUtils.isText(Long.class));
        assertFalse(ClassUtils.isText(Float.class));
        assertFalse(ClassUtils.isText(Double.class));
        assertFalse(ClassUtils.isText(BigDecimal.class));
        assertFalse(ClassUtils.isText(BigInteger.class));
        assertFalse(ClassUtils.isText(byte.class));
        assertFalse(ClassUtils.isText(short.class));
        assertFalse(ClassUtils.isText(int.class));
        assertFalse(ClassUtils.isText(long.class));
        assertFalse(ClassUtils.isText(float.class));
        assertFalse(ClassUtils.isText(double.class));
        assertTrue(ClassUtils.isText(Character.class));
        assertTrue(ClassUtils.isText(String.class));
        assertTrue(ClassUtils.isText(StringBuffer.class));
        assertTrue(ClassUtils.isText(char.class));
        assertFalse(ClassUtils.isText(Date.class));
    }
}

interface Interface1 {
}

interface Interface2 extends Interface1 {
}

class Parent implements Interface1 {
}

class Child1 extends Parent {
}

class Child2 extends Child1 implements Interface2, Interface1 {
}

class Child3 extends Parent {
}

class Child4 extends Object {
}
