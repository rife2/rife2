/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.Date;

/**
 * The ClassUtils class provides useful utility methods for working with Java classes.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public final class ClassUtils {
    private ClassUtils() {
        // no-op
    }

    /**
     * Returns true if the specified class is numeric.
     *
     * @param klass the class to check
     * @return true if the specified class is numeric, false otherwise
     * @since 1.0
     */
    public static boolean isNumeric(Class klass) {
        return Number.class.isAssignableFrom(klass) ||
            byte.class == klass ||
            short.class == klass ||
            int.class == klass ||
            long.class == klass ||
            float.class == klass ||
            double.class == klass;
    }

    /**
     * Returns true if the specified class is text.
     *
     * @param klass the class to check
     * @return true if the specified class is text, false otherwise
     * @since 1.0
     */
    public static boolean isText(Class klass) {
        return CharSequence.class.isAssignableFrom(klass) ||
            Character.class == klass ||
            char.class == klass;
    }

    /**
     * Returns true if the specified class is a basic type.
     *
     * @param klass the class to check
     * @return true if the specified class is a basic type, false otherwise
     * @since 1.0
     */
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

    /**
     * Returns true if the specified class is from the JDK.
     *
     * @param klass the class to check
     * @return true if the specified class is from the JDK, false otherwise
     * @since 1.0
     */
    public static boolean isFromJdk(Class klass) {
        if (null == klass) {
            return false;
        }

        return isBasic(klass) || klass.getClassLoader() == Object.class.getClassLoader();
    }

    /**
     * Returns the simple name of the specified class without the package name.
     *
     * @param klass the class whose simple name to return
     * @return the simple name of the specified class
     * @since 1.0
     */
    public static String simpleClassName(Class klass) {
        var class_name = klass.getName();
        if (klass.getPackage() != null) {
            class_name = class_name.substring(klass.getPackage().getName().length() + 1);
        }

        return class_name;
    }

    /**
     * Returns a shortened version of the specified class name, with "$" characters replaced by underscores.
     *
     * @param klass the class whose name to shorten
     * @return a shortened version of the specified class name
     * @since 1.0
     */
    public static String shortenClassName(Class klass) {
        return simpleClassName(klass).replace('$', '_');
    }

    /**
     * Returns an array of the values of the enum constants of the specified class, or null if the class is not an enum.
     *
     * @param klass the class whose enum constant values to return
     * @return an array of the values of the enum constants of the specified class, or null if the class is not an enum
     * @since 1.0
     */
    public static String[] getEnumClassValues(Class klass) {
        if (klass.isEnum()) {
            var values = klass.getEnumConstants();
            return ArrayUtils.createStringArray(values);
        }

        return null;
    }
}