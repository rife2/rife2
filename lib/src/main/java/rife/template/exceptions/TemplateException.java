/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import rife.datastructures.DocumentPosition;
import rife.tools.StringUtils;

import java.io.Serial;

public class TemplateException extends RuntimeException {
    @Serial private static final long serialVersionUID = 8643896354837543058L;

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public static String formatError(String name, DocumentPosition errorLocation, String message) {
        String without_tabs = StringUtils.stripFromFront(errorLocation.lineContent(), "\t");
        int removed_tab_count = errorLocation.lineContent().length() - without_tabs.length();

        return "\n" + name + ":" + errorLocation.line() + ":" + errorLocation.column() + ": " + message + "\n" +
            StringUtils.repeat("    ", removed_tab_count) + without_tabs + "\n" +
            StringUtils.repeat("   ", removed_tab_count) + StringUtils.repeat(" ", errorLocation.column()) + StringUtils.repeat("^", Math.max(errorLocation.marking(), 1)) + "\n";
    }
}
