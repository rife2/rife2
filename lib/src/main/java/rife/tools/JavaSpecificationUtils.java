/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

/**
 * Utility class to obtain information about the currently running Java
 * specification.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public final class JavaSpecificationUtils {
    private JavaSpecificationUtils() {
        // no-op
    }

    /**
     * Retrieves the version of the currently running JVM.
     *
     * @return the version of the current JVM as a double
     * @since 1.0
     */
    public static double getVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }
}
