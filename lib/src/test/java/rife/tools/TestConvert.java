/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.config.TestRifeConfig;
import rife.tools.exceptions.ConversionException;

import java.sql.Time;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestConvert {
    @Test
    void testToType()
    throws ConversionException {
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
    void testToString() {
        assertNull(Convert.toString(null));
        assertEquals("1234", Convert.toString(1234));
    }

    @Test
    void testToBooleanErrors() {
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
    void testToBoolean()
    throws ConversionException {
        assertTrue(Convert.toBoolean(1));
        assertFalse(Convert.toBoolean(0));
        assertTrue(Convert.toBoolean("1"));
        assertFalse(Convert.toBoolean("0"));
        assertTrue(Convert.toBoolean("t"));
    }

    @Test
    void testToBooleanDefaults() {
        assertTrue(Convert.toBoolean(null, true));
        assertFalse(Convert.toBoolean(4, false));
        assertTrue(Convert.toBoolean(new Object(), true));
    }

    @Test
    void testToCharErrors() {
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
    void testToChar()
    throws ConversionException {
        assertEquals((char) 23, Convert.toChar(23L));
        assertEquals((char) 89, Convert.toChar(89.7d));
        assertEquals('I', Convert.toChar('I'));
        assertEquals('p', Convert.toChar('p'));
        assertEquals('A', Convert.toChar("A"));
    }

    @Test
    void testToCharDefaults() {
        assertEquals('c', Convert.toChar(null, 'c'));
        assertEquals('f', Convert.toChar(new Object(), 'f'));
        assertEquals('W', Convert.toChar("kjoiji", 'W'));
    }

    @Test
    void testToByteErrors() {
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
    void testToByte()
    throws ConversionException {
        assertEquals((byte) 23, Convert.toByte(23L));
        assertEquals((byte) 89, Convert.toByte(89.7d));
        assertEquals((byte) 12, Convert.toByte("12"));
    }

    @Test
    void testToByteDefaults() {
        assertEquals((byte) 87, Convert.toByte(null, (byte) 87));
        assertEquals((byte) 3, Convert.toByte(new Object(), (byte) 3));
        assertEquals((byte) 79, Convert.toByte("kjoiji", (byte) 79));
    }

    @Test
    void testToShortErrors() {
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
    void testToShort()
    throws ConversionException {
        assertEquals((short) 23, Convert.toShort(23L));
        assertEquals((short) 89, Convert.toShort(89.7d));
        assertEquals((short) 1247, Convert.toShort("1247"));
    }

    @Test
    void testToShortDefaults() {
        assertEquals((short) 87, Convert.toShort(null, (short) 87));
        assertEquals((short) 3, Convert.toShort(new Object(), (short) 3));
        assertEquals((short) 79, Convert.toShort("kjoiji", (short) 79));
    }

    @Test
    void testToIntErrors() {
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
    void testToInt()
    throws ConversionException {
        assertEquals(23, Convert.toInt(23L));
        assertEquals(89, Convert.toInt(89.7d));
        assertEquals(239, Convert.toInt("239"));
    }

    @Test
    void testToIntDefaults() {
        assertEquals(87, Convert.toInt(null, 87));
        assertEquals(3, Convert.toInt(new Object(), 3));
        assertEquals(79, Convert.toInt("kjoiji", 79));
    }

    @Test
    void testToLongErrors() {
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
    void testToLong()
    throws ConversionException {
        assertEquals(23L, Convert.toLong(23));
        assertEquals(890L, Convert.toLong(890.7d));
        assertEquals(86980L, Convert.toLong("86980"));
    }

    @Test
    void testToLongDefaults() {
        assertEquals(878069L, Convert.toLong(null, 878069L));
        assertEquals(3L, Convert.toLong(new Object(), 3L));
        assertEquals(24879L, Convert.toLong("dfjhoij", 24879L));
    }

    @Test
    void testToFloatErrors() {
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
    void testToFloat()
    throws ConversionException {
        assertEquals(23f, Convert.toFloat(23));
        assertEquals(23.9f, Convert.toFloat(23.9f));
        assertEquals(890.7f, Convert.toFloat(890.7d));
        assertEquals(869.8f, Convert.toFloat("869.8"));
    }

    @Test
    void testToFloatDefaults() {
        assertEquals(89.76f, Convert.toFloat(null, 89.76f));
        assertEquals(3.98f, Convert.toFloat(new Object(), 3.98f));
        assertEquals(28.43f, Convert.toFloat("dfjhoij", 28.43f));
    }

    @Test
    void testToDoubleErrors() {
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
    void testToDouble()
    throws ConversionException {
        assertEquals(23d, Convert.toDouble(23));
        assertEquals(23.9d, Convert.toDouble(23.9f), 0.01d);
        assertEquals(869232.98792d, Convert.toDouble(869232.98792d), 0.000001d);
        assertEquals(2862324.2487d, Convert.toDouble("2862324.2487"), 0.00001d);
    }

    @Test
    void testToDoubleDefaults() {
        assertEquals(89.76d, Convert.toDouble(null, 89.76d), 0.01d);
        assertEquals(869232.98792d, Convert.toDouble(new Object(), 869232.98792d), 0.000001d);
        assertEquals(2248682.24242d, Convert.toDouble("dfjhoij", 2248682.24242d), 0.000001d);
    }

    private static String formatPreciseDate(Date date) {
        return RifeConfig.tools().getConcisePreciseDateFormat().format(date);
    }

    private static String formatShortDate(Date date) {
        return RifeConfig.tools().getDefaultShortDateFormat().format(date);
    }

    private static String formatTime(Date date) {
        return RifeConfig.tools().getConcisePreciseTimeFormat().format(date);
    }

    @Test void testToDate()
    throws ConversionException {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23);
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toDate(cal)));
        assertEquals("20230123000000000-0500", formatPreciseDate(Convert.toDate(new java.sql.Date(123, Calendar.JANUARY, 23))));
        assertEquals("19700101134523000-0500", formatPreciseDate(Convert.toDate(new Time(13, 45, 23))));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toDate(Instant.parse("2023-01-23T18:45:23.00Z"))));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toDate(LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23))));
        assertEquals("20230123000000000-0500", formatPreciseDate(Convert.toDate(LocalDate.of(2023, Month.JANUARY, 23))));
        assertEquals("19700101134523000-0500", formatPreciseDate(Convert.toDate(LocalTime.of(13, 45, 23))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toDate(Long.valueOf(1674499523142L))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toDate("1674499523142")));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toDate("20230123134523000-0500")));
        assertEquals("20230123134500000-0500", formatPreciseDate(Convert.toDate("2023-01-23 13:45")));

        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toDate((Object)cal)));
        assertEquals("20230123000000000-0500", formatPreciseDate(Convert.toDate((Object)new java.sql.Date(123, Calendar.JANUARY, 23))));
        assertEquals("19700101134523000-0500", formatPreciseDate(Convert.toDate((Object)new Time(13, 45, 23))));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toDate((Object)Instant.parse("2023-01-23T18:45:23.00Z"))));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toDate((Object)LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23))));
        assertEquals("20230123000000000-0500", formatPreciseDate(Convert.toDate((Object)LocalDate.of(2023, Month.JANUARY, 23))));
        assertEquals("19700101134523000-0500", formatPreciseDate(Convert.toDate((Object)LocalTime.of(13, 45, 23))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toDate((Object)Long.valueOf(1674499523142L))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toDate((Object)"1674499523142")));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toDate((Object)"20230123134523000-0500")));
        assertEquals("20230123134500000-0500", formatPreciseDate(Convert.toDate((Object)"2023-01-23 13:45")));
    }

    @Test void testToSqlDate()
    throws ConversionException {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23);
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate(cal.getTime())));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate(cal)));
        assertEquals("1/1/70",  formatShortDate(Convert.toSqlDate(new Time(13, 45, 23))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate(Instant.parse("2023-01-23T18:45:23.00Z"))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate(LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate(LocalDate.of(2023, Month.JANUARY, 23))));
        assertEquals("1/1/70",  formatShortDate(Convert.toSqlDate(LocalTime.of(13, 45, 23))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate(Long.valueOf(1674499523142L))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate("1674499523142")));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate("20230123134523000-0500")));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate("2023-01-23 13:45")));

        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)cal.getTime())));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)cal)));
        assertEquals("1/1/70",  formatShortDate(Convert.toSqlDate((Object)new Time(13, 45, 23))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)Instant.parse("2023-01-23T18:45:23.00Z"))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)LocalDate.of(2023, Month.JANUARY, 23))));
        assertEquals("1/1/70",  formatShortDate(Convert.toSqlDate((Object)LocalTime.of(13, 45, 23))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)Long.valueOf(1674499523142L))));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)"1674499523142")));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)"20230123134523000-0500")));
        assertEquals("1/23/23", formatShortDate(Convert.toSqlDate((Object)"2023-01-23 13:45")));
    }

    @Test void testToTimestamp()
    throws ConversionException {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23, 142);
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp(cal.getTime())));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp(cal)));
        assertEquals("20230123000000000-0500", formatPreciseDate(Convert.toTimestamp(new java.sql.Date(123, Calendar.JANUARY, 23))));
        assertEquals("19700101134523000-0500", formatPreciseDate(Convert.toTimestamp(new Time(13, 45, 23))));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toTimestamp(Instant.parse("2023-01-23T18:45:23.00Z"))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp(LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000))));
        assertEquals("20230123000000000-0500", formatPreciseDate(Convert.toTimestamp(LocalDate.of(2023, Month.JANUARY, 23))));
        assertEquals("19700101134523000-0500", formatPreciseDate(Convert.toTimestamp(LocalTime.of(13, 45, 23))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp(Long.valueOf(1674499523142L))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp("1674499523142")));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toTimestamp("20230123134523000-0500")));
        assertEquals("20230123134500000-0500", formatPreciseDate(Convert.toTimestamp("2023-01-23 13:45")));

        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp((Object)cal.getTime())));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp((Object)cal)));
        assertEquals("20230123000000000-0500", formatPreciseDate(Convert.toTimestamp((Object)new java.sql.Date(123, Calendar.JANUARY, 23))));
        assertEquals("19700101134523000-0500", formatPreciseDate(Convert.toTimestamp((Object)new Time(13, 45, 23))));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toTimestamp((Object)Instant.parse("2023-01-23T18:45:23.00Z"))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp((Object)LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000))));
        assertEquals("20230123000000000-0500", formatPreciseDate(Convert.toTimestamp((Object)LocalDate.of(2023, Month.JANUARY, 23))));
        assertEquals("19700101134523000-0500", formatPreciseDate(Convert.toTimestamp((Object)LocalTime.of(13, 45, 23))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp((Object)Long.valueOf(1674499523142L))));
        assertEquals("20230123134523142-0500", formatPreciseDate(Convert.toTimestamp((Object)"1674499523142")));
        assertEquals("20230123134523000-0500", formatPreciseDate(Convert.toTimestamp((Object)"20230123134523000-0500")));
        assertEquals("20230123134500000-0500", formatPreciseDate(Convert.toTimestamp((Object)"2023-01-23 13:45")));
    }

    @Test void testToTime()
    throws ConversionException {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23, 142);
        assertEquals("134523000-0500", formatTime(Convert.toTime(cal.getTime())));
        assertEquals("134523000-0500", formatTime(Convert.toTime(cal)));
        assertEquals("000000000-0500", formatTime(Convert.toTime(new java.sql.Date(123, Calendar.JANUARY, 23))));
        assertEquals("134523000-0500", formatTime(Convert.toTime(Instant.parse("2023-01-23T18:45:23.00Z"))));
        assertEquals("134523000-0500", formatTime(Convert.toTime(LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000))));
        assertEquals("000000000-0500", formatTime(Convert.toTime(LocalDate.of(2023, Month.JANUARY, 23))));
        assertEquals("134523000-0500", formatTime(Convert.toTime(LocalTime.of(13, 45, 23))));
        assertEquals("134523000-0500", formatTime(Convert.toTime(Long.valueOf(1674499523142L))));
        assertEquals("134523000-0500", formatTime(Convert.toTime("1674499523142")));
        assertEquals("134523000-0500", formatTime(Convert.toTime("134523000-0500")));
        assertEquals("134500000-0500", formatTime(Convert.toTime("13:45")));

        assertEquals("134523000-0500", formatTime(Convert.toTime((Object)cal.getTime())));
        assertEquals("134523000-0500", formatTime(Convert.toTime((Object)cal)));
        assertEquals("000000000-0500", formatTime(Convert.toTime((Object)new java.sql.Date(123, Calendar.JANUARY, 23))));
        assertEquals("134523000-0500", formatTime(Convert.toTime((Object)Instant.parse("2023-01-23T18:45:23.00Z"))));
        assertEquals("134523000-0500", formatTime(Convert.toTime((Object)LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000))));
        assertEquals("000000000-0500", formatTime(Convert.toTime((Object)LocalDate.of(2023, Month.JANUARY, 23))));
        assertEquals("134523000-0500", formatTime(Convert.toTime((Object)LocalTime.of(13, 45, 23))));
        assertEquals("134523000-0500", formatTime(Convert.toTime((Object)Long.valueOf(1674499523142L))));
        assertEquals("134523000-0500", formatTime(Convert.toTime((Object)"1674499523142")));
        assertEquals("134523000-0500", formatTime(Convert.toTime((Object)"134523000-0500")));
        assertEquals("134500000-0500", formatTime(Convert.toTime((Object)"13:45")));
    }

    @Test void testToInstant()
    throws ConversionException {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23, 142);
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant(cal.getTime()).toString());
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant(cal).toString());
        assertEquals("2023-01-23T05:00:00Z",     Convert.toInstant(new java.sql.Date(123, Calendar.JANUARY, 23)).toString());
        assertEquals("1970-01-01T18:45:23Z",     Convert.toInstant(new Time(13, 45, 23)).toString());
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant(LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000)).toString());
        assertEquals("2023-01-23T05:00:00Z",     Convert.toInstant(LocalDate.of(2023, Month.JANUARY, 23)).toString());
        assertEquals("1970-01-01T18:45:23Z",     Convert.toInstant(LocalTime.of(13, 45, 23)).toString());
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant(Long.valueOf(1674499523142L)).toString());
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant("1674499523142").toString());
        assertEquals("2023-01-23T18:45:23Z",     Convert.toInstant("20230123134523000-0500").toString());
        assertEquals("2023-01-23T18:45:00Z",     Convert.toInstant("2023-01-23 13:45").toString());

        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant((Object)cal.getTime()).toString());
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant((Object)cal).toString());
        assertEquals("2023-01-23T05:00:00Z",     Convert.toInstant((Object)new java.sql.Date(123, Calendar.JANUARY, 23)).toString());
        assertEquals("1970-01-01T18:45:23Z",     Convert.toInstant((Object)new Time(13, 45, 23)).toString());
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant((Object)LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000)).toString());
        assertEquals("2023-01-23T05:00:00Z",     Convert.toInstant((Object)LocalDate.of(2023, Month.JANUARY, 23)).toString());
        assertEquals("1970-01-01T18:45:23Z",     Convert.toInstant((Object)LocalTime.of(13, 45, 23)).toString());
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant((Object)Long.valueOf(1674499523142L)).toString());
        assertEquals("2023-01-23T18:45:23.142Z", Convert.toInstant((Object)"1674499523142").toString());
        assertEquals("2023-01-23T18:45:23Z",     Convert.toInstant((Object)"20230123134523000-0500").toString());
        assertEquals("2023-01-23T18:45:00Z",     Convert.toInstant((Object)"2023-01-23 13:45").toString());
    }

    @Test void testToLocalDateTime()
    throws ConversionException {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23, 142);
        assertEquals("2023-01-23T13:45:23.142", Convert.toLocalDateTime(cal.getTime()).toString());
        assertEquals("2023-01-23T13:45:23.142", Convert.toLocalDateTime(cal).toString());
        assertEquals("2023-01-23T00:00",        Convert.toLocalDateTime(new java.sql.Date(123, Calendar.JANUARY, 23)).toString());
        assertEquals("1970-01-01T13:45:23",     Convert.toLocalDateTime(new Time(13, 45, 23)).toString());
        assertEquals("2023-01-23T13:45:23",     Convert.toLocalDateTime(Instant.parse("2023-01-23T18:45:23.00Z")).toString());
        assertEquals("2023-01-23T00:00",        Convert.toLocalDateTime(LocalDate.of(2023, Month.JANUARY, 23)).toString());
        assertEquals("1970-01-01T13:45:23",     Convert.toLocalDateTime(LocalTime.of(13, 45, 23)).toString());
        assertEquals("2023-01-23T13:45:23.142", Convert.toLocalDateTime(Long.valueOf(1674499523142L)).toString());
        assertEquals("2023-01-23T13:45:23.142", Convert.toLocalDateTime("1674499523142").toString());
        assertEquals("2023-01-23T13:45:23",     Convert.toLocalDateTime("20230123134523000-0500").toString());
        assertEquals("2023-01-23T13:45",        Convert.toLocalDateTime("2023-01-23 13:45").toString());

        assertEquals("2023-01-23T13:45:23.142", Convert.toLocalDateTime((Object)cal.getTime()).toString());
        assertEquals("2023-01-23T13:45:23.142", Convert.toLocalDateTime((Object)cal).toString());
        assertEquals("2023-01-23T00:00",        Convert.toLocalDateTime((Object)new java.sql.Date(123, Calendar.JANUARY, 23)).toString());
        assertEquals("1970-01-01T13:45:23",     Convert.toLocalDateTime((Object)new Time(13, 45, 23)).toString());
        assertEquals("2023-01-23T13:45:23",     Convert.toLocalDateTime((Object)Instant.parse("2023-01-23T18:45:23.00Z")).toString());
        assertEquals("2023-01-23T00:00",        Convert.toLocalDateTime((Object)LocalDate.of(2023, Month.JANUARY, 23)).toString());
        assertEquals("1970-01-01T13:45:23",     Convert.toLocalDateTime((Object)LocalTime.of(13, 45, 23)).toString());
        assertEquals("2023-01-23T13:45:23.142", Convert.toLocalDateTime((Object)Long.valueOf(1674499523142L)).toString());
        assertEquals("2023-01-23T13:45:23.142", Convert.toLocalDateTime((Object)"1674499523142").toString());
        assertEquals("2023-01-23T13:45:23",     Convert.toLocalDateTime((Object)"20230123134523000-0500").toString());
        assertEquals("2023-01-23T13:45",        Convert.toLocalDateTime((Object)"2023-01-23 13:45").toString());
    }

    @Test void testToLocalDate()
    throws ConversionException {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23, 142);
        assertEquals("2023-01-23", Convert.toLocalDate(cal.getTime()).toString());
        assertEquals("2023-01-23", Convert.toLocalDate(cal).toString());
        assertEquals("2023-01-23", Convert.toLocalDate(new java.sql.Date(123, Calendar.JANUARY, 23)).toString());
        assertEquals("1970-01-01", Convert.toLocalDate(new Time(13, 45, 23)).toString());
        assertEquals("2023-01-23", Convert.toLocalDate(Instant.parse("2023-01-23T18:45:23.00Z")).toString());
        assertEquals("2023-01-23", Convert.toLocalDate(LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000)).toString());
        assertEquals("1970-01-01", Convert.toLocalDate(LocalTime.of(13, 45, 23)).toString());
        assertEquals("2023-01-23", Convert.toLocalDate(Long.valueOf(1674499523142L)).toString());
        assertEquals("2023-01-23", Convert.toLocalDate("1674499523142").toString());
        assertEquals("2023-01-23", Convert.toLocalDate("20230123134523000-0500").toString());
        assertEquals("2023-01-23", Convert.toLocalDate("2023-01-23 13:45").toString());

        assertEquals("2023-01-23", Convert.toLocalDate((Object)cal.getTime()).toString());
        assertEquals("2023-01-23", Convert.toLocalDate((Object)cal).toString());
        assertEquals("2023-01-23", Convert.toLocalDate((Object)new java.sql.Date(123, Calendar.JANUARY, 23)).toString());
        assertEquals("1970-01-01", Convert.toLocalDate((Object)new Time(13, 45, 23)).toString());
        assertEquals("2023-01-23", Convert.toLocalDate((Object)Instant.parse("2023-01-23T18:45:23.00Z")).toString());
        assertEquals("2023-01-23", Convert.toLocalDate((Object)LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000)).toString());
        assertEquals("1970-01-01", Convert.toLocalDate((Object)LocalTime.of(13, 45, 23)).toString());
        assertEquals("2023-01-23", Convert.toLocalDate((Object)Long.valueOf(1674499523142L)).toString());
        assertEquals("2023-01-23", Convert.toLocalDate((Object)"1674499523142").toString());
        assertEquals("2023-01-23", Convert.toLocalDate((Object)"20230123134523000-0500").toString());
        assertEquals("2023-01-23", Convert.toLocalDate((Object)"2023-01-23 13:45").toString());
    }

    @Test void testToLocalTime()
    throws ConversionException {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23, 142);
        assertEquals("13:45:23.142", Convert.toLocalTime(cal.getTime()).toString());
        assertEquals("13:45:23.142", Convert.toLocalTime(cal).toString());
        assertEquals("00:00",        Convert.toLocalTime(new java.sql.Date(123, Calendar.JANUARY, 23)).toString());
        assertEquals("13:45:23",     Convert.toLocalTime(new Time(13, 45, 23)).toString());
        assertEquals("13:45:23",     Convert.toLocalTime(Instant.parse("2023-01-23T18:45:23.00Z")).toString());
        assertEquals("00:00",        Convert.toLocalTime(LocalDate.of(2023, Month.JANUARY, 23)).toString());
        assertEquals("13:45:23.142", Convert.toLocalTime(LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000)).toString());
        assertEquals("13:45:23.142", Convert.toLocalTime(Long.valueOf(1674499523142L)).toString());
        assertEquals("13:45:23.142", Convert.toLocalTime("1674499523142").toString());
        assertEquals("13:45:23",     Convert.toLocalTime("134523000-0500").toString());
        assertEquals("13:45",        Convert.toLocalTime("13:45").toString());

        assertEquals("13:45:23.142", Convert.toLocalTime((Object)cal.getTime()).toString());
        assertEquals("13:45:23.142", Convert.toLocalTime((Object)cal).toString());
        assertEquals("00:00",        Convert.toLocalTime((Object)new java.sql.Date(123, Calendar.JANUARY, 23)).toString());
        assertEquals("13:45:23",     Convert.toLocalTime((Object)new Time(13, 45, 23)).toString());
        assertEquals("13:45:23",     Convert.toLocalTime((Object)Instant.parse("2023-01-23T18:45:23.00Z")).toString());
        assertEquals("00:00",        Convert.toLocalTime((Object)LocalDate.of(2023, Month.JANUARY, 23)).toString());
        assertEquals("13:45:23.142", Convert.toLocalTime((Object)LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000)).toString());
        assertEquals("13:45:23.142", Convert.toLocalTime((Object)Long.valueOf(1674499523142L)).toString());
        assertEquals("13:45:23.142", Convert.toLocalTime((Object)"1674499523142").toString());
        assertEquals("13:45:23",     Convert.toLocalTime((Object)"134523000-0500").toString());
        assertEquals("13:45",        Convert.toLocalTime((Object)"13:45").toString());
    }
}
