/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.config.RifeConfig;
import rife.tools.exceptions.ConversionException;

import java.lang.reflect.Array;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;

public final class Convert {
    private Convert() {
        // no-op
    }

    public static Object toType(Object value, Class target)
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
        if (target == Time.class) return toTime(value);
        if (target == Date.class) return toDate(value);
        if (target == Instant.class) return toInstant(value);
        if (target == LocalDateTime.class) return toLocalDateTime(value);
        if (target == LocalDate.class) return toLocalDate(value);
        if (target == LocalTime.class) return toLocalTime(value);
        if (String.class.isAssignableFrom(target)) return toString(value);
        if (target.isAssignableFrom(value.getClass())) return value;

        throw new ConversionException(value, target, null);
    }

    public static String toString(Object value) {
        if (null == value) {
            return null;
        }

        return value.toString();
    }

    public static char toChar(Object value, char defaultValue) {
        try {
            return toChar(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

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

    public static boolean toBoolean(Object value, boolean defaultValue) {
        try {
            return toBoolean(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

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

    public static byte toByte(Object value, byte defaultValue) {
        try {
            return toByte(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

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

    public static short toShort(Object value, short defaultValue) {
        try {
            return toShort(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

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

    public static int toInt(Object value, int defaultValue) {
        try {
            return toInt(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

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

    public static long toLong(Object value, long defaultValue) {
        try {
            return toLong(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

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

    public static float toFloat(Object value, float defaultValue) {
        try {
            return toFloat(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

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

    public static double toDouble(Object value, double defaultValue) {
        try {
            return toDouble(value);
        } catch (ConversionException e) {
            return defaultValue;
        }
    }

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

    public static Date toDate(Calendar cal) {
        if (null == cal) {
            return null;
        }
        return cal.getTime();
    }

    public static Date toDate(Instant instant) {
        if (null == instant) {
            return null;
        }
        return Date.from(instant);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return new Date(Timestamp.valueOf(localDateTime).getTime());
    }

    public static Date toDate(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return new Date(Timestamp.valueOf(localDate.atStartOfDay()).getTime());
    }

    @SuppressWarnings("deprecated")
    public static Date toDate(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }
        return new Date(new Time(localTime.getHour(), localTime.getMinute(), localTime.getSecond()).getTime());
    }

    public static Date toDate(Number number) {
        if (null == number) {
            return null;
        }
        return new Date(number.longValue());
    }

    public static Date toDate(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }
        try {
            return new Date(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                return BeanUtils.getConcisePreciseDateFormat().parse(string);
            } catch (ParseException e2) {
                try {
                    return RifeConfig.tools().getDefaultInputDateFormat().parse(string);
                } catch (ParseException e3) {
                    throw new ConversionException(string, Date.class, e2);
                }
            }
        }
    }

    public static Date toDate(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
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

    public static java.sql.Date toSqlDate(Date date) {
        if (null == date) {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }

    public static java.sql.Date toSqlDate(Calendar cal) {
        if (null == cal) {
            return null;
        }
        return new java.sql.Date(cal.getTime().getTime());
    }

    public static java.sql.Date toSqlDate(Instant instant) {
        if (null == instant) {
            return null;
        }
        return new java.sql.Date(instant.toEpochMilli());
    }

    public static java.sql.Date toSqlDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return new java.sql.Date(Timestamp.valueOf(localDateTime).getTime());
    }

    public static java.sql.Date toSqlDate(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return new java.sql.Date(Timestamp.valueOf(localDate.atStartOfDay()).getTime());
    }

    @SuppressWarnings("deprecated")
    public static java.sql.Date toSqlDate(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }
        return new java.sql.Date(new Time(localTime.getHour(), localTime.getMinute(), localTime.getSecond()).getTime());
    }

    public static java.sql.Date toSqlDate(Number number) {
        if (null == number) {
            return null;
        }
        return new java.sql.Date(number.longValue());
    }

    public static java.sql.Date toSqlDate(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }
        try {
            return new java.sql.Date(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                return new java.sql.Date(BeanUtils.getConcisePreciseDateFormat().parse(string).getTime());
            } catch (ParseException e2) {
                try {
                    return new java.sql.Date(RifeConfig.tools().getDefaultInputDateFormat().parse(string).getTime());
                } catch (ParseException e3) {
                    throw new ConversionException(string, java.sql.Date.class, e2);
                }
            }
        }
    }

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

    public static Timestamp toTimestamp(Date date) {
        if (null == date) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

    public static Timestamp toTimestamp(Calendar cal) {
        if (null == cal) {
            return null;
        }
        return new Timestamp(cal.getTime().getTime());
    }

    public static Timestamp toTimestamp(Instant instant) {
        if (null == instant) {
            return null;
        }
        return Timestamp.from(instant);
    }

    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

    public static Timestamp toTimestamp(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return Timestamp.valueOf(localDate.atStartOfDay());
    }

    @SuppressWarnings("deprecated")
    public static Timestamp toTimestamp(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }
        return new Timestamp(new Time(localTime.getHour(), localTime.getMinute(), localTime.getSecond()).getTime());
    }

    public static Timestamp toTimestamp(Number number) {
        if (null == number) {
            return null;
        }
        return new Timestamp(number.longValue());
    }

    public static Timestamp toTimestamp(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }
        try {
            return new Timestamp(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                return new Timestamp(BeanUtils.getConcisePreciseDateFormat().parse(string).getTime());
            } catch (ParseException e2) {
                try {
                    return new Timestamp(RifeConfig.tools().getDefaultInputDateFormat().parse(string).getTime());
                } catch (ParseException e3) {
                    throw new ConversionException(string, Timestamp.class, e2);
                }
            }
        }
    }

    public static Timestamp toTimestamp(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof Timestamp ts) {
            return ts;
        }
        if (value instanceof Date date) {
            return toTimestamp(date);
        }
        if (value instanceof Calendar cal) {
            return toTimestamp(cal);
        }
        if (value instanceof Instant instant) {
            return toTimestamp(instant);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toTimestamp(localDateTime);
        }
        if (value instanceof LocalDate localDate) {
            return toTimestamp(localDate);
        }
        if (value instanceof LocalTime localTime) {
            return toTimestamp(localTime);
        }
        if (value instanceof Number number) {
            return toTimestamp(number);
        }
        if (value instanceof String string) {
            return toTimestamp(string);
        }

        throw new ConversionException(value, Timestamp.class, null);
    }

    @SuppressWarnings("deprecated")
    public static Time toTime(Date date) {
        if (null == date) {
            return null;
        }
        return new Time(date.getHours(), date.getMinutes(), date.getSeconds());
    }

    @SuppressWarnings("deprecated")
    public static Time toTime(Calendar cal) {
        if (null == cal) {
            return null;
        }
        return new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    }

    @SuppressWarnings("deprecated")
    public static Time toTime(Instant instant) {
        if (null == instant) {
            return null;
        }
        var zoned = instant.atZone(RifeConfig.tools().getDefaultZoneId());
        return new Time(zoned.getHour(), zoned.getMinute(), zoned.getSecond());
    }

    public static Time toTime(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return Time.valueOf(localDateTime.toLocalTime());
    }

    @SuppressWarnings("deprecated")
    public static Time toTime(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return new Time(0, 0, 0);
    }

    public static Time toTime(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }
        return Time.valueOf(localTime);
    }

    public static Time toTime(Number number) {
        if (null == number) {
            return null;
        }
        return new Time(number.longValue());
    }

    @SuppressWarnings("deprecated")
    public static Time toTime(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return new Time(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                var date = BeanUtils.getConcisePreciseTimeFormat().parse(string);
                return new Time(date.getHours(), date.getMinutes(), date.getSeconds());
            } catch (ParseException e2) {
                try {
                    var date = RifeConfig.tools().getDefaultInputTimeFormat().parse(string);
                    return new Time(date.getHours(), date.getMinutes(), date.getSeconds());
                } catch (ParseException e3) {
                    throw new ConversionException(string, Time.class, e2);
                }
            }
        }
    }

    @SuppressWarnings("deprecated")
    public static Time toTime(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof Time time) {
            return time;
        }
        if (value instanceof Date date) {
            return toTime(date);
        }
        if (value instanceof Calendar cal) {
            return toTime(cal);
        }
        if (value instanceof Instant instant) {
            return toTime(instant);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return toTime(localDateTime);
        }
        if (value instanceof LocalDate localDate) {
            return toTime(localDate);
        }
        if (value instanceof LocalTime localTime) {
            return toTime(localTime);
        }
        if (value instanceof Number number) {
            return toTime(number);
        }
        if (value instanceof String string) {
            return toTime(string);
        }

        throw new ConversionException(value, Time.class, null);
    }

    public static Instant toInstant(Date date) {
        if (null == date) {
            return null;
        }

        return date.toInstant();
    }

    public static Instant toInstant(Calendar cal) {
        if (null == cal) {
            return null;
        }

        return cal.toInstant();
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }

        return localDateTime.atZone(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    public static Instant toInstant(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }

        return localDate.atStartOfDay(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    public static Instant toInstant(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }

        return localTime.atDate(LocalDate.EPOCH).atZone(RifeConfig.tools().getDefaultZoneId()).toInstant();
    }

    public static Instant toInstant(Number number) {
        if (null == number) {
            return null;
        }

        return Instant.ofEpochMilli(number.longValue());
    }

    public static Instant toInstant(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return Instant.ofEpochMilli(Long.parseLong(string));
        } catch (NumberFormatException e) {
            try {
                return BeanUtils.getConcisePreciseDateTimeFormatter().parse(string, Instant::from);
            } catch (DateTimeParseException e2) {
                try {
                    return RifeConfig.tools().getDefaultInputDateFormat().parse(string).toInstant();
                } catch (ParseException e3) {
                    throw new ConversionException(string, Instant.class, e2);
                }
            }
        }
    }

    public static Instant toInstant(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof Instant instant) {
            return instant;
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

    public static LocalDateTime toLocalDateTime(Date date) {
        if (null == date) {
            return null;
        }

        return LocalDateTime.ofInstant(date.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalDateTime toLocalDateTime(Calendar cal) {
        if (null == cal) {
            return null;
        }

        return LocalDateTime.ofInstant(cal.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (null == instant) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalDateTime toLocalDateTime(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }

        return localDate.atStartOfDay();
    }

    public static LocalDateTime toLocalDateTime(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }

        return localTime.atDate(LocalDate.EPOCH);
    }

    public static LocalDateTime toLocalDateTime(Number number) {
        if (null == number) {
            return null;
        }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalDateTime toLocalDateTime(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(string)), RifeConfig.tools().getDefaultZoneId());
        } catch (NumberFormatException e) {
            try {
                return LocalDateTime.from(BeanUtils.getConcisePreciseDateTimeFormatter().parse(string));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDateTime.from(RifeConfig.tools().getDefaultInputDateTimeFormatter().parse(string));
                } catch (DateTimeParseException e3) {
                    throw new ConversionException(string, LocalDateTime.class, e2);
                }
            }
        }
    }

    public static LocalDateTime toLocalDateTime(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
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

    public static LocalDate toLocalDate(Date date) {
        if (null == date) {
            return null;
        }

        return LocalDate.ofInstant(date.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalDate toLocalDate(Calendar cal) {
        if (null == cal) {
            return null;
        }

        return LocalDate.ofInstant(cal.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalDate toLocalDate(Instant instant) {
        if (null == instant) {
            return null;
        }

        return LocalDate.ofInstant(instant, RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalDate toLocalDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }

        return localDateTime.toLocalDate();
    }

    public static LocalDate toLocalDate(LocalTime localTime) {
        if (null == localTime) {
            return null;
        }

        return LocalDate.EPOCH;
    }

    public static LocalDate toLocalDate(Number number) {
        if (null == number) {
            return null;
        }

        return LocalDate.ofInstant(Instant.ofEpochMilli(number.longValue()), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalDate toLocalDate(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return LocalDate.ofInstant(Instant.ofEpochMilli(Long.parseLong(string)), RifeConfig.tools().getDefaultZoneId());
        } catch (NumberFormatException e) {
            try {
                return LocalDate.from(BeanUtils.getConcisePreciseDateTimeFormatter().parse(string));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.from(RifeConfig.tools().getDefaultInputDateTimeFormatter().parse(string));
                } catch (DateTimeParseException e3) {
                    throw new ConversionException(string, LocalDate.class, e2);
                }
            }
        }
    }

    public static LocalDate toLocalDate(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
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

    public static LocalTime toLocalTime(Date date) {
        if (null == date) {
            return null;
        }

        return LocalTime.ofInstant(date.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalTime toLocalTime(Calendar cal) {
        if (null == cal) {
            return null;
        }

        return LocalTime.ofInstant(cal.toInstant(), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalTime toLocalTime(Instant instant) {
        if (null == instant) {
            return null;
        }

        return LocalTime.ofInstant(instant, RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalTime toLocalTime(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }

        return localDateTime.toLocalTime();
    }

    public static LocalTime toLocalTime(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }

        return LocalTime.MIDNIGHT;
    }

    public static LocalTime toLocalTime(Number number) {
        if (null == number) {
            return null;
        }

        return LocalTime.ofInstant(Instant.ofEpochMilli(number.longValue()), RifeConfig.tools().getDefaultZoneId());
    }

    public static LocalTime toLocalTime(String string)
    throws ConversionException {
        if (null == string) {
            return null;
        }

        try {
            return LocalTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(string)), RifeConfig.tools().getDefaultZoneId());
        } catch (NumberFormatException e) {
            try {
                return LocalTime.from(BeanUtils.getConcisePreciseTimeFormatter().parse(string));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalTime.from(RifeConfig.tools().getDefaultInputTimeFormatter().parse(string));
                } catch (DateTimeParseException e3) {
                    throw new ConversionException(string, LocalTime.class, e2);
                }
            }
        }
    }

    public static LocalTime toLocalTime(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof LocalTime localTime) {
            return localTime;
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

    public static <T> T getDefaultValue(Class<T> clazz) {
        return (T) Array.get(Array.newInstance(clazz, 1), 0);
    }
}