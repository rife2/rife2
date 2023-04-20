/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.config.RifeConfig;
import rife.tools.exceptions.ConversionException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * General purpose class providing methods to convert between data types.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public final class Convert {
    private Convert() {
        // no-op
    }

    /**
     * Converts the given value into the specified target class.
     *
     * @param value  the value to be converted.
     * @param target the class representing the target type of the conversion.
     * @return the value converted to the target type
     * @throws ConversionException if the value can't be converted to the target type.
     * @since 1.0
     */
    public static Object toType(Object value, Class<?> target)
    throws ConversionException {
        if (null == target) return null;

        if (target.isPrimitive()) {
            if (target == boolean.class) return toBoolean(value);
            if (target == char.class) return toChar(value);
            if (target == byte.class) return toByte(value);
            if (target == short.class) return toShort(value);
            if (target == int.class) return toInt(value);
            if (target == long.class) return toLong(value);
            if (target == float.class) return toFloat(value);
            if (target == double.class) return toDouble(value);
        }

        if (null == value) return null;

        if (target == Boolean.class) return toBoolean(value);
        if (target == Character.class) return toChar(value);
        if (target == Byte.class) return toByte(value);
        if (target == Short.class) return toShort(value);
        if (target == Integer.class) return toInt(value);
        if (target == Long.class) return toLong(value);
        if (target == Float.class) return toFloat(value);
        if (target == Double.class) return toDouble(value);
        if (target == Time.class) return toSqlTime(value);
        if (target == Date.class) return toDate(value);
        if (target == Instant.class) return toInstant(value);
        if (target == LocalDateTime.class) return toLocalDateTime(value);
        if (target == LocalDate.class) return toLocalDate(value);
        if (target == LocalTime.class) return toLocalTime(value);
        if (String.class.isAssignableFrom(target)) return toString(value);
        if (target.isAssignableFrom(value.getClass())) return value;
        if (value instanceof String str) return fromString(str, target);

        throw new ConversionException(value, target, null);
    }

    /**
     * Converts the given string into an object of specified type.
     *
     * @param string the string to be converted.
     * @param type   the class representing the target type of the conversion.
     * @return the string converted to the target type.
     * @throws ConversionException if the string can't be converted to the target type.
     * @since 1.5.20
     */
    public static Object fromString(String string, Class<?> type)
    throws ConversionException {
        if (string == null || type == null) {
            return null;
        }
        if (CharSequence.class.isAssignableFrom(type)) {
            return string;
        }

        Method method = null;
        try {
            method = type.getDeclaredMethod("valueOf", String.class);
            if (!Modifier.isStatic(method.getModifiers()) || !method.getReturnType().isAssignableFrom(type)) {
                method = null;
            }
        } catch (NoSuchMethodException ignored) {
            // try other method
        }
        try {
            method = type.getMethod("valueOf", CharSequence.class);
            if (!Modifier.isStatic(method.getModifiers()) || !method.getReturnType().isAssignableFrom(type)) {
                method = null;
            }
        } catch (NoSuchMethodException ignored) {
            // try other method
        }
        if (method == null) {
            try {
                method = type.getMethod("parse", String.class);
                if (!Modifier.isStatic(method.getModifiers()) || !method.getReturnType().isAssignableFrom(type)) {
                    method = null;
                }
            } catch (NoSuchMethodException ignored) {
                // try other method
            }
        }
        if (method == null) {
            try {
                method = type.getMethod("parse", CharSequence.class);
                if (!Modifier.isStatic(method.getModifiers()) || !method.getReturnType().isAssignableFrom(type)) {
                    method = null;
                }
            } catch (NoSuchMethodException ignored) {
                // try other method
            }
        }
        if (method == null) {
            try {
                method = type.getMethod("fromString", String.class);
                if (!Modifier.isStatic(method.getModifiers()) || !method.getReturnType().isAssignableFrom(type)) {
                    method = null;
                }
            } catch (NoSuchMethodException ignored) {
                // try other method
            }
        }
        if (method == null) {
            try {
                method = type.getMethod("fromString", CharSequence.class);
                if (!Modifier.isStatic(method.getModifiers()) || !method.getReturnType().isAssignableFrom(type)) {
                    method = null;
                }
            } catch (NoSuchMethodException ignored) {
                // try other method
            }
        }
        if (method == null) {
            try {
                method = type.getMethod("of", String.class);
                if (!Modifier.isStatic(method.getModifiers()) || !method.getReturnType().isAssignableFrom(type)) {
                    method = null;
                }
            } catch (NoSuchMethodException ignored) {
                // try other method
            }
        }
        if (method == null) {
            try {
                method = type.getMethod("of", CharSequence.class);
                if (!Modifier.isStatic(method.getModifiers()) || !method.getReturnType().isAssignableFrom(type)) {
                    method = null;
                }
            } catch (NoSuchMethodException ignored) {
                // try other method
            }
        }

        if (method != null) {
            try {
                return method.invoke(null, string);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ConversionException(string, type, e);
            }
        }

        throw new ConversionException(string, type, null);
    }

    /**
     * Returns the string representation of the specified object.
     * <p>
     * If {@code null} is provided as value, the result will also be {@code null} and
     * not the string literal {@code "null"}.
     *
     * @param value the object to convert to string.
     * @return the string representation of the specified object.
     * @since 1.0
     */
    public static String toString(Object value) {
        if (null == value) {
            return null;
        }

        return value.toString();
    }

    /**
     * Converts the specified object to a char value; returns the default value if the conversion fails.
     *
     * @param value        the object to convert to char.
     * @param defaultValue the default value.
     * @return the char value represented by the specified object or the default value if the conversion fails.
     * @since 1.0
     */
    public static char toChar(Object value, char defaultValue) {
        try {
            return toChar(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

    /**
     * Converts the specified object to a char value.
     *
     * @param value the object to convert to char.
     * @return the char value represented by the specified object.
     * @throws ConversionException if the specified object cannot be converted to a char value.
     * @since 1.0
     */
    public static char toChar(Object value)
    throws ConversionException {
        if (null == value) {
            throw new ConversionException(value, char.class, null);
        }

        if (value instanceof Character character) {
            return character;
        }

        if (value instanceof Number number) {
            int int_value = number.intValue();
            return (char) int_value;
        }

        if (value instanceof String string &&
            1 == string.length()) {
            return string.charAt(0);
        }

        throw new ConversionException(value, char.class, null);
    }

    /**
     * Converts the specified object to a boolean value; returns the default value if the conversion fails.
     *
     * @param value        the object to convert to boolean.
     * @param defaultValue the default value.
     * @return the boolean value represented by the specified object or the default value if the conversion fails.
     * @since 1.0
     */
    public static boolean toBoolean(Object value, boolean defaultValue) {
        try {
            return toBoolean(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

    /**
     * Converts the specified object to a boolean value.
     *
     * @param value the object to convert to boolean.
     * @return the boolean value represented by the specified object.
     * @throws ConversionException if the specified object cannot be converted to a boolean value.
     * @since 1.0
     */
    public static boolean toBoolean(Object value)
    throws ConversionException {
        if (null == value) {
            throw new ConversionException(value, boolean.class, null);
        }

        if (value instanceof Boolean bool) {
            return bool;
        }

        if (value instanceof Number number) {
            byte byte_value = number.byteValue();
            if (0 == byte_value) {
                return false;
            }

            if (1 == byte_value) {
                return true;
            }

            throw new ConversionException(value, boolean.class, null);
        }

        if (value instanceof String string) {
            return StringUtils.convertToBoolean(string);
        }

        throw new ConversionException(value, boolean.class, null);
    }

    /**
     * Converts the specified object to a byte value; returns the default value if the conversion fails.
     *
     * @param value        the object to convert to byte.
     * @param defaultValue the default value.
     * @return the byte value represented by the specified object or the default value if the conversion fails.
     * @since 1.0
     */
    public static byte toByte(Object value, byte defaultValue) {
        try {
            return toByte(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

    /**
     * Converts the specified object to a byte value.
     *
     * @param value the object to convert to byte.
     * @return the byte value represented by the specified object.
     * @throws ConversionException if the specified object cannot be converted to a byte value.
     * @since 1.0
     */
    public static byte toByte(Object value)
    throws ConversionException {
        if (null == value) {
            throw new ConversionException(value, byte.class, null);
        }

        if (value instanceof Number number) {
            return number.byteValue();
        }

        if (value instanceof String string) {
            try {
                return Byte.parseByte(string);
            } catch (NumberFormatException e) {
                throw new ConversionException(value, byte.class, e);
            }
        }

        throw new ConversionException(value, byte.class, null);
    }

    /**
     * Converts the specified object to a short value; returns the default value if the conversion fails.
     *
     * @param value        the object to convert to short.
     * @param defaultValue the default value.
     * @return the short value represented by the specified object or the default value if the conversion fails.
     * @since 1.0
     */
    public static short toShort(Object value, short defaultValue) {
        try {
            return toShort(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

    /**
     * Converts the specified object to a short value.
     *
     * @param value the object to convert to short.
     * @return the short value represented by the specified object.
     * @throws ConversionException if the specified object cannot be converted to a short value.
     * @since 1.0
     */
    public static short toShort(Object value)
    throws ConversionException {
        if (null == value) {
            throw new ConversionException(value, short.class, null);
        }

        if (value instanceof Number number) {
            return number.shortValue();
        }

        if (value instanceof String string) {
            try {
                return Short.parseShort(string);
            } catch (NumberFormatException e) {
                throw new ConversionException(value, short.class, e);
            }
        }

        throw new ConversionException(value, short.class, null);
    }

    /**
     * Converts the specified object to an int value; returns the default value if the conversion fails.
     *
     * @param value        the object to convert to int.
     * @param defaultValue the default value.
     * @return the int value represented by the specified object or the default value if the conversion fails.
     * @since 1.0
     */
    public static int toInt(Object value, int defaultValue) {
        try {
            return toInt(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

    /**
     * Converts the specified object to an int value.
     *
     * @param value the object to convert to int.
     * @return the int value represented by the specified object.
     * @throws ConversionException if the specified object cannot be converted to an int value.
     * @since 1.0
     */
    public static int toInt(Object value)
    throws ConversionException {
        if (null == value) {
            throw new ConversionException(value, int.class, null);
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                throw new ConversionException(value, int.class, e);
            }
        }

        throw new ConversionException(value, int.class, null);
    }

    /**
     * Converts the specified object to a long value; returns the default value if the conversion fails.
     *
     * @param value        the object to convert to long.
     * @param defaultValue the default value.
     * @return the long value represented by the specified object or the default value if the conversion fails.
     * @since 1.0
     */
    public static long toLong(Object value, long defaultValue) {
        try {
            return toLong(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

    /**
     * Converts the specified object to a long value.
     *
     * @param value the object to convert to long.
     * @return the long value represented by the specified object.
     * @throws ConversionException if the specified object cannot be converted to a long value.
     * @since 1.0
     */
    public static long toLong(Object value)
    throws ConversionException {
        if (null == value) {
            throw new ConversionException(value, long.class, null);
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String string) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
                throw new ConversionException(value, long.class, e);
            }
        }

        throw new ConversionException(value, long.class, null);
    }

    /**
     * Converts the specified object to a float value; returns the default value if the conversion fails.
     *
     * @param value        the object to convert to float.
     * @param defaultValue the default value.
     * @return the float value represented by the specified object or the default value if the conversion fails.
     * @since 1.0
     */
    public static float toFloat(Object value, float defaultValue) {
        try {
            return toFloat(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

    /**
     * Converts the specified object to a float value.
     *
     * @param value the object to convert to float.
     * @return the float value represented by the specified object.
     * @throws ConversionException if the specified object cannot be converted to a float value.
     * @since 1.0
     */
    public static float toFloat(Object value)
    throws ConversionException {
        if (null == value) {
            throw new ConversionException(value, float.class, null);
        }

        if (value instanceof Number number) {
            return number.floatValue();
        }

        if (value instanceof String string) {
            try {
                return Float.parseFloat(string);
            } catch (NumberFormatException e) {
                throw new ConversionException(value, float.class, e);
            }
        }

        throw new ConversionException(value, float.class, null);
    }

    /**
     * Converts the specified object to a double value; returns the default value if the conversion fails.
     *
     * @param value        the object to convert to double.
     * @param defaultValue the default value.
     * @return the double value represented by the specified object or the default value if the conversion fails.
     * @since 1.0
     */
    public static double toDouble(Object value, double defaultValue) {
        try {
            return toDouble(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

    /**
     * Converts the specified object to a double value.
     *
     * @param value the object to convert to double.
     * @return the double value represented by the specified object.
     * @throws ConversionException if the specified object cannot be converted to a double value.
     * @since 1.0
     */
    public static double toDouble(Object value)
    throws ConversionException {
        if (null == value) {
            throw new ConversionException(value, double.class, null);
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        if (value instanceof String string) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException e) {
                throw new ConversionException(value, double.class, e);
            }
        }

        throw new ConversionException(value, double.class, null);
    }

    /**
     * Converts a given Calendar object to a Date.
     *
     * @param cal the Calendar object to be converted to a Date
     * @return the Date representing the Calendar object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(Calendar cal) {
        if (null == cal) {
            return null;
        }
        return cal.getTime();
    }

    /**
     * Converts a given java.sql.Date object to a Date.
     *
     * @param date the java.sql.Date object to be converted to a Date
     * @return the Date representing the java.sql.Date object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(java.sql.Date date) {
        if (null == date) {
            return null;
        }
        return Date.from(toInstant(date));
    }

    /**
     * Converts a given Time object to a Date.
     *
     * @param time the Time object to be converted to a Date
     * @return the Date representing the Time object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(Time time) {
        if (null == time) {
            return null;
        }
        return Date.from(toInstant(time));
    }

    /**
     * Converts a given Timestamp object to a Date.
     *
     * @param ts the Timestamp object to be converted to a Date
     * @return the Date representing the Timestamp object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(Timestamp ts) {
        if (null == ts) {
            return null;
        }
        return Date.from(toInstant(ts));
    }

    /**
     * Converts a given Instant object to a Date.
     *
     * @param instant the Instant object to be converted to a Date
     * @return the Date representing the Instant object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(Instant instant) {
        if (null == instant) {
            return null;
        }
        return Date.from(instant);
    }

    /**
     * Converts a given LocalDateTime object to a Date.
     *
     * @param localDateTime the LocalDateTime object to be converted to a Date
     * @return the Date representing the LocalDateTime object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return Date.from(toInstant(localDateTime));
    }

    /**
     * Converts a given LocalDate object to a Date.
     *
     * @param localDate the LocalDate object to be converted to a Date
     * @return the Date representing the LocalDate object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return Date.from(toInstant(localDate));
    }

    /**
     * Converts a given LocalTime object to a Date.
     *
     * @param localTime the LocalTime object to be converted to a Date
     * @return the Date representing the LocalTime object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }
        return Date.from(toInstant(localTime));
    }

    /**
     * Converts a given Number object to a Date.
     *
     * @param number the Number object to be converted to a Date
     * @return the Date representing the Number object passed as parameter, or null if it is null
     * @since 1.0
     */
    public static Date toDate(Number number) {
        if (null == number) {
            return null;
        }
        return new Date(number.longValue());
    }

    /**
     * Converts a given String object to a Date.
     *
     * @param string the String object to be converted to a Date
     * @return the Date representing the String object passed as parameter, or null if it is null
     * @throws ConversionException if the String object passed cannot be converted to a Date
     * @since 1.0
     */
    public static Date toDate(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }
        try {
            return new Date(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                return RifeConfig.tools().getConcisePreciseDateFormat().parse(string);
            } catch (ParseException e2) {
                try {
                    return RifeConfig.tools().getDefaultInputDateFormat().parse(string);
                } catch (ParseException e3) {
                    throw new ConversionException(string, Date.class, e2);
                }
            }
        }
    }

    /**
     * Converts a given object to a Date. Depending on the type of the object passed,
     * it uses the appropriate method to convert it to a Date.
     *
     * @param value the Object to be converted to a Date
     * @return the Date representing the Object passed as parameter, or null if it is null
     * @throws ConversionException if the Object passed cannot be converted to a Date
     * @since 1.0
     */
    public static Date toDate(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof Timestamp ts) {
            return toDate(ts);
        }
        if (value instanceof java.sql.Date date) {
            return toDate(date);
        }
        if (value instanceof Time time) {
            return toDate(time);
        }
        if (value instanceof Date date) {
            return date;
        }
        if (value instanceof Calendar cal) {
            return toDate(cal);
        }
        if (value instanceof Instant instant) {
            return toDate(instant);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toDate(localDateTime);
        }
        if (value instanceof LocalDate localDate) {
            return toDate(localDate);
        }
        if (value instanceof LocalTime localTime) {
            return toDate(localTime);
        }
        if (value instanceof Number number) {
            return toDate(number);
        }
        if (value instanceof String string) {
            return toDate(string);
        }

        throw new ConversionException(value, Date.class, null);
    }

    /**
     * Converts a java.util.Date object to a java.sql.Date object.
     *
     * @param date the java.util.Date object to be converted
     * @return a java.sql.Date object representing the same date as the input
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(Date date) {
        if (null == date) {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }

    /**
     * Converts a java.util.Calendar object to a java.sql.Date object.
     *
     * @param cal the java.util.Calendar object to be converted
     * @return a java.sql.Date object representing the same date as the input
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(Calendar cal) {
        if (null == cal) {
            return null;
        }
        return new java.sql.Date(cal.getTime().getTime());
    }

    /**
     * Converts an Instant object to a java.sql.Date object.
     *
     * @param instant the Instant object to be converted
     * @return a java.sql.Date object representing the same date as the input
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(Instant instant) {
        if (null == instant) {
            return null;
        }
        return new java.sql.Date(toDate(instant).getTime());
    }

    /**
     * Converts a LocalDateTime object to a java.sql.Date object.
     *
     * @param localDateTime the LocalDateTime object to be converted
     * @return a java.sql.Date object representing the same date as the input
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return new java.sql.Date(localDateTime.atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli());
    }

    /**
     * Converts a LocalDate object to a java.sql.Date object.
     *
     * @param localDate the LocalDate object to be converted
     * @return a java.sql.Date object representing the same date as the input with time set to midnight
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }

        return new java.sql.Date(localDate.atStartOfDay(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli());
    }

    /**
     * Converts a LocalTime object to a java.sql.Date object.
     *
     * @param localTime the LocalTime object to be converted
     * @return a java.sql.Date object representing the same time as the input with date set to epoch (1970-01-01)
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }
        return new java.sql.Date(localTime.atDate(LocalDate.EPOCH).atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli());
    }

    /**
     * Converts a Number object to a java.sql.Date object.
     *
     * @param number the Number object to be converted
     * @return a java.sql.Date object representing the same date as the input
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(Number number) {
        if (null == number) {
            return null;
        }
        return new java.sql.Date(number.longValue());
    }

    /**
     * Converts a String object to a java.sql.Date object.
     *
     * @param string the String object to be converted
     * @return a java.sql.Date object representing the same date as the input, parsed using a default format or specified format (if possible)
     * @throws ConversionException if the input cannot be parsed to a java.sql.Date object using any of the available formats
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }
        try {
            return new java.sql.Date(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                return new java.sql.Date(RifeConfig.tools().getConcisePreciseDateFormat().parse(string).getTime());
            } catch (ParseException e2) {
                try {
                    return new java.sql.Date(RifeConfig.tools().getDefaultInputDateFormat().parse(string).getTime());
                } catch (ParseException e3) {
                    throw new ConversionException(string, java.sql.Date.class, e2);
                }
            }
        }
    }

    /**
     * Converts an object to a java.sql.Date object.
     *
     * @param value the object to be converted
     * @return a java.sql.Date object representing the same date as the input, if the input is one of the supported types
     * @throws ConversionException if the input is not one of the supported types
     * @since 1.0
     */
    public static java.sql.Date toSqlDate(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof java.sql.Date date) {
            return date;
        }
        if (value instanceof Date date) {
            return toSqlDate(date);
        }
        if (value instanceof Calendar cal) {
            return toSqlDate(cal);
        }
        if (value instanceof Instant instant) {
            return toSqlDate(instant);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toSqlDate(localDateTime);
        }
        if (value instanceof LocalDate localDate) {
            return toSqlDate(localDate);
        }
        if (value instanceof LocalTime localTime) {
            return toSqlDate(localTime);
        }
        if (value instanceof Number number) {
            return toSqlDate(number);
        }
        if (value instanceof String string) {
            return toSqlDate(string);
        }

        throw new ConversionException(value, java.sql.Date.class, null);
    }

    /**
     * Converts a java.sql.Date object to a Timestamp object.
     *
     * @param date the java.sql.Date object to be converted
     * @return a Timestamp object representing the same date and time as the input
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(java.sql.Date date) {
        if (null == date) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

    /**
     * Converts a java.sql.Time object to a Timestamp object.
     *
     * @param time the java.sql.Time object to be converted
     * @return a Timestamp object representing the same time as the input
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(Time time) {
        if (null == time) {
            return null;
        }
        return new Timestamp(time.getTime());
    }

    /**
     * Converts a java.util.Date object to a Timestamp object.
     *
     * @param date the java.util.Date object to be converted
     * @return a Timestamp object representing the same date and time as the input
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(Date date) {
        if (null == date) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

    /**
     * Converts a java.util.Calendar object to a Timestamp object.
     *
     * @param cal the java.util.Calendar object to be converted
     * @return a Timestamp object representing the same date and time as the input
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(Calendar cal) {
        if (null == cal) {
            return null;
        }
        return new Timestamp(cal.getTime().getTime());
    }

    public static Timestamp toSqlTimestamp(Instant instant) {
        if (null == instant) {
            return null;
        }
        return Timestamp.from(instant);
    }

    /**
     * Converts a LocalDateTime object to a Timestamp object.
     *
     * @param localDateTime the LocalDateTime object to be converted
     * @return a Timestamp object representing the same date and time as the input
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

    /**
     * Converts a LocalDate object to a Timestamp object.
     *
     * @param localDate the LocalDate object to be converted
     * @return a Timestamp object representing the same date and time as the input
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return Timestamp.valueOf(toLocalDateTime(localDate));
    }

    /**
     * Converts a LocalTime object to a Timestamp object.
     *
     * @param localTime the LocalTime object to be converted
     * @return a Timestamp object representing the same time as the input
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }
        return Timestamp.valueOf(toLocalDateTime(localTime));
    }

    /**
     * Converts a Number object to a Timestamp object.
     *
     * @param number the Number object to be converted
     * @return a Timestamp object representing the same time in milliseconds since Epoch as the input
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(Number number) {
        if (null == number) {
            return null;
        }
        return new Timestamp(number.longValue());
    }

    /**
     * Converts a String object to a Timestamp object.
     *
     * @param string the String object to be converted to a Timestamp
     * @return a Timestamp object representing the same time as the input
     * @throws ConversionException if the given string cannot be parsed as a timestamp
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }
        try {
            return new Timestamp(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                var df = RifeConfig.tools().getDefaultInputDateFormat();
                df.setTimeZone(TimeZone.getDefault());
                return new Timestamp(df.parse(string).getTime());
            } catch (ParseException e2) {
                throw new ConversionException(string, Timestamp.class, e2);
            }
        }
    }

    /**
     * Converts an object to a Timestamp object.
     *
     * @param value the object to be converted to a Timestamp
     * @return object representing the same timestamp as the input, if the input is one of the supported types
     * @throws ConversionException if the input is not one of the supported types
     * @since 1.0
     */
    public static Timestamp toSqlTimestamp(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof Timestamp ts) {
            return ts;
        }
        if (value instanceof java.sql.Date date) {
            return toSqlTimestamp(date);
        }
        if (value instanceof Time time) {
            return toSqlTimestamp(time);
        }
        if (value instanceof Date date) {
            return toSqlTimestamp(date);
        }
        if (value instanceof Calendar cal) {
            return toSqlTimestamp(cal);
        }
        if (value instanceof Instant instant) {
            return toSqlTimestamp(instant);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toSqlTimestamp(localDateTime);
        }
        if (value instanceof LocalDate localDate) {
            return toSqlTimestamp(localDate);
        }
        if (value instanceof LocalTime localTime) {
            return toSqlTimestamp(localTime);
        }
        if (value instanceof Number number) {
            return toSqlTimestamp(number);
        }
        if (value instanceof String string) {
            return toSqlTimestamp(string);
        }

        throw new ConversionException(value, Timestamp.class, null);
    }

    /**
     * Converts a Timestamp object to a Time object.
     *
     * @param ts the Timestamp object to be converted
     * @return a Time object representing the same time as the input Timestamp object
     * @since 1.0
     */
    public static Time toSqlTime(Timestamp ts) {
        if (null == ts) {
            return null;
        }
        return new Time(ts.getTime());
    }

    /**
     * Converts a java.sql.Date object to a Time object.
     *
     * @param date the java.sql.Date object to be converted
     * @return a Time object representing the same time as the input java.sql.Date object
     * @since 1.0
     */
    public static Time toSqlTime(java.sql.Date date) {
        if (null == date) {
            return null;
        }
        return new Time(date.getTime());
    }

    /**
     * Converts a Date object to a Time object.
     *
     * @param date the Date object to be converted
     * @return a Time object representing the same time as the input Date object
     * @since 1.0
     */
    @SuppressWarnings("deprecated")
    public static Time toSqlTime(Date date) {
        if (null == date) {
            return null;
        }
        var cal = RifeConfig.tools().getCalendarInstance();
        cal.setTime(date);
        return toSqlTime(cal);
    }

    /**
     * Converts a Calendar object to a Time object.
     *
     * @param cal the Calendar object to be converted
     * @return a Time object representing the same time as the input Calendar object
     * @since 1.0
     */
    @SuppressWarnings("deprecated")
    public static Time toSqlTime(Calendar cal) {
        if (null == cal) {
            return null;
        }
        return toSqlTime(cal.toInstant());
    }

    /**
     * Converts an Instant object to a Time object.
     *
     * @param instant the Instant object to be converted
     * @return a Time object representing the same time as the input Instant object
     * @since 1.0
     */
    @SuppressWarnings("deprecated")
    public static Time toSqlTime(Instant instant) {
        if (null == instant) {
            return null;
        }
        var zoned = instant.atZone(TimeZone.getDefault().toZoneId());
        return new Time(zoned.getHour(), zoned.getMinute(), zoned.getSecond());
    }

    /**
     * Converts a LocalDateTime object to a Time object.
     *
     * @param localDateTime the LocalDateTime object to be converted
     * @return a Time object representing the same time as the input LocalDateTime object
     * @since 1.0
     */
    public static Time toSqlTime(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return toSqlTime(localDateTime.atZone(TimeZone.getDefault().toZoneId()).toInstant());
    }

    /**
     * Converts a LocalDate object to a Time object.
     *
     * @param localDate the LocalDate object to be converted
     * @return a Time object representing midnight for the input LocalDate object
     * @since 1.0
     */
    @SuppressWarnings("deprecated")
    public static Time toSqlTime(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return toSqlTime(LocalTime.MIDNIGHT);
    }

    /**
     * Converts a LocalTime object to a Time object.
     *
     * @param localTime the LocalTime object to be converted
     * @return a Time object representing the same time as the input LocalTime object on January 1, 1970
     * @since 1.0
     */
    public static Time toSqlTime(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }
        return toSqlTime(localTime.atDate(LocalDate.EPOCH));
    }

    /**
     * Converts a Number object to a Time object.
     *
     * @param number the Number object to be converted
     * @return a Time object representing the same time as the input Number object, treated as a number of milliseconds since January 1, 1970
     * @since 1.0
     */
    public static Time toSqlTime(Number number) {
        if (null == number) {
            return null;
        }
        return toSqlTime(new Date(number.longValue()));
    }

    /**
     * Converts a String object to a Time object.
     *
     * @param string the String object to be converted
     * @return a Time object representing the same time as the input String object
     * @throws ConversionException if the input String cannot be parsed as a Time object
     * @since 1.0
     */
    @SuppressWarnings("deprecated")
    public static Time toSqlTime(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return toSqlTime(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                var df = RifeConfig.tools().getDefaultInputTimeFormat();
                df.setTimeZone(TimeZone.getDefault());
                var date = df.parse(string);
                return toSqlTime(date);
            } catch (ParseException e2) {
                throw new ConversionException(string, Time.class, e2);
            }
        }
    }

    /**
     * Converts an object to a TIme object.
     *
     * @param value the object to be converted to a Time
     * @return object representing the same time as the input, if the input is one of the supported types
     * @throws ConversionException if the input is not one of the supported types
     * @since 1.0
     */
    @SuppressWarnings("deprecated")
    public static Time toSqlTime(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof Time time) {
            return time;
        }
        if (value instanceof Timestamp ts) {
            return toSqlTime(ts);
        }
        if (value instanceof java.sql.Date date) {
            return toSqlTime(date);
        }
        if (value instanceof Date date) {
            return toSqlTime(date);
        }
        if (value instanceof Calendar cal) {
            return toSqlTime(cal);
        }
        if (value instanceof Instant instant) {
            return toSqlTime(instant);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toSqlTime(localDateTime);
        }
        if (value instanceof LocalDate localDate) {
            return toSqlTime(localDate);
        }
        if (value instanceof LocalTime localTime) {
            return toSqlTime(localTime);
        }
        if (value instanceof Number number) {
            return toSqlTime(number);
        }
        if (value instanceof String string) {
            return toSqlTime(string);
        }

        throw new ConversionException(value, Time.class, null);
    }

    /**
     * Converts a java.sql.Date to an Instant.
     *
     * @param date the date to be converted.
     * @return the instant representing the provided date or null if the date is null.
     * @since 1.0
     */
    public static Instant toInstant(java.sql.Date date) {
        if (null == date) {
            return null;
        }

        return date.toLocalDate().atStartOfDay().atZone(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    /**
     * Converts a Time to an Instant.
     *
     * @param time the time to be converted.
     * @return the instant representing the provided time or null if the time is null.
     * @since 1.0
     */
    public static Instant toInstant(Time time) {
        if (null == time) {
            return null;
        }

        return time.toLocalTime().atDate(LocalDate.EPOCH).atZone(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    /**
     * Converts a Timestamp to an Instant.
     *
     * @param ts the timestamp to be converted.
     * @return the instant representing the provided timestamp or null if the timestamp is null.
     * @since 1.0
     */
    public static Instant toInstant(Timestamp ts) {
        if (null == ts) {
            return null;
        }

        return ts.toLocalDateTime().atZone(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    /**
     * Converts a Date to an Instant.
     *
     * @param date the date to be converted.
     * @return the instant representing the provided date or null if the date is null.
     * @since 1.0
     */
    public static Instant toInstant(Date date) {
        if (null == date) {
            return null;
        }

        return date.toInstant();
    }

    /**
     * Converts a Calendar to an Instant.
     *
     * @param cal the calendar to be converted.
     * @return the instant representing the provided calendar or null if the calendar is null.
     * @since 1.0
     */
    public static Instant toInstant(Calendar cal) {
        if (null == cal) {
            return null;
        }

        return cal.toInstant();
    }

    /**
     * Converts a LocalDateTime to an Instant.
     *
     * @param localDateTime the LocalDateTime to be converted.
     * @return the instant representing the provided LocalDateTime or null if the LocalDateTime is null.
     * @since 1.0
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }

        return localDateTime.atZone(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    /**
     * Converts a LocalDate to an Instant.
     *
     * @param localDate the LocalDate to be converted.
     * @return the instant representing the provided LocalDate or null if the LocalDate is null.
     * @since 1.0
     */
    public static Instant toInstant(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }

        return localDate.atStartOfDay(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    /**
     * Converts a LocalTime to an Instant.
     *
     * @param localTime the LocalTime to be converted.
     * @return the instant representing the provided LocalTime or null if the LocalTime is null.
     * @since 1.0
     */
    public static Instant toInstant(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }

        return localTime.atDate(LocalDate.EPOCH).atZone(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    /**
     * Converts a Number to an Instant.
     *
     * @param number the Number to be converted.
     * @return the instant representing the provided Number or null if the Number is null.
     * @since 1.0
     */
    public static Instant toInstant(Number number) {
        if (null == number) {
            return null;
        }

        return Instant.ofEpochMilli(number.longValue());
    }

    /**
     * Converts a String to an Instant.
     *
     * @param string the String to be converted.
     * @return the instant representing the provided String or null if the String is null.
     * @throws ConversionException if the String cannot be converted to an Instant.
     * @since 1.0
     */
    public static Instant toInstant(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return Instant.ofEpochMilli(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                return RifeConfig.tools().getConcisePreciseDateTimeFormatter().parse(string, Instant::from);
            } catch (DateTimeParseException e2) {
                try {
                    return RifeConfig.tools().getDefaultInputDateFormat().parse(string).toInstant();
                } catch (ParseException e3) {
                    throw new ConversionException(string, Instant.class, e2);
                }
            }
        }
    }

    /**
     * Converts an Object to an Instant.
     *
     * @param value the Object to be converted.
     * @return the instant representing the provided Object or null if the Object is null.
     * @throws ConversionException if the Object cannot be converted to an Instant.
     * @since 1.0
     */
    public static Instant toInstant(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Timestamp ts) {
            return toInstant(ts);
        }
        if (value instanceof java.sql.Date date) {
            return toInstant(date);
        }
        if (value instanceof Time time) {
            return toInstant(time);
        }
        if (value instanceof Date date) {
            return toInstant(date);
        }
        if (value instanceof Calendar cal) {
            return toInstant(cal);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toInstant(localDateTime);
        }
        if (value instanceof LocalDate localDate) {
            return toInstant(localDate);
        }
        if (value instanceof LocalTime localTime) {
            return toInstant(localTime);
        }
        if (value instanceof Number number) {
            return toInstant(number);
        }
        if (value instanceof String string) {
            return toInstant(string);
        }

        throw new ConversionException(value, Instant.class, null);
    }

    /**
     * Converts a java.sql.Timestamp to a LocalDateTime object.
     *
     * @param ts the java.sql.Timestamp to convert
     * @return the LocalDateTime object equivalent to the given timestamp, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(Timestamp ts) {
        if (null == ts) {
            return null;
        }

        return ts.toLocalDateTime();
    }

    /**
     * Converts a java.sql.Date to a LocalDateTime object.
     *
     * @param date the java.sql.Date to convert
     * @return the LocalDateTime object equivalent to the given date, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(java.sql.Date date) {
        if (null == date) {
            return null;
        }

        return date.toLocalDate().atStartOfDay();
    }

    /**
     * Converts a java.sql.Time to a LocalDateTime object.
     *
     * @param time the java.sql.Time to convert
     * @return the LocalDateTime object equivalent to the given time, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(Time time) {
        if (null == time) {
            return null;
        }

        return time.toLocalTime().atDate(LocalDate.EPOCH);
    }

    /**
     * Converts a java.util.Date to a LocalDateTime object.
     *
     * @param date the java.util.Date to convert
     * @return the LocalDateTime object equivalent to the given date, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (null == date) {
            return null;
        }

        return LocalDateTime.ofInstant(date.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a java.util.Calendar to a LocalDateTime object.
     *
     * @param cal the java.util.Calendar to convert
     * @return the LocalDateTime object equivalent to the given calendar, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(Calendar cal) {
        if (null == cal) {
            return null;
        }

        return LocalDateTime.ofInstant(cal.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts an Instant to a LocalDateTime object.
     *
     * @param instant the Instant to convert
     * @return the LocalDateTime object equivalent to the given instant, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (null == instant) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a LocalDate to a LocalDateTime object.
     *
     * @param localDate the LocalDate to convert
     * @return the LocalDateTime object representing the start of the given date, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }

        return localDate.atStartOfDay();
    }

    /**
     * Converts a LocalTime to a LocalDateTime object.
     *
     * @param localTime the LocalTime to convert
     * @return the LocalDateTime object representing the given time on Jan 1, 1970, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }

        return localTime.atDate(LocalDate.EPOCH);
    }

    /**
     * Converts a Number to a LocalDateTime object.
     *
     * @param number the Number object to convert
     * @return the LocalDateTime object equivalent to the millisecond epoch value of the given number, or null if the input is null.
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(Number number) {
        if (null == number) {
            return null;
        }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a String to a LocalDateTime object.
     *
     * @param string the String to convert
     * @return the LocalDateTime object parsed from the given string, or null if the input is null.
     * @throws ConversionException if the string cannot be parsed as a LocalDateTime object
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(string)), RifeConfig.tools().getDefaultZoneId());
        } catch (NumberFormatException e) {
            try {
                return LocalDateTime.from(RifeConfig.tools().getConcisePreciseDateTimeFormatter().parse(string));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDateTime.from(RifeConfig.tools().getDefaultInputDateTimeFormatter().parse(string));
                } catch (DateTimeParseException e3) {
                    throw new ConversionException(string, LocalDateTime.class, e2);
                }
            }
        }
    }

    /**
     * Converts an Object to an LocalDateTime.
     *
     * @param value the Object to convert
     * @return the LocalDateTime object corresponding to the given object, or null if the input is null.
     * @throws ConversionException if the object cannot be converted to a LocalDateTime object
     * @since 1.0
     */
    public static LocalDateTime toLocalDateTime(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp ts) {
            return toLocalDateTime(ts);
        }
        if (value instanceof java.sql.Date date) {
            return toLocalDateTime(date);
        }
        if (value instanceof Time time) {
            return toLocalDateTime(time);
        }
        if (value instanceof Date date) {
            return toLocalDateTime(date);
        }
        if (value instanceof Calendar cal) {
            return toLocalDateTime(cal);
        }
        if (value instanceof Instant instant) {
            return toLocalDateTime(instant);
        }
        if (value instanceof LocalDate localDate) {
            return toLocalDateTime(localDate);
        }
        if (value instanceof LocalTime localTime) {
            return toLocalDateTime(localTime);
        }
        if (value instanceof Number number) {
            return toLocalDateTime(number);
        }
        if (value instanceof String string) {
            return toLocalDateTime(string);
        }

        throw new ConversionException(value, LocalDateTime.class, null);
    }

    /**
     * Converts a Timestamp to a LocalDate.
     *
     * @param ts The Timestamp to convert
     * @return The corresponding LocalDate, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(Timestamp ts) {
        if (null == ts) {
            return null;
        }

        return ts.toLocalDateTime().toLocalDate();
    }

    /**
     * Converts a java.sql.Date to a LocalDate.
     *
     * @param date The sql Date to convert
     * @return The corresponding LocalDate, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(java.sql.Date date) {
        if (null == date) {
            return null;
        }

        return date.toLocalDate();
    }

    /**
     * Converts a Time to a LocalDate. Always returns LocalDate.EPOCH.
     *
     * @param time The Time to convert
     * @return LocalDate.EPOCH, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(Time time) {
        if (null == time) {
            return null;
        }

        return LocalDate.EPOCH;
    }

    /**
     * Converts a Date to a LocalDate using the default zone ID defined in RifeConfig.
     *
     * @param date The Date to convert
     * @return The corresponding LocalDate, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(Date date) {
        if (null == date) {
            return null;
        }

        return LocalDate.ofInstant(date.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a Calendar to a LocalDate using the default zone ID defined in RifeConfig.
     *
     * @param cal The Calendar to convert
     * @return The corresponding LocalDate, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(Calendar cal) {
        if (null == cal) {
            return null;
        }

        return LocalDate.ofInstant(cal.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts an Instant to a LocalDate using the default zone ID defined in RifeConfig.
     *
     * @param instant The Instant to convert
     * @return The corresponding LocalDate, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(Instant instant) {
        if (null == instant) {
            return null;
        }

        return LocalDate.ofInstant(instant, RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a LocalDateTime to a LocalDate.
     *
     * @param localDateTime The LocalDateTime to convert
     * @return The corresponding LocalDate, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }

        return localDateTime.toLocalDate();
    }

    /**
     * Converts a LocalTime to a LocalDate. Always returns LocalDate.EPOCH.
     *
     * @param localTime The LocalTime to convert
     * @return LocalDate.EPOCH, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }

        return LocalDate.EPOCH;
    }

    /**
     * Converts a Number representing an epoch millisecond timestamp to a LocalDate using the default zone ID defined in RifeConfig.
     *
     * @param number The Number to convert
     * @return The corresponding LocalDate, or null if the input is null
     * @since 1.0
     */
    public static LocalDate toLocalDate(Number number) {
        if (null == number) {
            return null;
        }

        return LocalDate.ofInstant(Instant.ofEpochMilli(number.longValue()), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a String representation of a date/time to a LocalDate using various parsing strategies.
     *
     * @param string The String to convert
     * @return The corresponding LocalDate
     * @throws ConversionException If the input string cannot be parsed as a LocalDate
     * @since 1.0
     */
    public static LocalDate toLocalDate(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return LocalDate.ofInstant(Instant.ofEpochMilli(Long.parseLong(string)), RifeConfig.tools().getDefaultZoneId());
        } catch (NumberFormatException e) {
            try {
                return LocalDate.from(RifeConfig.tools().getConcisePreciseDateTimeFormatter().parse(string));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.from(RifeConfig.tools().getDefaultInputDateTimeFormatter().parse(string));
                } catch (DateTimeParseException e3) {
                    throw new ConversionException(string, LocalDate.class, e2);
                }
            }
        }
    }

    /**
     * Converts an Object to a LocalDate.
     *
     * @param value The Object to convert
     * @return The corresponding LocalDate
     * @throws ConversionException If the input Object cannot be converted to a LocalDate
     * @since 1.0
     */
    public static LocalDate toLocalDate(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Timestamp ts) {
            return toLocalDate(ts);
        }
        if (value instanceof java.sql.Date date) {
            return toLocalDate(date);
        }
        if (value instanceof Time time) {
            return toLocalDate(time);
        }
        if (value instanceof Date date) {
            return toLocalDate(date);
        }
        if (value instanceof Calendar cal) {
            return toLocalDate(cal);
        }
        if (value instanceof Instant instant) {
            return toLocalDate(instant);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toLocalDate(localDateTime);
        }
        if (value instanceof LocalTime localTime) {
            return toLocalDate(localTime);
        }
        if (value instanceof Number number) {
            return toLocalDate(number);
        }
        if (value instanceof String string) {
            return toLocalDate(string);
        }

        throw new ConversionException(value, LocalDate.class, null);
    }

    /**
     * Converts a java.sql.Date object to a LocalTime object.
     *
     * @param date the java.sql.Date object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(java.sql.Date date) {
        if (null == date) {
            return null;
        }

        return LocalTime.MIDNIGHT;
    }

    /**
     * Converts a Timestamp object to a LocalTime object.
     *
     * @param ts the Timestamp object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(Timestamp ts) {
        if (null == ts) {
            return null;
        }

        return ts.toLocalDateTime().toLocalTime();
    }

    /**
     * Converts a Time object to a LocalTime object.
     *
     * @param time the Time object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(Time time) {
        if (null == time) {
            return null;
        }

        return time.toLocalTime();
    }

    /**
     * Converts a Date object to a LocalTime object.
     *
     * @param date the Date object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(Date date) {
        if (null == date) {
            return null;
        }

        return LocalTime.ofInstant(date.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a Calendar object to a LocalTime object.
     *
     * @param cal the Calendar object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(Calendar cal) {
        if (null == cal) {
            return null;
        }

        return LocalTime.ofInstant(cal.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts an Instant object to a LocalTime object.
     *
     * @param instant the Instant object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(Instant instant) {
        if (null == instant) {
            return null;
        }

        return LocalTime.ofInstant(instant, RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a LocalDateTime object to a LocalTime object.
     *
     * @param localDateTime the LocalDateTime object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }

        return localDateTime.toLocalTime();
    }

    /**
     * Converts a LocalDate object to a LocalTime object.
     *
     * @param localDate the LocalDate object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }

        return LocalTime.MIDNIGHT;
    }

    /**
     * Converts a Number object to a LocalTime object.
     *
     * @param number the Number object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @since 1.0
     */
    public static LocalTime toLocalTime(Number number) {
        if (null == number) {
            return null;
        }

        return LocalTime.ofInstant(Instant.ofEpochMilli(number.longValue()), RifeConfig.tools().getDefaultZoneId());
    }

    /**
     * Converts a String object to a LocalTime object.
     *
     * @param string the String object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @throws ConversionException if the conversion process fails
     * @since 1.0
     */
    public static LocalTime toLocalTime(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return LocalTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(string)), RifeConfig.tools().getDefaultZoneId());
        } catch (NumberFormatException e) {
            try {
                return LocalTime.from(RifeConfig.tools().getConcisePreciseTimeFormatter().parse(string));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalTime.from(RifeConfig.tools().getDefaultInputTimeFormatter().parse(string));
                } catch (DateTimeParseException e3) {
                    throw new ConversionException(string, LocalTime.class, e2);
                }
            }
        }
    }

    /**
     * Converts an Object to a LocalTime object.
     *
     * @param value the Object to convert
     * @return the resulting LocalTime object or null if the input is null
     * @throws ConversionException if the conversion process fails
     * @since 1.0
     */
    public static LocalTime toLocalTime(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof LocalTime localTime) {
            return localTime;
        }
        if (value instanceof Timestamp ts) {
            return toLocalTime(ts);
        }
        if (value instanceof java.sql.Date date) {
            return toLocalTime(date);
        }
        if (value instanceof Time time) {
            return toLocalTime(time);
        }
        if (value instanceof Date date) {
            return toLocalTime(date);
        }
        if (value instanceof Calendar cal) {
            return toLocalTime(cal);
        }
        if (value instanceof Instant instant) {
            return toLocalTime(instant);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toLocalTime(localDateTime);
        }
        if (value instanceof LocalDate localDate) {
            return toLocalTime(localDate);
        }
        if (value instanceof Number number) {
            return toLocalTime(number);
        }
        if (value instanceof String string) {
            return toLocalTime(string);
        }

        throw new ConversionException(value, LocalTime.class, null);
    }

    /**
     * Returns the default value of a given class.
     *
     * @param clazz the class whose default value is to be returned
     * @return the default value of the given class
     * @since 1.0
     */
    public static <T> T getDefaultValue(Class<T> clazz) {
        return (T) Array.get(Array.newInstance(clazz, 1), 0);
    }
}