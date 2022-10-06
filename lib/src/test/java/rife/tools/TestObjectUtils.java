/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestObjectUtils {
    @Test
    public void testGenericClone() {
        assertNull(ObjectUtils.genericClone(null));

        Object object_orig = new Object();
        Object object_copy = ObjectUtils.genericClone(object_orig);
        assertSame(object_orig, object_copy);

        String string_orig = "the string";
        String string_copy = ObjectUtils.genericClone(string_orig);
        assertSame(string_orig, string_copy);

        Exception exception_orig = new Exception("the string");
        Exception exception_copy = ObjectUtils.genericClone(exception_orig);
        assertSame(exception_orig, exception_copy);

        StringBuffer stringbuffer_orig = new StringBuffer("the string");
        StringBuffer stringbuffer_copy = ObjectUtils.genericClone(stringbuffer_orig);
        assertNotSame(stringbuffer_orig, stringbuffer_copy);
        assertEquals(stringbuffer_orig.toString(), stringbuffer_copy.toString());

        Byte byte_orig = (byte) 1;
        Byte byte_copy = ObjectUtils.genericClone(byte_orig);
        assertSame(byte_orig, byte_copy);

        Short short_orig = (short) 1;
        Short short_copy = ObjectUtils.genericClone(short_orig);
        assertSame(short_orig, short_copy);

        Integer integer_orig = 1;
        Integer integer_copy = ObjectUtils.genericClone(integer_orig);
        assertSame(integer_orig, integer_copy);

        Long long_orig = 1L;
        Long long_copy = ObjectUtils.genericClone(long_orig);
        assertSame(long_orig, long_copy);

        Float float_orig = 1F;
        Float float_copy = ObjectUtils.genericClone(float_orig);
        assertSame(float_orig, float_copy);

        Double double_orig = 1.0;
        Double double_copy = ObjectUtils.genericClone(double_orig);
        assertSame(double_orig, double_copy);

        Boolean boolean_orig = Boolean.FALSE;
        Boolean boolean_copy = ObjectUtils.genericClone(boolean_orig);
        assertSame(boolean_orig, boolean_copy);

        Character character_orig = 'a';
        Character character_copy = ObjectUtils.genericClone(character_orig);
        assertSame(character_orig, character_copy);

        Uncloneable uncloneable_orig = new Uncloneable(89);
        assertNull(ObjectUtils.genericClone(uncloneable_orig));

        ProtectedCloneable protected_cloneable_orig = new ProtectedCloneable(89);
        assertNull(ObjectUtils.genericClone(protected_cloneable_orig));

        ArrayList<String> string_collection_orig = new ArrayList<String>();
        string_collection_orig.add("one");
        string_collection_orig.add("two");
        ArrayList<String> string_collection_copy = ObjectUtils.genericClone(string_collection_orig);
        assertNotSame(string_collection_orig, string_collection_copy);
        assertEquals(string_collection_orig.size(), string_collection_copy.size());
        assertSame(string_collection_orig.get(0), string_collection_copy.get(0));
        assertSame(string_collection_orig.get(1), string_collection_copy.get(1));

        Date cloneable_orig = new Date();
        Date cloneable_copy = ObjectUtils.genericClone(cloneable_orig);
        assertNotSame(cloneable_orig, cloneable_copy);
        assertEquals(cloneable_orig.getDate(), cloneable_copy.getDate());

        ArrayList<Date> date_collection_orig = new ArrayList<Date>();
        date_collection_orig.add(new Date());
        date_collection_orig.add(new Date());
        ArrayList<Date> date_collection_copy = ObjectUtils.genericClone(date_collection_orig);
        assertNotSame(date_collection_orig, date_collection_copy);
        assertEquals(date_collection_orig.size(), date_collection_copy.size());
        assertSame(date_collection_orig.get(0), date_collection_copy.get(0));
        assertSame(date_collection_orig.get(1), date_collection_copy.get(1));
    }

    @Test
    public void testDeepClone() {
        try {
            assertNull(ObjectUtils.deepClone(null));

            Object object_orig = new Object();
            Object object_copy = ObjectUtils.deepClone(object_orig);
            assertSame(object_orig, object_copy);

            String string_orig = "the string";
            String string_copy = ObjectUtils.deepClone(string_orig);
            assertSame(string_orig, string_copy);

            Date cloneable_orig = new Date();
            Date cloneable_copy = ObjectUtils.deepClone(cloneable_orig);
            assertNotSame(cloneable_orig, cloneable_copy);
            assertEquals(cloneable_orig.getDate(), cloneable_copy.getDate());

            Uncloneable uncloneable_orig = new Uncloneable(89);
            try {
                ObjectUtils.deepClone(uncloneable_orig);
                fail();
            } catch (CloneNotSupportedException e) {
                assertTrue(true);
            }

            ProtectedCloneable protected_cloneable_orig = new ProtectedCloneable(89);
            try {
                ObjectUtils.deepClone(protected_cloneable_orig);
                fail();
            } catch (CloneNotSupportedException e) {
                assertTrue(true);
            }

            boolean[] boolean_array_orig = new boolean[]{true, false, true};
            boolean[] boolean_array_copy = ObjectUtils.deepClone(boolean_array_orig);
            assertNotSame(boolean_array_orig, boolean_array_copy);
            assertEquals(boolean_array_orig.length, boolean_array_copy.length);
            assertEquals(boolean_array_orig[0], boolean_array_orig[0]);
            assertEquals(boolean_array_orig[1], boolean_array_orig[1]);
            assertEquals(boolean_array_orig[2], boolean_array_orig[2]);

            byte[] byte_array_orig = new byte[]{(byte) 7, (byte) 23};
            byte[] byte_array_copy = ObjectUtils.deepClone(byte_array_orig);
            assertNotSame(byte_array_orig, byte_array_copy);
            assertEquals(byte_array_orig.length, byte_array_copy.length);
            assertEquals(byte_array_orig[0], byte_array_copy[0]);
            assertEquals(byte_array_orig[1], byte_array_copy[1]);

            char[] char_array_orig = new char[]{'k', 'I'};
            char[] char_array_copy = ObjectUtils.deepClone(char_array_orig);
            assertNotSame(char_array_orig, char_array_copy);
            assertEquals(char_array_orig.length, char_array_copy.length);
            assertEquals(char_array_orig[0], char_array_copy[0]);
            assertEquals(char_array_orig[1], char_array_copy[1]);

            short[] short_array_orig = new short[]{(short) 77, (short) 9};
            short[] short_array_copy = ObjectUtils.deepClone(short_array_orig);
            assertNotSame(short_array_orig, short_array_copy);
            assertEquals(short_array_orig.length, short_array_copy.length);
            assertEquals(short_array_orig[0], short_array_copy[0]);
            assertEquals(short_array_orig[1], short_array_copy[1]);

            int[] int_array_orig = new int[]{879, 86, 13, 89};
            int[] int_array_copy = ObjectUtils.deepClone(int_array_orig);
            assertNotSame(int_array_orig, int_array_copy);
            assertEquals(int_array_orig.length, int_array_copy.length);
            assertEquals(int_array_orig[0], int_array_copy[0]);
            assertEquals(int_array_orig[1], int_array_copy[1]);
            assertEquals(int_array_orig[2], int_array_copy[2]);
            assertEquals(int_array_orig[3], int_array_copy[3]);

            long[] long_array_orig = new long[]{869523L, 913437L};
            long[] long_array_copy = ObjectUtils.deepClone(long_array_orig);
            assertNotSame(long_array_orig, long_array_copy);
            assertEquals(long_array_orig.length, long_array_copy.length);
            assertEquals(long_array_orig[0], long_array_copy[0]);
            assertEquals(long_array_orig[1], long_array_copy[1]);

            float[] float_array_orig = new float[]{89.4f, 1123.9f, 1.1f};
            float[] float_array_copy = ObjectUtils.deepClone(float_array_orig);
            assertNotSame(float_array_orig, float_array_copy);
            assertEquals(float_array_orig.length, float_array_copy.length);
            assertEquals(float_array_orig[0], float_array_copy[0]);
            assertEquals(float_array_orig[1], float_array_copy[1]);
            assertEquals(float_array_orig[2], float_array_copy[2]);

            double[] double_array_orig = new double[]{87986.121d, 979121.d};
            double[] double_array_copy = ObjectUtils.deepClone(double_array_orig);
            assertNotSame(double_array_orig, double_array_copy);
            assertEquals(double_array_orig.length, double_array_copy.length);
            assertEquals(double_array_orig[0], double_array_copy[0]);
            assertEquals(double_array_orig[1], double_array_copy[1]);

            String[] string_array_orig = new String[]{"one", "two"};
            String[] string_array_copy = ObjectUtils.deepClone(string_array_orig);
            assertNotSame(string_array_orig, string_array_copy);
            assertEquals(string_array_orig.length, string_array_copy.length);
            assertSame(string_array_orig[0], string_array_copy[0]);
            assertSame(string_array_orig[1], string_array_copy[1]);

            Date[] date_array_orig = new Date[]{new Date(), new Date()};
            Date[] date_array_copy = ObjectUtils.deepClone(date_array_orig);
            assertNotSame(date_array_orig, date_array_copy);
            assertEquals(date_array_orig.length, date_array_copy.length);
            assertNotSame(date_array_orig[0], date_array_copy[0]);
            assertNotSame(date_array_orig[1], date_array_copy[1]);
            assertEquals(date_array_orig[0], date_array_copy[0]);
            assertEquals(date_array_orig[1], date_array_copy[1]);

            long[][] long_multiarray_orig = new long[][]{{869523L}, {2323L}};
            long[][] long_multiarray_copy = ObjectUtils.deepClone(long_multiarray_orig);
            assertNotSame(long_multiarray_orig, long_multiarray_copy);
            assertEquals(long_multiarray_orig.length, long_multiarray_copy.length);
            assertNotSame(long_multiarray_orig[0], long_multiarray_copy[0]);
            assertNotSame(long_multiarray_orig[1], long_multiarray_copy[1]);
            assertEquals(long_multiarray_orig[0].length, long_multiarray_copy[0].length);
            assertEquals(long_multiarray_orig[1].length, long_multiarray_copy[1].length);
            assertNotSame(long_multiarray_orig[0][0], long_multiarray_copy[0][0]);
            assertNotSame(long_multiarray_orig[1][0], long_multiarray_copy[1][0]);
            assertEquals(long_multiarray_orig[0][0], long_multiarray_copy[0][0]);
            assertEquals(long_multiarray_orig[1][0], long_multiarray_copy[1][0]);

            String[][][] string_multiarray_orig = new String[][][]{{{"000", "001"}, {"010", "011"}, {"020", "021"}}, {{"100", "101"}, {"110", "111"}, {"120", "121"}}};
            String[][][] string_multiarray_copy = ObjectUtils.deepClone(string_multiarray_orig);
            assertNotSame(string_multiarray_orig, string_multiarray_copy);
            assertEquals(string_multiarray_orig.length, string_multiarray_copy.length);
            assertNotSame(string_multiarray_orig[0], string_multiarray_copy[0]);
            assertNotSame(string_multiarray_orig[1], string_multiarray_copy[1]);
            assertEquals(string_multiarray_orig[0].length, string_multiarray_copy[0].length);
            assertNotSame(string_multiarray_orig[0][0], string_multiarray_copy[0][0]);
            assertNotSame(string_multiarray_orig[0][1], string_multiarray_copy[0][1]);
            assertEquals(string_multiarray_orig[1].length, string_multiarray_copy[1].length);
            assertNotSame(string_multiarray_orig[1][0], string_multiarray_copy[1][0]);
            assertNotSame(string_multiarray_orig[1][1], string_multiarray_copy[1][1]);
            assertSame(string_multiarray_copy[0][0][0], string_multiarray_copy[0][0][0]);
            assertSame(string_multiarray_copy[0][0][1], string_multiarray_copy[0][0][1]);
            assertSame(string_multiarray_copy[0][1][0], string_multiarray_copy[0][1][0]);
            assertSame(string_multiarray_copy[0][1][1], string_multiarray_copy[0][1][1]);
            assertSame(string_multiarray_copy[0][2][0], string_multiarray_copy[0][2][0]);
            assertSame(string_multiarray_copy[0][2][1], string_multiarray_copy[0][2][1]);
            assertSame(string_multiarray_copy[1][0][0], string_multiarray_copy[1][0][0]);
            assertSame(string_multiarray_copy[1][0][1], string_multiarray_copy[1][0][1]);
            assertSame(string_multiarray_copy[1][1][0], string_multiarray_copy[1][1][0]);
            assertSame(string_multiarray_copy[1][1][1], string_multiarray_copy[1][1][1]);
            assertSame(string_multiarray_copy[1][2][0], string_multiarray_copy[1][2][0]);
            assertSame(string_multiarray_copy[1][2][1], string_multiarray_copy[1][2][1]);
            assertEquals(string_multiarray_copy[0][0][0], string_multiarray_copy[0][0][0]);
            assertEquals(string_multiarray_copy[0][0][1], string_multiarray_copy[0][0][1]);
            assertEquals(string_multiarray_copy[0][1][0], string_multiarray_copy[0][1][0]);
            assertEquals(string_multiarray_copy[0][1][1], string_multiarray_copy[0][1][1]);
            assertEquals(string_multiarray_copy[0][2][0], string_multiarray_copy[0][2][0]);
            assertEquals(string_multiarray_copy[0][2][1], string_multiarray_copy[0][2][1]);
            assertEquals(string_multiarray_copy[1][0][0], string_multiarray_copy[1][0][0]);
            assertEquals(string_multiarray_copy[1][0][1], string_multiarray_copy[1][0][1]);
            assertEquals(string_multiarray_copy[1][1][0], string_multiarray_copy[1][1][0]);
            assertEquals(string_multiarray_copy[1][1][1], string_multiarray_copy[1][1][1]);
            assertEquals(string_multiarray_copy[1][2][0], string_multiarray_copy[1][2][0]);
            assertEquals(string_multiarray_copy[1][2][1], string_multiarray_copy[1][2][1]);

            Date[][] date_multiarray_orig = new Date[][]{{new Date()}, {new Date()}};
            Date[][] date_multiarray_copy = ObjectUtils.deepClone(date_multiarray_orig);
            assertNotSame(date_multiarray_orig, date_multiarray_copy);
            assertEquals(date_multiarray_orig.length, date_multiarray_copy.length);
            assertNotSame(date_multiarray_orig[0], date_multiarray_copy[0]);
            assertNotSame(date_multiarray_orig[1], date_multiarray_copy[1]);
            assertNotSame(date_multiarray_orig[0][0], date_multiarray_copy[0][0]);
            assertNotSame(date_multiarray_orig[1][0], date_multiarray_copy[1][0]);
            assertEquals(date_multiarray_orig[0][0], date_multiarray_copy[0][0]);
            assertEquals(date_multiarray_orig[1][0], date_multiarray_copy[1][0]);

            ArrayList<String> string_collection_orig = new ArrayList<String>();
            string_collection_orig.add("one");
            string_collection_orig.add("two");
            ArrayList<String> string_collection_copy = ObjectUtils.deepClone(string_collection_orig);
            assertNotSame(string_collection_orig, string_collection_copy);
            assertEquals(string_collection_orig.size(), string_collection_copy.size());
            assertSame(string_collection_orig.get(0), string_collection_copy.get(0));
            assertSame(string_collection_orig.get(1), string_collection_copy.get(1));

            ArrayList<Date> date_collection_orig = new ArrayList<Date>();
            date_collection_orig.add(new Date());
            date_collection_orig.add(new Date());
            ArrayList<Date> date_collection_copy = ObjectUtils.deepClone(date_collection_orig);
            assertNotSame(date_collection_orig, date_collection_copy);
            assertEquals(date_collection_orig.size(), date_collection_copy.size());
            assertNotSame(date_collection_orig.get(0), date_collection_copy.get(0));
            assertNotSame(date_collection_orig.get(1), date_collection_copy.get(1));
            assertEquals(date_collection_orig.get(0), date_collection_copy.get(0));
            assertEquals(date_collection_orig.get(1), date_collection_copy.get(1));

            LinkedHashMap<String, String> string_map_orig = new LinkedHashMap<String, String>();
            string_map_orig.put("k1", "v1");
            string_map_orig.put("k2", "v2");
            string_map_orig.put("k3", "v3");
            LinkedHashMap<String, String> string_map_copy = ObjectUtils.deepClone(string_map_orig);
            assertNotSame(string_map_orig, string_map_copy);
            assertEquals(string_map_orig.size(), string_map_copy.size());
            Set<Map.Entry<String, String>> string_map_orig_entries = string_map_orig.entrySet();
            Iterator<Map.Entry<String, String>> string_map_orig_entries_it = string_map_orig_entries.iterator();
            Set<Map.Entry<String, String>> string_map_copy_entries = string_map_copy.entrySet();
            Iterator<Map.Entry<String, String>> string_map_copy_entries_it = string_map_copy_entries.iterator();
            Map.Entry<String, String> string_map_orig_entry = null;
            Map.Entry<String, String> string_map_copy_entry = null;
            while (string_map_orig_entries_it.hasNext()) {
                string_map_orig_entry = string_map_orig_entries_it.next();
                string_map_copy_entry = string_map_copy_entries_it.next();

                assertSame(string_map_orig_entry.getKey(), string_map_copy_entry.getKey());
                assertSame(string_map_orig_entry.getValue(), string_map_copy_entry.getValue());
            }

            LinkedHashMap<Date, Date> date_map_orig = new LinkedHashMap<Date, Date>();
            date_map_orig.put(new Date(), new Date());
            date_map_orig.put(new Date(), new Date());
            date_map_orig.put(new Date(), new Date());
            LinkedHashMap<Date, Date> date_map_copy = ObjectUtils.deepClone(date_map_orig);
            assertNotSame(date_map_orig, date_map_copy);
            assertEquals(date_map_orig.size(), date_map_copy.size());
            Set<Map.Entry<Date, Date>> date_map_orig_entries = date_map_orig.entrySet();
            Iterator<Map.Entry<Date, Date>> date_map_orig_entries_it = date_map_orig_entries.iterator();
            Set<Map.Entry<Date, Date>> date_map_copy_entries = date_map_copy.entrySet();
            Iterator<Map.Entry<Date, Date>> date_map_copy_entries_it = date_map_copy_entries.iterator();
            Map.Entry<Date, Date> date_map_orig_entry = null;
            Map.Entry<Date, Date> date_map_copy_entry = null;
            while (date_map_orig_entries_it.hasNext()) {
                date_map_orig_entry = date_map_orig_entries_it.next();
                date_map_copy_entry = date_map_copy_entries_it.next();

                assertNotSame(date_map_orig_entry.getKey(), date_map_copy_entry.getKey());
                assertEquals(date_map_orig_entry.getKey(), date_map_copy_entry.getKey());
                assertNotSame(date_map_orig_entry.getValue(), date_map_copy_entry.getValue());
                assertEquals(date_map_orig_entry.getValue(), date_map_copy_entry.getValue());
            }
        } catch (CloneNotSupportedException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetBaseClass() {
        assertSame(Void.TYPE, ObjectUtils.getBaseClass(null));
        assertSame(Object.class, ObjectUtils.getBaseClass(new Object()));
        assertSame(String.class, ObjectUtils.getBaseClass(""));
        assertSame(short.class, ObjectUtils.getBaseClass(new short[0]));
        assertSame(int.class, ObjectUtils.getBaseClass(new int[0]));
        assertSame(long.class, ObjectUtils.getBaseClass(new long[0]));
        assertSame(float.class, ObjectUtils.getBaseClass(new float[0]));
        assertSame(double.class, ObjectUtils.getBaseClass(new double[0]));
        assertSame(boolean.class, ObjectUtils.getBaseClass(new boolean[0]));
        assertSame(char.class, ObjectUtils.getBaseClass(new char[0]));
        assertSame(byte.class, ObjectUtils.getBaseClass(new byte[0]));
        assertSame(String.class, ObjectUtils.getBaseClass(new String[0]));
        assertSame(short.class, ObjectUtils.getBaseClass(new short[0][1]));
        assertSame(Integer.class, ObjectUtils.getBaseClass(new Integer[7][8]));
    }
}

class ProtectedCloneable extends Uncloneable implements Cloneable {
    public ProtectedCloneable(int value) {
        super(value);
    }

    protected ProtectedCloneable clone()
        throws CloneNotSupportedException {
        return (ProtectedCloneable) super.clone();
    }
}

class Uncloneable {
    private int mValue = 0;

    public Uncloneable(int value) {
        mValue = value;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
