/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;
import rife.tools.exceptions.ConversionException;

import static org.junit.jupiter.api.Assertions.*;

public class TestConvert {
    @Test
    public void testToType() throws ConversionException {
        assertEquals(true, Convert.toType(1, boolean.class));
        assertEquals(false, Convert.toType(0, boolean.class));
        assertEquals(true, Convert.toType("1", boolean.class));
        assertEquals(false, Convert.toType("0", boolean.class));
        assertEquals(true, Convert.toType("t", boolean.class));

        assertEquals((char) 23, Convert.toType(23L, char.class));
        assertEquals((char) 89, Convert.toType(89.7d, char.class));
        assertEquals('I', Convert.toType('I', char.class));
        assertEquals('p', Convert.toType('p', char.class));
        assertEquals('A', Convert.toType("A", char.class));

        assertEquals((byte) 23, Convert.toType(23L, byte.class));
        assertEquals((byte) 89, Convert.toType(89.7d, byte.class));
        assertEquals((byte) 12, Convert.toType("12", byte.class));

        assertEquals((short) 23, Convert.toType(23L, short.class));
        assertEquals((short) 89, Convert.toType(89.7d, short.class));
        assertEquals((short) 1247, Convert.toType("1247", short.class));

        assertEquals(23, Convert.toType(23L, int.class));
        assertEquals(89, Convert.toType(89.7d, int.class));
        assertEquals(239, Convert.toType("239", int.class));

        assertEquals(23L, Convert.toType(23, long.class));
        assertEquals(890L, Convert.toType(890.7d, long.class));
        assertEquals(86980L, Convert.toType("86980", long.class));

        assertEquals(23f, Convert.toType(23, float.class));
        assertEquals(23.9f, Convert.toType(23.9f, float.class));
        assertEquals(890.7f, Convert.toType(890.7d, float.class));
        assertEquals(869.8f, Convert.toType("869.8", float.class));

        assertEquals(23d, Convert.toType(23, double.class));
        assertEquals(23.9d, (Double) Convert.toType(23.9f, double.class), 0.01d);
        assertEquals(869232.98792d, (Double) Convert.toType(869232.98792d, double.class), 0.000001d);
        assertEquals(2862324.2487d, (Double) Convert.toType("2862324.2487", double.class), 0.00001d);

        assertNull(Convert.toType(null, Boolean.class));
        assertNull(Convert.toType(null, Character.class));
        assertNull(Convert.toType(null, Byte.class));
        assertNull(Convert.toType(null, Short.class));
        assertNull(Convert.toType(null, Integer.class));
        assertNull(Convert.toType(null, Long.class));
        assertNull(Convert.toType(null, Float.class));
        assertNull(Convert.toType(null, Double.class));
        assertNull(Convert.toType(null, String.class));

        assertEquals(true, Convert.toType(1, Boolean.class));
        assertEquals(false, Convert.toType(0, Boolean.class));
        assertEquals(true, Convert.toType("1", Boolean.class));
        assertEquals(false, Convert.toType("0", Boolean.class));
        assertEquals(true, Convert.toType("t", Boolean.class));

        assertEquals((char) 23, Convert.toType(23L, Character.class));
        assertEquals((char) 89, Convert.toType(89.7d, Character.class));
        assertEquals('I', Convert.toType('I', char.class));
        assertEquals('p', Convert.toType('p', Character.class));
        assertEquals('A', Convert.toType("A", Character.class));

        assertEquals((byte) 23, Convert.toType(23L, Byte.class));
        assertEquals((byte) 89, Convert.toType(89.7d, Byte.class));
        assertEquals((byte) 12, Convert.toType("12", Byte.class));

        assertEquals((short) 23, Convert.toType(23L, Short.class));
        assertEquals((short) 89, Convert.toType(89.7d, Short.class));
        assertEquals((short) 1247, Convert.toType("1247", Short.class));

        assertEquals(23, Convert.toType(23L, Integer.class));
        assertEquals(89, Convert.toType(89.7d, Integer.class));
        assertEquals(239, Convert.toType("239", Integer.class));

        assertEquals(23L, Convert.toType(23, Long.class));
        assertEquals(890L, Convert.toType(890.7d, Long.class));
        assertEquals(86980L, Convert.toType("86980", Long.class));

        assertEquals(23f, Convert.toType(23, Float.class));
        assertEquals(23.9f, Convert.toType(23.9f, Float.class));
        assertEquals(890.7f, Convert.toType(890.7d, Float.class));
        assertEquals(869.8f, Convert.toType("869.8", Float.class));

        assertEquals(23d, Convert.toType(23, Double.class));
        assertEquals(23.9d, (Double) Convert.toType(23.9f, Double.class), 0.01d);
        assertEquals(869232.98792d, (Double) Convert.toType(869232.98792d, Double.class), 0.000001d);
        assertEquals(2862324.2487d, (Double) Convert.toType("2862324.2487", Double.class), 0.00001d);

        assertEquals("1234", Convert.toType(1234, String.class));

        assertEquals("IUOJKO", Convert.toType(new StringBuffer("IUOJKO"), CharSequence.class).toString());
    }

    @Test
    public void testToString() {
        assertNull(Convert.toString(null));
        assertEquals("1234", Convert.toString(1234));
    }

    @Test
    public void testToBooleanErrors() {
        try {
            Convert.toBoolean(null);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertNull(e.getFrom());
            assertSame(boolean.class, e.getTo());
        }

        Integer integer = 4;
        try {
            Convert.toBoolean(integer);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(integer, e.getFrom());
            assertSame(boolean.class, e.getTo());
        }

        Object object = new Object();
        try {
            Convert.toBoolean(object);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(object, e.getFrom());
            assertSame(boolean.class, e.getTo());
        }
    }

    @Test
    public void testToBoolean()
        throws ConversionException {
        assertTrue(Convert.toBoolean(1));
        assertFalse(Convert.toBoolean(0));
        assertTrue(Convert.toBoolean("1"));
        assertFalse(Convert.toBoolean("0"));
        assertTrue(Convert.toBoolean("t"));
    }

    @Test
    public void testToBooleanDefaults() {
        assertTrue(Convert.toBoolean(null, true));
        assertFalse(Convert.toBoolean(4, false));
        assertTrue(Convert.toBoolean(new Object(), true));
    }

    @Test
    public void testToCharErrors() {
        try {
            Convert.toChar(null);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertNull(e.getFrom());
            assertSame(char.class, e.getTo());
        }

        Object object = new Object();
        try {
            Convert.toChar(object);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(object, e.getFrom());
            assertSame(char.class, e.getTo());
        }

        String string = "dfsdf";
        try {
            Convert.toChar(string);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(string, e.getFrom());
            assertSame(char.class, e.getTo());
        }
    }

    @Test
    public void testToChar()
        throws ConversionException {
        assertEquals((char) 23, Convert.toChar(23L));
        assertEquals((char) 89, Convert.toChar(89.7d));
        assertEquals('I', Convert.toChar('I'));
        assertEquals('p', Convert.toChar('p'));
        assertEquals('A', Convert.toChar("A"));
    }

    @Test
    public void testToCharDefaults() {
        assertEquals('c', Convert.toChar(null, 'c'));
        assertEquals('f', Convert.toChar(new Object(), 'f'));
        assertEquals('W', Convert.toChar("kjoiji", 'W'));
    }

    @Test
    public void testToByteErrors() {
        try {
            Convert.toByte(null);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertNull(e.getFrom());
            assertSame(byte.class, e.getTo());
        }

        Object object = new Object();
        try {
            Convert.toByte(object);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(object, e.getFrom());
            assertSame(byte.class, e.getTo());
        }

        String string = "dfsdf";
        try {
            Convert.toByte(string);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(string, e.getFrom());
            assertSame(byte.class, e.getTo());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testToByte()
        throws ConversionException {
        assertEquals((byte) 23, Convert.toByte(23L));
        assertEquals((byte) 89, Convert.toByte(89.7d));
        assertEquals((byte) 12, Convert.toByte("12"));
    }

    @Test
    public void testToByteDefaults() {
        assertEquals((byte) 87, Convert.toByte(null, (byte) 87));
        assertEquals((byte) 3, Convert.toByte(new Object(), (byte) 3));
        assertEquals((byte) 79, Convert.toByte("kjoiji", (byte) 79));
    }

    @Test
    public void testToShortErrors() {
        try {
            Convert.toShort(null);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertNull(e.getFrom());
            assertSame(short.class, e.getTo());
        }

        Object object = new Object();
        try {
            Convert.toShort(object);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(object, e.getFrom());
            assertSame(short.class, e.getTo());
        }

        String string = "dfsdf";
        try {
            Convert.toShort(string);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(string, e.getFrom());
            assertSame(short.class, e.getTo());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testToShort()
        throws ConversionException {
        assertEquals((short) 23, Convert.toShort(23L));
        assertEquals((short) 89, Convert.toShort(89.7d));
        assertEquals((short) 1247, Convert.toShort("1247"));
    }

    @Test
    public void testToShortDefaults() {
        assertEquals((short) 87, Convert.toShort(null, (short) 87));
        assertEquals((short) 3, Convert.toShort(new Object(), (short) 3));
        assertEquals((short) 79, Convert.toShort("kjoiji", (short) 79));
    }

    @Test
    public void testToIntErrors() {
        try {
            Convert.toInt(null);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertNull(e.getFrom());
            assertSame(int.class, e.getTo());
        }

        Object object = new Object();
        try {
            Convert.toInt(object);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(object, e.getFrom());
            assertSame(int.class, e.getTo());
        }

        String string = "dfsdf";
        try {
            Convert.toInt(string);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(string, e.getFrom());
            assertSame(int.class, e.getTo());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testToInt()
        throws ConversionException {
        assertEquals(23, Convert.toInt(23L));
        assertEquals(89, Convert.toInt(89.7d));
        assertEquals(239, Convert.toInt("239"));
    }

    @Test
    public void testToIntDefaults() {
        assertEquals(87, Convert.toInt(null, 87));
        assertEquals(3, Convert.toInt(new Object(), 3));
        assertEquals(79, Convert.toInt("kjoiji", 79));
    }

    @Test
    public void testToLongErrors() {
        try {
            Convert.toLong(null);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertNull(e.getFrom());
            assertSame(long.class, e.getTo());
        }

        Object object = new Object();
        try {
            Convert.toLong(object);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(object, e.getFrom());
            assertSame(long.class, e.getTo());
        }

        String string = "dfsdf";
        try {
            Convert.toLong(string);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(string, e.getFrom());
            assertSame(long.class, e.getTo());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testToLong()
        throws ConversionException {
        assertEquals(23L, Convert.toLong(23));
        assertEquals(890L, Convert.toLong(890.7d));
        assertEquals(86980L, Convert.toLong("86980"));
    }

    @Test
    public void testToLongDefaults() {
        assertEquals(878069L, Convert.toLong(null, 878069L));
        assertEquals(3L, Convert.toLong(new Object(), 3L));
        assertEquals(24879L, Convert.toLong("dfjhoij", 24879L));
    }

    @Test
    public void testToFloatErrors() {
        try {
            Convert.toFloat(null);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertNull(e.getFrom());
            assertSame(float.class, e.getTo());
        }

        Object object = new Object();
        try {
            Convert.toFloat(object);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(object, e.getFrom());
            assertSame(float.class, e.getTo());
        }

        String string = "dfsdf";
        try {
            Convert.toFloat(string);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(string, e.getFrom());
            assertSame(float.class, e.getTo());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testToFloat()
        throws ConversionException {
        assertEquals(23f, Convert.toFloat(23));
        assertEquals(23.9f, Convert.toFloat(23.9f));
        assertEquals(890.7f, Convert.toFloat(890.7d));
        assertEquals(869.8f, Convert.toFloat("869.8"));
    }

    @Test
    public void testToFloatDefaults() {
        assertEquals(89.76f, Convert.toFloat(null, 89.76f));
        assertEquals(3.98f, Convert.toFloat(new Object(), 3.98f));
        assertEquals(28.43f, Convert.toFloat("dfjhoij", 28.43f));
    }

    @Test
    public void testToDoubleErrors() {
        try {
            Convert.toDouble(null);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertNull(e.getFrom());
            assertSame(double.class, e.getTo());
        }

        Object object = new Object();
        try {
            Convert.toDouble(object);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(object, e.getFrom());
            assertSame(double.class, e.getTo());
        }

        String string = "dfsdf";
        try {
            Convert.toDouble(string);
            fail("exception not thrown");
        } catch (ConversionException e) {
            assertSame(string, e.getFrom());
            assertSame(double.class, e.getTo());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testToDouble()
        throws ConversionException {
        assertEquals(23d, Convert.toDouble(23));
        assertEquals(23.9d, Convert.toDouble(23.9f), 0.01d);
        assertEquals(869232.98792d, Convert.toDouble(869232.98792d), 0.000001d);
        assertEquals(2862324.2487d, Convert.toDouble("2862324.2487"), 0.00001d);
    }

    @Test
    public void testToDoubleDefaults() {
        assertEquals(89.76d, Convert.toDouble(null, 89.76d), 0.01d);
        assertEquals(869232.98792d, Convert.toDouble(new Object(), 869232.98792d), 0.000001d);
        assertEquals(2248682.24242d, Convert.toDouble("dfjhoij", 2248682.24242d), 0.000001d);
    }
}
