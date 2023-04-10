/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.validation.ConstrainedProperty;

import java.util.logging.Logger;

/**
 * General purpose class containing common array manipulation methods.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public final class ArrayUtils {
    public enum ArrayType {
        NO_ARRAY,
        OBJECT_ARRAY,
        BYTE_ARRAY,
        BOOLEAN_ARRAY,
        CHAR_ARRAY,
        SHORT_ARRAY,
        INT_ARRAY,
        LONG_ARRAY,
        FLOAT_ARRAY,
        DOUBLE_ARRAY
    }

    private ArrayUtils() {
        // no-op
    }

    /**
     * Determines the type of the array based on the object's class name.
     *
     * @param object the object to check if it is an array
     * @return an ArrayType value indicating the type of the array, or NO_ARRAY if object is not an array
     * @throws NullPointerException if object is null
     * @since 1.0
     */
    public static ArrayType getArrayType(Object object) {
        var classname = object.getClass().getName();

        // check if it's an array
        if ('[' == classname.charAt(0)) {
            for (var position = 1; position < classname.length(); position++) {
                if ('[' == classname.charAt(position)) {
                    continue;
                }

                switch (classname.charAt(position)) {
                    case 'L' -> {
                        return ArrayType.OBJECT_ARRAY;
                    }
                    case 'Z' -> {
                        return ArrayType.BOOLEAN_ARRAY;
                    }
                    case 'B' -> {
                        return ArrayType.BYTE_ARRAY;
                    }
                    case 'C' -> {
                        return ArrayType.CHAR_ARRAY;
                    }
                    case 'S' -> {
                        return ArrayType.SHORT_ARRAY;
                    }
                    case 'I' -> {
                        return ArrayType.INT_ARRAY;
                    }
                    case 'J' -> {
                        return ArrayType.LONG_ARRAY;
                    }
                    case 'F' -> {
                        return ArrayType.FLOAT_ARRAY;
                    }
                    case 'D' -> {
                        return ArrayType.DOUBLE_ARRAY;
                    }
                    default -> {
                        Logger.getLogger("rife.tools").severe("Unknown primitive array class: " + classname);
                        return null;
                    }
                }
            }
            return null;
        }

        return ArrayType.NO_ARRAY;
    }

    /**
     * Convert an {@code Object} to a textual representation in a
     * {@code String} array.
     * <p>
     * Note that array of type byte[] are explicitly not converted since that
     * would result in many binary data to create OutOfMemoryError exceptions.
     *
     * @param source The {@code Object} to convert.
     * @return The resulting {@code String} array; or
     * <p>
     * {@code null} if {@code source} is {@code null}.
     * @since 1.0
     */
    public static String[] createStringArray(Object source, ConstrainedProperty constrainedProperty) {
        if (null == source) {
            return null;
        }

        String[] result = null;

        var type = getArrayType(source);

        if (type == ArrayType.NO_ARRAY) {
            result = new String[]{BeanUtils.formatPropertyValue(source, constrainedProperty)};
        } else if (type == ArrayType.BYTE_ARRAY) {
            // explicitly don't convert byte arrays since they are often used
            // to store binary data and converting them to a string array
            // would result in an OutOfMemoryError exception easily
            result = null;
        } else if (type == ArrayType.OBJECT_ARRAY) {
            result = ArrayUtils.createStringArray((Object[]) source, constrainedProperty);
        } else if (type == ArrayType.BOOLEAN_ARRAY) {
            result = ArrayUtils.createStringArray((boolean[]) source, constrainedProperty);
        } else if (type == ArrayType.CHAR_ARRAY) {
            result = ArrayUtils.createStringArray((char[]) source, constrainedProperty);
        } else if (type == ArrayType.SHORT_ARRAY) {
            result = ArrayUtils.createStringArray((short[]) source, constrainedProperty);
        } else if (type == ArrayType.INT_ARRAY) {
            result = ArrayUtils.createStringArray((int[]) source, constrainedProperty);
        } else if (type == ArrayType.LONG_ARRAY) {
            result = ArrayUtils.createStringArray((long[]) source, constrainedProperty);
        } else if (type == ArrayType.FLOAT_ARRAY) {
            result = ArrayUtils.createStringArray((float[]) source, constrainedProperty);
        } else if (type == ArrayType.DOUBLE_ARRAY) {
            result = ArrayUtils.createStringArray((double[]) source, constrainedProperty);
        }

        return result;
    }

    /**
     * Creates a new string array containing formatted string values of the input object array.
     *
     * @param array the input object array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input object array
     * @since 1.0
     */
    public static String[] createStringArray(Object[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input object array.
     *
     * @param array               the input object array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input object array
     * @since 1.0
     */
    public static String[] createStringArray(Object[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates a new string array containing formatted string values of the input boolean array.
     *
     * @param array the input boolean array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input boolean array
     * @since 1.0
     */
    public static String[] createStringArray(boolean[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input boolean array.
     *
     * @param array               the input boolean array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input boolean array
     * @since 1.0
     */
    public static String[] createStringArray(boolean[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates a new string array containing formatted string values of the input byte array.
     *
     * @param array the input byte array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input byte array
     * @since 1.0
     */
    public static String[] createStringArray(byte[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input byte array.
     *
     * @param array               the input byte array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input byte array
     * @since 1.0
     */
    public static String[] createStringArray(byte[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates a new string array containing formatted string values of the input char array.
     *
     * @param array the input char array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input char array
     * @since 1.0
     */
    public static String[] createStringArray(char[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input char array.
     *
     * @param array               the input char array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input char array
     * @since 1.0
     */
    public static String[] createStringArray(char[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates a new string array containing formatted string values of the input short array.
     *
     * @param array the input short array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input short array
     * @since 1.0
     */
    public static String[] createStringArray(short[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input short array.
     *
     * @param array               the input short array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input short array
     * @since 1.0
     */
    public static String[] createStringArray(short[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates a new string array containing formatted string values of the input int array.
     *
     * @param array the input int array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input int array
     * @since 1.0
     */
    public static String[] createStringArray(int[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input int array.
     *
     * @param array               the input int array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input int array
     * @since 1.0
     */
    public static String[] createStringArray(int[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates a new string array containing formatted string values of the input long array.
     *
     * @param array the input long array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input long array
     * @since 1.0
     */
    public static String[] createStringArray(long[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input long array.
     *
     * @param array               the input long array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input long array
     * @since 1.0
     */
    public static String[] createStringArray(long[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates a new string array containing formatted string values of the input float array.
     *
     * @param array the input float array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input float array
     * @since 1.0
     */
    public static String[] createStringArray(float[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input float array.
     *
     * @param array               the input float array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input float array
     * @since 1.0
     */
    public static String[] createStringArray(float[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates a new string array containing formatted string values of the input double array.
     *
     * @param array the input double array, can be empty or {@code null}
     * @return a new string array containing formatted string values of the input double array
     * @since 1.0
     */
    public static String[] createStringArray(double[] array) {
        return createStringArray(array, null);
    }

    /**
     * Creates a new string array containing formatted string values of the input double array.
     *
     * @param array               the input double array, can be empty or {@code null}
     * @param constrainedProperty the optional {@link ConstrainedProperty} object containing constraints for formatting the property value
     * @return a new string array containing formatted string values of the input double array
     * @since 1.0
     */
    public static String[] createStringArray(double[] array, ConstrainedProperty constrainedProperty) {
        if (null == array) {
            return null;
        }

        var new_array = new String[array.length];

        for (var i = 0; i < array.length; i++) {
            new_array[i] = BeanUtils.formatPropertyValue(array[i], constrainedProperty);
        }

        return new_array;
    }

    /**
     * Creates an array of booleans from an array of objects.
     *
     * @param array the array to create a boolean array from
     * @return a new boolean array that contains the converted values of the original array,
     * or null if the original array is null
     * @since 1.0
     */
    public static boolean[] createBooleanArray(Object[] array) {
        if (null == array) {
            return null;
        }

        var new_array = new boolean[0];

        var converted_boolean = false;
        for (var element : array) {
            if (element != null) {
                converted_boolean = Boolean.parseBoolean(String.valueOf(element));
                new_array = join(new_array, converted_boolean);
            }
        }

        return new_array;
    }

    /**
     * Creates an array of bytes from an array of objects.
     *
     * @param array the array to create a byte array from
     * @return a new byte array that contains the converted values of the original array,
     * or null if the original array is null
     * @since 1.0
     */
    public static byte[] createByteArray(Object[] array) {
        if (null == array) {
            return null;
        }

        var new_array = new byte[0];

        byte converted_byte = -1;
        for (var element : array) {
            try {
                if (element != null) {
                    converted_byte = Byte.parseByte(String.valueOf(element));
                    new_array = join(new_array, converted_byte);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return new_array;
    }

    /**
     * Creates an array of chars from an array of objects.
     *
     * @param array the array to create a char array from
     * @return a new char array that contains the converted values of the original array,
     * or null if the original array is null
     * @since 1.0
     */
    public static char[] createCharArray(Object[] array) {
        if (null == array) {
            return null;
        }

        var new_array = new char[0];

        var converted_char = '\u0000';
        for (var element : array) {
            if (element != null) {
                var string_value = String.valueOf(element);
                if (string_value.length() != 1) {
                    continue;
                }
                converted_char = string_value.charAt(0);
                new_array = join(new_array, converted_char);
            }
        }

        return new_array;
    }

    /**
     * Creates an array of shorts from an array of objects.
     *
     * @param array the array to create a short array from
     * @return a new short array that contains the converted values of the original array,
     * or null if the original array is null
     * @since 1.0
     */
    public static short[] createShortArray(Object[] array) {
        if (null == array) {
            return null;
        }

        var new_array = new short[0];

        short converted_short = -1;
        for (var element : array) {
            try {
                if (element != null) {
                    converted_short = Short.parseShort(String.valueOf(element));
                    new_array = join(new_array, converted_short);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return new_array;
    }

    /**
     * Creates an array of ints from an array of objects.
     *
     * @param array the array to create an int array from
     * @return a new int array that contains the converted values of the original array,
     * or null if the original array is null
     * @since 1.0
     */
    public static int[] createIntArray(Object[] array) {
        if (null == array) {
            return null;
        }

        var new_array = new int[0];

        var converted_int = -1;
        for (var element : array) {
            try {
                if (element != null) {
                    converted_int = Integer.parseInt(String.valueOf(element));
                    new_array = join(new_array, converted_int);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return new_array;
    }

    /**
     * Creates an array of longs from an array of objects.
     *
     * @param array the array to create a long array from
     * @return a new long array that contains the converted values of the original array,
     * or null if the original array is null
     * @since 1.0
     */
    public static long[] createLongArray(Object[] array) {
        if (null == array) {
            return null;
        }

        var new_array = new long[0];

        long converted_long = -1;
        for (var element : array) {
            try {
                if (element != null) {
                    converted_long = Long.parseLong(String.valueOf(element));
                    new_array = join(new_array, converted_long);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return new_array;
    }

    /**
     * Creates an array of floats from an array of objects.
     *
     * @param array the array to create a float array from
     * @return a new float array that contains the converted values of the original array,
     * or null if the original array is null
     * @since 1.0
     */
    public static float[] createFloatArray(Object[] array) {
        if (null == array) {
            return null;
        }

        var new_array = new float[0];

        float converted_float = -1;
        for (var element : array) {
            try {
                if (element != null) {
                    converted_float = Float.parseFloat(String.valueOf(element));
                    new_array = join(new_array, converted_float);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return new_array;
    }

    /**
     * Creates an array of doubles from an array of objects.
     *
     * @param array the array to create a double array from
     * @return a new double array that contains the converted values of the original array,
     * or null if the original array is null
     * @since 1.0
     */
    public static double[] createDoubleArray(Object[] array) {
        if (null == array) {
            return null;
        }

        var new_array = new double[0];

        double converted_double = -1;
        for (var element : array) {
            try {
                if (element != null) {
                    converted_double = Double.parseDouble(String.valueOf(element));
                    new_array = join(new_array, converted_double);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return new_array;
    }

    /**
     * Joins two String arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first String array to join
     * @param second the second String array to join
     * @return a new String array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static String[] join(String[] first, String[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new String[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single String and a String array into a new array containing all elements.
     *
     * @param first  the String array to which to append the second String
     * @param second the String to append to the first array
     * @return a new String array with every element of the input array and the appended String,
     * or a new array containing only the second String if the input array is null
     * @since 1.0
     */
    public static String[] join(String[] first, String second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return new String[]{second};
        }
        if (null == second) {
            return first;
        }

        var new_array = new String[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }

    /**
     * Joins two byte arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first byte array to join
     * @param second the second byte array to join
     * @return a new byte array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static byte[] join(byte[] first, byte[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new byte[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single byte and a byte array into a new array containing all elements.
     *
     * @param first  the byte array to which to append the second byte
     * @param second the byte to append to the first array
     * @return a new byte array with every element of the input array and the appended byte,
     * or a new array containing only the second byte if the input array is null
     * @since 1.0
     */
    public static byte[] join(byte[] first, byte second) {
        if (null == first) {
            return new byte[]{second};
        }

        var new_array = new byte[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }

    /**
     * Joins two char arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first char array to join
     * @param second the second char array to join
     * @return a new char array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static char[] join(char[] first, char[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new char[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single char and a char array into a new array containing all elements.
     *
     * @param first  the char array to which to append the second char
     * @param second the char to append to the first array
     * @return a new char array with every element of the input array and the appended char,
     * or a new array containing only the second char if the input array is null
     * @since 1.0
     */
    public static char[] join(char[] first, char second) {
        if (null == first) {
            return new char[]{second};
        }

        var new_array = new char[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }

    /**
     * Joins two short arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first short array to join
     * @param second the second short array to join
     * @return a new short array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static short[] join(short[] first, short[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new short[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single short and a short array into a new array containing all elements.
     *
     * @param first  the short array to which to append the second short
     * @param second the short to append to the first array
     * @return a new short array with every element of the input array and the appended short,
     * or a new array containing only the second short if the input array is null
     * @since 1.0
     */
    public static short[] join(short[] first, short second) {
        if (null == first) {
            return new short[]{second};
        }

        var new_array = new short[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }

    /**
     * Joins two int arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first int array to join
     * @param second the second int array to join
     * @return a new int array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static int[] join(int[] first, int[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new int[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single int and an int array into a new array containing all elements.
     *
     * @param first  the int array to which to append the second int
     * @param second the int to append to the first array
     * @return a new int array with every element of the input array and the appended int,
     * or a new array containing only the second int if the input array is null
     * @since 1.0
     */
    public static int[] join(int[] first, int second) {
        if (null == first) {
            return new int[]{second};
        }

        var new_array = new int[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }

    /**
     * Joins two long arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first long array to join
     * @param second the second long array to join
     * @return a new long array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static long[] join(long[] first, long[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new long[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single long and a long array into a new array containing all elements.
     *
     * @param first  the long array to which to append the second long
     * @param second the long to append to the first array
     * @return a new long array with every element of the input array and the appended long,
     * or a new array containing only the second long if the input array is null
     * @since 1.0
     */
    public static long[] join(long[] first, long second) {
        if (null == first) {
            return new long[]{second};
        }

        var new_array = new long[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }

    /**
     * Joins two float arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first float array to join
     * @param second the second float array to join
     * @return a new float array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static float[] join(float[] first, float[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new float[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single float and a float array into a new array containing all elements.
     *
     * @param first  the float array to which to append the second float
     * @param second the float to append to the first array
     * @return a new float array with every element of the input array and the appended float,
     * or a new array containing only the second float if the input array is null
     * @since 1.0
     */
    public static float[] join(float[] first, float second) {
        if (null == first) {
            return new float[]{second};
        }

        var new_array = new float[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }

    /**
     * Joins two double arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first double array to join
     * @param second the second double array to join
     * @return a new double array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static double[] join(double[] first, double[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new double[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single double and a double array into a new array containing all elements.
     *
     * @param first  the double array to which to append the second double
     * @param second the double to append to the first array
     * @return a new double array with every element of the input array and the appended double,
     * or a new array containing only the second double if the input array is null
     * @since 1.0
     */
    public static double[] join(double[] first, double second) {
        if (null == first) {
            return new double[]{second};
        }

        var new_array = new double[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }

    /**
     * Joins two boolean arrays into a new array containing all elements from both arrays.
     *
     * @param first  the first boolean array to join
     * @param second the second boolean array to join
     * @return a new boolean array with every element of both input arrays, or null if both inputs are null
     * @since 1.0
     */
    public static boolean[] join(boolean[] first, boolean[] second) {
        if (null == first &&
            null == second) {
            return null;
        }
        if (null == first) {
            return second;
        }
        if (null == second) {
            return first;
        }

        var new_array = new boolean[first.length + second.length];

        System.arraycopy(first, 0, new_array, 0, first.length);
        System.arraycopy(second, 0, new_array, first.length, second.length);

        return new_array;
    }

    /**
     * Joins a single boolean and a boolean array into a new array containing all elements.
     *
     * @param first  the boolean array to which to append the second boolean
     * @param second the boolean to append to the first array
     * @return a new boolean array with every element of the input array and the appended boolean,
     * or a new array containing only the second boolean if the input array is null
     * @since 1.0
     */
    public static boolean[] join(boolean[] first, boolean second) {
        if (null == first) {
            return new boolean[]{second};
        }

        var new_array = new boolean[first.length + 1];

        System.arraycopy(first, 0, new_array, 0, first.length);
        new_array[first.length] = second;

        return new_array;
    }
}

