/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.Date;

public final class ClassUtils {
    private ClassUtils() {
        // no-op
    }

    public static boolean isNumeric(Class klass) {
        return Number.class.isAssignableFrom(klass) ||
            byte.class == klass ||
            short.class == klass ||
            int.class == klass ||
            long.class == klass ||
            float.class == klass ||
            double.class == klass;
    }

    public static boolean isText(Class klass) {
        return CharSequence.class.isAssignableFrom(klass) ||
            Character.class == klass ||
            char.class == klass;
    }

    public static boolean isBasic(Class klass) {
        if (null == klass) {
            return false;
        }

        return isNumeric(klass) ||
            boolean.class == klass ||
            Boolean.class == klass ||
            Date.class.isAssignableFrom(klass) ||
            klass.isEnum() ||
            isText(klass);
    }

    public static boolean isFromJdk(Class klass) {
        if (null == klass) {
            return false;
        }

        return isBasic(klass) || klass.getClassLoader() == Object.class.getClassLoader();
    }

    public static String simpleClassName(Class klass) {
        var class_name = klass.getName();
        if (klass.getPackage() != null) {
            class_name = class_name.substring(klass.getPackage().getName().length() + 1);
        }

        return class_name;
    }

    public static String shortenClassName(Class klass) {
        return simpleClassName(klass).replace('$', '_');
    }

    public static String[] getEnumClassValues(Class klass) {
        if (klass.isEnum()) {
            var values = klass.getEnumConstants();
            return ArrayUtils.createStringArray(values);
        }

        return null;
    }
}