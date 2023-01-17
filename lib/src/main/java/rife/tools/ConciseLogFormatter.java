/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.config.RifeConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConciseLogFormatter extends Formatter {
    private static final DateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = RifeConfig.tools().getSimpleDateFormat("yyyyMMdd HHmm");
    }

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public String format(LogRecord record) {
        var sb = new StringBuilder();
        var message = formatMessage(record);
        sb.append(DATE_FORMAT.format(new Date()));
        sb.append(" ");
        sb.append(record.getLevel());
        sb.append(" ");
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

