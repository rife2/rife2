/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class RawFormatter extends Formatter {
    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record) {
        var sb = new StringBuilder();
        var message = formatMessage(record);
        sb.append(message);
        sb.append(System.lineSeparator());
        if (record.getThrown() != null) {
            try {
                var sw = new StringWriter();
                var pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw);
            } catch (Exception ignored) {
            }
        }
        return sb.toString();
    }
}

