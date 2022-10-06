/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

/**
 * Utility class to obtain information about the currently running Java
 * specification.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 1.6
 */
public abstract class JavaSpecificationUtils {
    /**
     * Retrieves the version of the currently running JVM.
     *
     * @return the version of the current JVM as a double
     * @since 1.6
     */
    public static double getVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }

    /**
     * Checks if the currently running JVM is at least compliant with JDK 1.5.
     *
     * @return <code>true</code> if the JVM is complient with JDK 1.5; or
     * <p><code>false</code> otherwise
     * @since 1.6
     */
    public static boolean isAtLeastJdk15() {
        return getVersion() >= 1.5;
    }

    /**
     * Checks if the currently running JVM is at least compliant with JDK 1.6.
     *
     * @return <code>true</code> if the JVM is compliant with JDK 1.6; or
     * <p><code>false</code> otherwise
     * @since 1.6
     */
    public static boolean isAtLeastJdk16() {
        return getVersion() >= 1.6;
    }
}
