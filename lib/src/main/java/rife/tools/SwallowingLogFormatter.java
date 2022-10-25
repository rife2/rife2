/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SwallowingLogFormatter extends Formatter {
    private final ArrayList<LogRecord> records_ = new ArrayList<LogRecord>();

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public String format(LogRecord record) {
        records_.add(record);
        return "";
    }

    public List<LogRecord> getRecords() {
        return records_;
    }
}

