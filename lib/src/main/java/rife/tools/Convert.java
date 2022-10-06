/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.config.RifeConfig;
import rife.tools.exceptions.ConversionException;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class Convert {
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
        if (target == Date.class) return toDate(value);
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

        if (value instanceof Character) {
            return (Character) value;
        }

        if (value instanceof Number) {
            int int_value = ((Number) value).intValue();
            return (char) int_value;
        }

        if (value instanceof String &&
            1 == ((String) value).length()) {
            return ((String) value).charAt(0);
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

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            byte byte_value = ((Number) value).byteValue();
            if (0 == byte_value) {
                return false;
            }

            if (1 == byte_value) {
                return true;
            }

            throw new ConversionException(value, boolean.class, null);
        }

        if (value instanceof String) {
            return StringUtils.convertToBoolean((String) value);
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

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            try {
                return Byte.parseByte((String) value);
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

        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        if (value instanceof String) {
            try {
                return Short.parseShort((String) value);
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

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
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

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
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

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
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

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new ConversionException(value, double.class, e);
            }
        }

        throw new ConversionException(value, double.class, null);
    }

    public static Date toDate(Object value)
    throws ConversionException {
        if (null == value) {
            return null;
        }

        if (value instanceof Date) {
            return (Date) value;
        }

        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }

        if (value instanceof String) {
            try {
                return new Date(Long.parseLong((String) value));
            } catch (NumberFormatException e) {
                try {
                    return BeanUtils.getConcisePreciseDateFormat().parse((String) value);
                } catch (ParseException e2) {
                    try {
                        return RifeConfig.tools().getDefaultInputDateFormat().parse((String) value);
                    } catch (ParseException e3) {
                        throw new ConversionException(value, Date.class, e2);
                    }
                }
            }
        }

        if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        }

        throw new ConversionException(value, Date.class, null);
    }

    public static <T> T getDefaultValue(Class<T> clazz) {
        return (T) Array.get(Array.newInstance(clazz, 1), 0);
    }

}

