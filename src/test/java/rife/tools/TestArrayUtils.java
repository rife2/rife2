/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.validation.ConstrainedProperty;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class TestArrayUtils {
    @Test
    void testGetArrayType() {
        assertEquals(ArrayUtils.ArrayType.NO_ARRAY, ArrayUtils.getArrayType(new Object()));

        assertEquals(ArrayUtils.ArrayType.BOOLEAN_ARRAY, ArrayUtils.getArrayType(new boolean[1]));
        assertEquals(ArrayUtils.ArrayType.BYTE_ARRAY, ArrayUtils.getArrayType(new byte[1]));
        assertEquals(ArrayUtils.ArrayType.SHORT_ARRAY, ArrayUtils.getArrayType(new short[1]));
        assertEquals(ArrayUtils.ArrayType.CHAR_ARRAY, ArrayUtils.getArrayType(new char[1]));
        assertEquals(ArrayUtils.ArrayType.INT_ARRAY, ArrayUtils.getArrayType(new int[1]));
        assertEquals(ArrayUtils.ArrayType.LONG_ARRAY, ArrayUtils.getArrayType(new long[1]));
        assertEquals(ArrayUtils.ArrayType.FLOAT_ARRAY, ArrayUtils.getArrayType(new float[1]));
        assertEquals(ArrayUtils.ArrayType.DOUBLE_ARRAY, ArrayUtils.getArrayType(new double[1]));
        assertEquals(ArrayUtils.ArrayType.OBJECT_ARRAY, ArrayUtils.getArrayType(new Object[1]));

        assertEquals(ArrayUtils.ArrayType.BOOLEAN_ARRAY, ArrayUtils.getArrayType(new boolean[1][1]));
        assertEquals(ArrayUtils.ArrayType.BYTE_ARRAY, ArrayUtils.getArrayType(new byte[1][1]));
        assertEquals(ArrayUtils.ArrayType.SHORT_ARRAY, ArrayUtils.getArrayType(new short[1][1]));
        assertEquals(ArrayUtils.ArrayType.CHAR_ARRAY, ArrayUtils.getArrayType(new char[1][1]));
        assertEquals(ArrayUtils.ArrayType.INT_ARRAY, ArrayUtils.getArrayType(new int[1][1]));
        assertEquals(ArrayUtils.ArrayType.LONG_ARRAY, ArrayUtils.getArrayType(new long[1][1]));
        assertEquals(ArrayUtils.ArrayType.FLOAT_ARRAY, ArrayUtils.getArrayType(new float[1][1]));
        assertEquals(ArrayUtils.ArrayType.DOUBLE_ARRAY, ArrayUtils.getArrayType(new double[1][1]));
        assertEquals(ArrayUtils.ArrayType.OBJECT_ARRAY, ArrayUtils.getArrayType(new Object[1][1]));
    }

    @Test
    void testCreateStringArray() {
        assertNull(ArrayUtils.createStringArray((Object) null, null));
        String[] converted = null;

        converted = ArrayUtils.createStringArray("just a test", null);
        assertEquals(1, converted.length);
        assertEquals("just a test", converted[0]);

        var source_string = new String[]{"9", "kojk", "4", "3", "ok", "6.0", "8"};
        converted = ArrayUtils.createStringArray((Object) source_string, null);
        assertEquals(source_string.length, converted.length);
        assertEquals(source_string[0], converted[0]);
        assertEquals(source_string[1], converted[1]);
        assertEquals(source_string[2], converted[2]);
        assertEquals(source_string[3], converted[3]);
        assertEquals(source_string[4], converted[4]);
        assertEquals(source_string[5], converted[5]);
        assertEquals(source_string[6], converted[6]);

        var source_boolean = new boolean[]{false, false, true, false, true};
        converted = ArrayUtils.createStringArray((Object) source_boolean, null);
        assertEquals(source_boolean.length, converted.length);
        assertEquals(String.valueOf(source_boolean[0]), converted[0]);
        assertEquals(String.valueOf(source_boolean[1]), converted[1]);
        assertEquals(String.valueOf(source_boolean[2]), converted[2]);
        assertEquals(String.valueOf(source_boolean[3]), converted[3]);
        assertEquals(String.valueOf(source_boolean[4]), converted[4]);

        var source_byte = new byte[]{9, 4, 3, 6, 8};
        converted = ArrayUtils.createStringArray((Object) source_byte, null);
        assertNull(converted);

        var source_char = new char[]{'w', 'o', 'r', 'k', 's'};
        converted = ArrayUtils.createStringArray((Object) source_char, null);
        assertEquals(source_char.length, converted.length);
        assertEquals(String.valueOf(source_char[0]), converted[0]);
        assertEquals(String.valueOf(source_char[1]), converted[1]);
        assertEquals(String.valueOf(source_char[2]), converted[2]);
        assertEquals(String.valueOf(source_char[3]), converted[3]);
        assertEquals(String.valueOf(source_char[4]), converted[4]);

        var source_short = new short[]{84, 23, 43, 12, 5};
        converted = ArrayUtils.createStringArray((Object) source_short, null);
        assertEquals(source_short.length, converted.length);
        assertEquals(String.valueOf(source_short[0]), converted[0]);
        assertEquals(String.valueOf(source_short[1]), converted[1]);
        assertEquals(String.valueOf(source_short[2]), converted[2]);
        assertEquals(String.valueOf(source_short[3]), converted[3]);
        assertEquals(String.valueOf(source_short[4]), converted[4]);

        var source_int = new int[]{9834, 454, 2355, 2398, 4834};
        converted = ArrayUtils.createStringArray((Object) source_int, null);
        assertEquals(source_int.length, converted.length);
        assertEquals(String.valueOf(source_int[0]), converted[0]);
        assertEquals(String.valueOf(source_int[1]), converted[1]);
        assertEquals(String.valueOf(source_int[2]), converted[2]);
        assertEquals(String.valueOf(source_int[3]), converted[3]);
        assertEquals(String.valueOf(source_int[4]), converted[4]);

        var source_long = new long[]{59035, 90465, 723479, 47543, 987543};
        converted = ArrayUtils.createStringArray((Object) source_long, null);
        assertEquals(source_long.length, converted.length);
        assertEquals(String.valueOf(source_long[0]), converted[0]);
        assertEquals(String.valueOf(source_long[1]), converted[1]);
        assertEquals(String.valueOf(source_long[2]), converted[2]);
        assertEquals(String.valueOf(source_long[3]), converted[3]);
        assertEquals(String.valueOf(source_long[4]), converted[4]);

        var source_float = new float[]{228.02f, 8734.3f, 8634.2f, 34321.9f, 3478.2f};
        converted = ArrayUtils.createStringArray((Object) source_float, null);
        assertEquals(source_float.length, converted.length);
        assertEquals(String.valueOf(source_float[0]), converted[0]);
        assertEquals(String.valueOf(source_float[1]), converted[1]);
        assertEquals(String.valueOf(source_float[2]), converted[2]);
        assertEquals(String.valueOf(source_float[3]), converted[3]);
        assertEquals(String.valueOf(source_float[4]), converted[4]);

        var source_double = new double[]{987634.3434d, 653928.434d, 394374.34387d, 3847764332.3434d, 3434d};
        converted = ArrayUtils.createStringArray((Object) source_double, null);
        assertEquals(source_double.length, converted.length);
        assertEquals(String.valueOf(source_double[0]), converted[0]);
        assertEquals(String.valueOf(source_double[1]), converted[1]);
        assertEquals(String.valueOf(source_double[2]), converted[2]);
        assertEquals(String.valueOf(source_double[3]), converted[3]);
        assertEquals(String.valueOf(source_double[4]), converted[4]);
    }

    @Test
    void testCreateStringArrayObject() {
        assertNull(ArrayUtils.createStringArray((Object[]) null));

        var source = new String[]{"9", "kojk", "4", "3", "ok", "6.0", "8"};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(source[0], converted[0]);
        assertEquals(source[1], converted[1]);
        assertEquals(source[2], converted[2]);
        assertEquals(source[3], converted[3]);
        assertEquals(source[4], converted[4]);
        assertEquals(source[5], converted[5]);
        assertEquals(source[6], converted[6]);
    }

    @Test
    void testCreateStringArrayDate() {
        assertNull(ArrayUtils.createStringArray((Date) null, null));

        var cal = Calendar.getInstance();
        cal.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
        cal.set(2005, Calendar.AUGUST, 18, 9, 27, 13);
        cal.set(Calendar.MILLISECOND, 552);
        var converted = ArrayUtils.createStringArray(cal.getTime(), null);
        assertEquals(1, converted.length);
        assertEquals("20050818092713552-0500", converted[0]);

        var sf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss", Locale.ENGLISH);
        sf.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
        converted = ArrayUtils.createStringArray(cal.getTime(), new ConstrainedProperty("someProperty").format(sf));
        assertEquals(1, converted.length);
        assertEquals("2005.08.18 AD at 09:27:13", converted[0]);
    }

    @Test
    void testCreateStringArrayDoubleFormat() {
        assertNull(ArrayUtils.createStringArray((Double) null, null));

        var converted = ArrayUtils.createStringArray(6782.349876675, new ConstrainedProperty("someProperty").format(NumberFormat.getCurrencyInstance(Locale.US)));
        assertEquals(1, converted.length);
        assertEquals("$6,782.35", converted[0]);
    }

    @Test
    void testCreateStringArrayBoolean() {
        assertNull(ArrayUtils.createStringArray((boolean[]) null));

        var source = new boolean[]{false, false, true, false, true};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(source.length, converted.length);
        assertEquals(String.valueOf(source[0]), converted[0]);
        assertEquals(String.valueOf(source[1]), converted[1]);
        assertEquals(String.valueOf(source[2]), converted[2]);
        assertEquals(String.valueOf(source[3]), converted[3]);
        assertEquals(String.valueOf(source[4]), converted[4]);
    }

    @Test
    void testCreateStringArrayByte() {
        assertNull(ArrayUtils.createStringArray((byte[]) null));

        var source = new byte[]{9, 4, 3, 6, 8};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(String.valueOf(source[0]), converted[0]);
        assertEquals(String.valueOf(source[1]), converted[1]);
        assertEquals(String.valueOf(source[2]), converted[2]);
        assertEquals(String.valueOf(source[3]), converted[3]);
        assertEquals(String.valueOf(source[4]), converted[4]);
    }

    @Test
    void testCreateStringArrayChar() {
        assertNull(ArrayUtils.createStringArray((char[]) null));

        var source = new char[]{'w', 'o', 'r', 'k', 's'};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(String.valueOf(source[0]), converted[0]);
        assertEquals(String.valueOf(source[1]), converted[1]);
        assertEquals(String.valueOf(source[2]), converted[2]);
        assertEquals(String.valueOf(source[3]), converted[3]);
        assertEquals(String.valueOf(source[4]), converted[4]);
    }

    @Test
    void testCreateStringArrayShort() {
        assertNull(ArrayUtils.createStringArray((short[]) null));

        var source = new short[]{84, 23, 43, 12, 5};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(String.valueOf(source[0]), converted[0]);
        assertEquals(String.valueOf(source[1]), converted[1]);
        assertEquals(String.valueOf(source[2]), converted[2]);
        assertEquals(String.valueOf(source[3]), converted[3]);
        assertEquals(String.valueOf(source[4]), converted[4]);
    }

    @Test
    void testCreateStringArrayInt() {
        assertNull(ArrayUtils.createStringArray((int[]) null));

        var source = new int[]{9834, 454, 2355, 2398, 4834};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(String.valueOf(source[0]), converted[0]);
        assertEquals(String.valueOf(source[1]), converted[1]);
        assertEquals(String.valueOf(source[2]), converted[2]);
        assertEquals(String.valueOf(source[3]), converted[3]);
        assertEquals(String.valueOf(source[4]), converted[4]);
    }

    @Test
    void testCreateStringArrayLong() {
        assertNull(ArrayUtils.createStringArray((long[]) null));

        var source = new long[]{59035, 90465, 723479, 47543, 987543};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(String.valueOf(source[0]), converted[0]);
        assertEquals(String.valueOf(source[1]), converted[1]);
        assertEquals(String.valueOf(source[2]), converted[2]);
        assertEquals(String.valueOf(source[3]), converted[3]);
        assertEquals(String.valueOf(source[4]), converted[4]);
    }

    @Test
    void testCreateStringArrayFloat() {
        assertNull(ArrayUtils.createStringArray((float[]) null));

        var source = new float[]{228.02f, 8734.3f, 8634.2f, 34321.9f, 3478.2f};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(String.valueOf(source[0]), converted[0]);
        assertEquals(String.valueOf(source[1]), converted[1]);
        assertEquals(String.valueOf(source[2]), converted[2]);
        assertEquals(String.valueOf(source[3]), converted[3]);
        assertEquals(String.valueOf(source[4]), converted[4]);
    }

    @Test
    void testCreateStringArrayDouble() {
        assertNull(ArrayUtils.createStringArray((double[]) null));

        var source = new double[]{987634.3434d, 653928.434d, 394374.34387d, 3847764332.3434d, 3434d};
        var converted = ArrayUtils.createStringArray(source);
        assertEquals(source.length, converted.length);
        assertEquals(String.valueOf(source[0]), converted[0]);
        assertEquals(String.valueOf(source[1]), converted[1]);
        assertEquals(String.valueOf(source[2]), converted[2]);
        assertEquals(String.valueOf(source[3]), converted[3]);
        assertEquals(String.valueOf(source[4]), converted[4]);
    }

    @Test
    void testCreateBooleanArray() {
        assertNull(ArrayUtils.createBooleanArray(null));

        var source = new String[]{"false", "false", null, "true", "false", "true"};
        var target = new boolean[]{false, false, true, false, true};
        var converted = ArrayUtils.createBooleanArray(source);
        assertEquals(target.length, converted.length);
        assertEquals(target[0], converted[0]);
        assertEquals(target[1], converted[1]);
        assertEquals(target[2], converted[2]);
        assertEquals(target[3], converted[3]);
        assertEquals(target[4], converted[4]);
    }

    @Test
    void testCreateByteArray() {
        assertNull(ArrayUtils.createByteArray(null));

        var source = new Object[]{9, "ko", "4", null, 3L, "ok", "6", "8"};
        var target = new byte[]{9, 4, 3, 6, 8};
        var converted = ArrayUtils.createByteArray(source);
        assertEquals(target.length, converted.length);
        assertEquals(target[0], converted[0]);
        assertEquals(target[1], converted[1]);
        assertEquals(target[2], converted[2]);
        assertEquals(target[3], converted[3]);
        assertEquals(target[4], converted[4]);
    }

    @Test
    void testCreateCharArray() {
        assertNull(ArrayUtils.createCharArray(null));

        var source = new Object[]{'w', "loo", null, "ko", "o", "r", "k", "s", new StringBuffer("oook")};
        var target = new char[]{'w', 'o', 'r', 'k', 's'};
        var converted = ArrayUtils.createCharArray(source);
        assertEquals(target.length, converted.length);
        assertEquals(target[0], converted[0]);
        assertEquals(target[1], converted[1]);
        assertEquals(target[2], converted[2]);
        assertEquals(target[3], converted[3]);
        assertEquals(target[4], converted[4]);
    }

    @Test
    void testCreateShortArray() {
        assertNull(ArrayUtils.createShortArray(null));

        var source = new Object[]{"84", "ko", (byte) 23, "43", "ok", null, (short) 12, "5"};
        var target = new short[]{84, 23, 43, 12, 5};
        var converted = ArrayUtils.createShortArray(source);
        assertEquals(target.length, converted.length);
        assertEquals(target[0], converted[0]);
        assertEquals(target[1], converted[1]);
        assertEquals(target[2], converted[2]);
        assertEquals(target[3], converted[3]);
        assertEquals(target[4], converted[4]);
    }

    @Test
    void testCreateIntArray() {
        assertNull(ArrayUtils.createIntArray(null));

        var source = new Object[]{"ok", 9834, null, "454", new StringBuffer("2355"), "ko", "2398", 4834L, "koko"};
        var target = new int[]{9834, 454, 2355, 2398, 4834};
        var converted = ArrayUtils.createIntArray(source);
        assertEquals(target.length, converted.length);
        assertEquals(target[0], converted[0]);
        assertEquals(target[1], converted[1]);
        assertEquals(target[2], converted[2]);
        assertEquals(target[3], converted[3]);
        assertEquals(target[4], converted[4]);
    }

    @Test
    void testCreateLongArray() {
        assertNull(ArrayUtils.createLongArray(null));

        var source = new Object[]{59035, "90465", "ok", "723479", null, "47543", "ko", 987543};
        var target = new long[]{59035, 90465, 723479, 47543, 987543};
        var converted = ArrayUtils.createLongArray(source);
        assertEquals(target.length, converted.length);
        assertEquals(target[0], converted[0]);
        assertEquals(target[1], converted[1]);
        assertEquals(target[2], converted[2]);
        assertEquals(target[3], converted[3]);
        assertEquals(target[4], converted[4]);
    }

    @Test
    void testCreateFloatArray() {
        assertNull(ArrayUtils.createFloatArray(null));

        var source = new Object[]{"228.02", 8734.3f, "lokoko", "8634.2", null, "kokiro", 34321.9d, "3478.2"};
        var target = new float[]{228.02f, 8734.3f, 8634.2f, 34321.9f, 3478.2f};
        var converted = ArrayUtils.createFloatArray(source);
        assertEquals(target.length, converted.length);
        assertEquals(target[0], converted[0], 0);
        assertEquals(target[1], converted[1], 0);
        assertEquals(target[2], converted[2], 0);
        assertEquals(target[3], converted[3], 0);
        assertEquals(target[4], converted[4], 0);
    }

    @Test
    void testCreateDoubleArray() {
        assertNull(ArrayUtils.createDoubleArray(null));

        var source = new Object[]{987634.3434d, null, "653928.434", "oooook", 394374.34387d, "3847764332.3434", "koooko", 3434};
        var target = new double[]{987634.3434d, 653928.434d, 394374.34387d, 3847764332.3434d, 3434d};
        var converted = ArrayUtils.createDoubleArray(source);
        assertEquals(target.length, converted.length);
        assertEquals(target[0], converted[0], 0);
        assertEquals(target[1], converted[1], 0);
        assertEquals(target[2], converted[2], 0);
        assertEquals(target[3], converted[3], 0);
        assertEquals(target[4], converted[4], 0);
    }

    @Test
    void testJoinString() {
        var first = new String[]{"lkjhkjh", "uhggh", "kgyugioh", "kjhgkhjh", "phhgg"};
        var second = new String[]{"ihhjf", "hhjgvgjfc", "oighiufhuf", "uiguhgi", "iuggiug"};

        assertNull(ArrayUtils.join((String[]) null, (String[]) null));
        assertSame(first, ArrayUtils.join(first, (String[]) null));
        assertSame(second, ArrayUtils.join((String[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second[0]);
        assertEquals(join[6], second[1]);
        assertEquals(join[7], second[2]);
        assertEquals(join[8], second[3]);
        assertEquals(join[9], second[4]);
    }

    @Test
    void testJoinStringSingle() {
        var first = new String[]{"lkjhkjh", "uhggh", "kgyugioh", "kjhgkhjh", "phhgg"};
        var second = "ihhjf";

        assertNull(ArrayUtils.join((String[]) null, (String) null));
        assertSame(first, ArrayUtils.join(first, (String) null));
        assertEquals(1, ArrayUtils.join((String[]) null, second).length);
        assertEquals(second, ArrayUtils.join((String[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second);
    }

    @Test
    void testJoinByte() {
        var first = new byte[]{2, 5, 5, 4, 6};
        var second = new byte[]{9, 4, 3, 6, 8};

        assertNull(ArrayUtils.join((byte[]) null, (byte[]) null));
        assertSame(first, ArrayUtils.join(first, (byte[]) null));
        assertSame(second, ArrayUtils.join((byte[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second[0]);
        assertEquals(join[6], second[1]);
        assertEquals(join[7], second[2]);
        assertEquals(join[8], second[3]);
        assertEquals(join[9], second[4]);
    }

    @Test
    void testJoinByteSingle() {
        var first = new byte[]{2, 5, 5, 4, 6};
        byte second = 9;

        assertEquals(1, ArrayUtils.join((byte[]) null, second).length);
        assertEquals(second, ArrayUtils.join((byte[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second);
    }

    @Test
    void testJoinChar() {
        var first = new char[]{'t', 'h', 'i', 's', ' '};
        var second = new char[]{'w', 'o', 'r', 'k', 's'};

        assertNull(ArrayUtils.join((char[]) null, (char[]) null));
        assertSame(first, ArrayUtils.join(first, (char[]) null));
        assertSame(second, ArrayUtils.join((char[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second[0]);
        assertEquals(join[6], second[1]);
        assertEquals(join[7], second[2]);
        assertEquals(join[8], second[3]);
        assertEquals(join[9], second[4]);
    }

    @Test
    void testJoinCharSingle() {
        var first = new char[]{'t', 'h', 'i', 's', ' '};
        var second = 'w';

        assertEquals(1, ArrayUtils.join((char[]) null, second).length);
        assertEquals(second, ArrayUtils.join((char[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second);
    }

    @Test
    void testJoinShort() {
        var first = new short[]{8, 3, 54, 23, 54};
        var second = new short[]{84, 23, 43, 12, 5};

        assertNull(ArrayUtils.join((short[]) null, (short[]) null));
        assertSame(first, ArrayUtils.join(first, (short[]) null));
        assertSame(second, ArrayUtils.join((short[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second[0]);
        assertEquals(join[6], second[1]);
        assertEquals(join[7], second[2]);
        assertEquals(join[8], second[3]);
        assertEquals(join[9], second[4]);
    }

    @Test
    void testJoinShortSingle() {
        var first = new short[]{8, 3, 54, 23, 54};
        short second = 84;

        assertEquals(1, ArrayUtils.join((short[]) null, second).length);
        assertEquals(second, ArrayUtils.join((short[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second);
    }

    @Test
    void testJoinInt() {
        var first = new int[]{834, 3476, 343, 234, 545};
        var second = new int[]{9834, 454, 2355, 2398, 4834};

        assertNull(ArrayUtils.join((int[]) null, (int[]) null));
        assertSame(first, ArrayUtils.join(first, (int[]) null));
        assertSame(second, ArrayUtils.join((int[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second[0]);
        assertEquals(join[6], second[1]);
        assertEquals(join[7], second[2]);
        assertEquals(join[8], second[3]);
        assertEquals(join[9], second[4]);
    }

    @Test
    void testJoinIntSingle() {
        var first = new int[]{834, 3476, 343, 234, 545};
        var second = 9834;

        assertEquals(1, ArrayUtils.join((int[]) null, second).length);
        assertEquals(second, ArrayUtils.join((int[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second);
    }

    @Test
    void testJoinLong() {
        var first = new long[]{987634, 98785, 54654, 9864, 4697932};
        var second = new long[]{59035, 90465, 723479, 47543, 987543};

        assertNull(ArrayUtils.join((long[]) null, (long[]) null));
        assertSame(first, ArrayUtils.join(first, (long[]) null));
        assertSame(second, ArrayUtils.join((long[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second[0]);
        assertEquals(join[6], second[1]);
        assertEquals(join[7], second[2]);
        assertEquals(join[8], second[3]);
        assertEquals(join[9], second[4]);
    }

    @Test
    void testJoinLongSingle() {
        var first = new long[]{987634, 98785, 54654, 9864, 4697932};
        long second = 59035;

        assertEquals(1, ArrayUtils.join((long[]) null, second).length);
        assertEquals(second, ArrayUtils.join((long[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second);
    }

    @Test
    void testJoinFloat() {
        var first = new float[]{43.3f, 7489.2f, 7634.98f, 343.8f, 736.9f};
        var second = new float[]{228.02f, 8734.3f, 8634.2f, 34321.9f, 3478.2f};

        assertNull(ArrayUtils.join((float[]) null, (float[]) null));
        assertSame(first, ArrayUtils.join(first, (float[]) null));
        assertSame(second, ArrayUtils.join((float[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0], 0);
        assertEquals(join[1], first[1], 0);
        assertEquals(join[2], first[2], 0);
        assertEquals(join[3], first[3], 0);
        assertEquals(join[4], first[4], 0);
        assertEquals(join[5], second[0], 0);
        assertEquals(join[6], second[1], 0);
        assertEquals(join[7], second[2], 0);
        assertEquals(join[8], second[3], 0);
        assertEquals(join[9], second[4], 0);
    }

    @Test
    void testJoinFloatSingle() {
        var first = new float[]{43.3f, 7489.2f, 7634.98f, 343.8f, 736.9f};
        var second = 228.02f;

        assertEquals(1, ArrayUtils.join((float[]) null, second).length);
        assertEquals(second, ArrayUtils.join((float[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0], 0);
        assertEquals(join[1], first[1], 0);
        assertEquals(join[2], first[2], 0);
        assertEquals(join[3], first[3], 0);
        assertEquals(join[4], first[4], 0);
        assertEquals(join[5], second, 0);
    }

    @Test
    void testJoinDouble() {
        var first = new double[]{973284.678943d, 8936498736.232d, 78634.9834d, 37467.334d, 986347.234243d};
        var second = new double[]{987634.3434d, 653928.434d, 394374.34387d, 3847764332.3434d, 3434d};

        assertNull(ArrayUtils.join((double[]) null, (double[]) null));
        assertSame(first, ArrayUtils.join(first, (double[]) null));
        assertSame(second, ArrayUtils.join((double[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0], 0);
        assertEquals(join[1], first[1], 0);
        assertEquals(join[2], first[2], 0);
        assertEquals(join[3], first[3], 0);
        assertEquals(join[4], first[4], 0);
        assertEquals(join[5], second[0], 0);
        assertEquals(join[6], second[1], 0);
        assertEquals(join[7], second[2], 0);
        assertEquals(join[8], second[3], 0);
        assertEquals(join[9], second[4], 0);
    }

    @Test
    void testJoinDoubleSingle() {
        var first = new double[]{973284.678943d, 8936498736.232d, 78634.9834d, 37467.334d, 986347.234243d};
        var second = 987634.3434d;

        assertEquals(1, ArrayUtils.join((double[]) null, second).length);
        assertEquals(second, ArrayUtils.join((double[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0], 0);
        assertEquals(join[1], first[1], 0);
        assertEquals(join[2], first[2], 0);
        assertEquals(join[3], first[3], 0);
        assertEquals(join[4], first[4], 0);
        assertEquals(join[5], second, 0);
    }

    @Test
    void testJoinBoolean() {
        var first = new boolean[]{true, false, false, true, true};
        var second = new boolean[]{false, false, true, false, true};

        assertNull(ArrayUtils.join((boolean[]) null, (boolean[]) null));
        assertSame(first, ArrayUtils.join(first, (boolean[]) null));
        assertSame(second, ArrayUtils.join((boolean[]) null, second));

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second[0]);
        assertEquals(join[6], second[1]);
        assertEquals(join[7], second[2]);
        assertEquals(join[8], second[3]);
        assertEquals(join[9], second[4]);
    }

    @Test
    void testJoinBooleanSingle() {
        var first = new boolean[]{true, false, false, true, true};
        var second = false;

        assertEquals(1, ArrayUtils.join((boolean[]) null, second).length);
        assertEquals(second, ArrayUtils.join((boolean[]) null, second)[0]);

        var join = ArrayUtils.join(first, second);
        assertEquals(join[0], first[0]);
        assertEquals(join[1], first[1]);
        assertEquals(join[2], first[2]);
        assertEquals(join[3], first[3]);
        assertEquals(join[4], first[4]);
        assertEquals(join[5], second);
    }
}
