/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import java.util.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

class MockHeaders {
    private static final String[] DATE_FORMAT_SYNTAXES =
        {
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy",
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "dd MMM yyyy HH:mm:ss",
            "dd-MMM-yy HH:mm:ss",
        };

    private static final SimpleDateFormat[] DATE_FORMATS;
    private static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT");
    private static final ThreadLocal<SimpleDateFormat[]> DATE_PARSED_CACHED = new ThreadLocal<>();
    private static final String SET_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    static {
        TIMEZONE_GMT.setID("GMT");
        DATE_FORMATS = new SimpleDateFormat[DATE_FORMAT_SYNTAXES.length];
        for (var i = 0; i < DATE_FORMATS.length; i++) {
            DATE_FORMATS[i] = new SimpleDateFormat(DATE_FORMAT_SYNTAXES[i], Locale.US);
            DATE_FORMATS[i].setTimeZone(TIMEZONE_GMT);
        }
    }

    private Map<String, List<String>> headers_;
    private SimpleDateFormat[] dateFormats_;

    public long getDateHeader(String name) {
        var header = getHeader(name);
        if (header == null) {
            return -1;
        }

        if (dateFormats_ == null) {
            dateFormats_ = DATE_PARSED_CACHED.get();
            if (dateFormats_ == null) {
                dateFormats_ = new SimpleDateFormat[DATE_FORMATS.length];
                DATE_PARSED_CACHED.set(dateFormats_);
            }
        }

        for (var i = 0; i < dateFormats_.length; i++) {
            // clone formatter for thread safety
            if (dateFormats_[i] == null) {
                dateFormats_[i] = (SimpleDateFormat) DATE_FORMATS[i].clone();
            }

            try {
                var date = (Date) dateFormats_[i].parseObject(header);
                return date.getTime();
            } catch (ParseException e) {
                // IllegalArgumentException will be thrown at the end of the method
            }
        }

        if (header.endsWith(" GMT")) {
            header = header.substring(0, header.length() - 4);
            for (SimpleDateFormat simpleDateFormat : dateFormats_) {
                try {
                    var date = (Date) simpleDateFormat.parseObject(header);
                    return date.getTime();
                } catch (ParseException e) {
                    // IllegalArgumentException will be thrown at the end of the method
                }
            }
        }

        throw new IllegalArgumentException(header);
    }

    public String getHeader(String name) {
        if (null == headers_) {
            return null;
        }

        var headers = headers_.get(name);
        if (null == headers ||
            0 == headers.size()) {
            return null;
        }

        return headers.get(0);
    }

    public Set<String> getHeaderNames() {
        if (null == headers_) {
            return Collections.emptySet();
        }

        return headers_.keySet();
    }

    public List<String> getHeaders(String name) {
        if (null == headers_) {
            return Collections.emptyList();
        }

        var headers = headers_.get(name);
        if (null == headers ||
            0 == headers.size()) {
            return Collections.emptyList();
        }

        return headers;
    }

    public int getIntHeader(String name) {
        var header = getHeader(name);
        if (null == header) {
            return -1;
        }

        try {
            return Integer.parseInt(header);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(header);
        }
    }

    public void addHeader(String name, String value) {
        if (null == headers_) {
            headers_ = new HashMap<String, List<String>>();
        }

        var headers = headers_.computeIfAbsent(name, k -> new ArrayList<>());
        headers.add(value);
    }

    public void addDateHeader(String name, long date) {
        addHeader(name, formatDate(date));
    }

    public void addIntHeader(String name, int integer) {
        addHeader(name, String.valueOf(integer));
    }

    public boolean containsHeader(String name) {
        if (null == headers_) {
            return false;
        }

        return headers_.containsKey(name);
    }

    public void setDateHeader(String name, long date) {
        setHeader(name, formatDate(date));
    }

    private String formatDate(long date) {
        var format = new SimpleDateFormat(SET_DATE_FORMAT);
        format.setTimeZone(TIMEZONE_GMT);
        return format.format(date);
    }

    public void setHeader(String name, final String value) {
        if (null == headers_) {
            headers_ = new HashMap<String, List<String>>();
        }

        headers_.put(name, new ArrayList<String>() {{
            add(value);
        }});
    }

    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    public void removeHeader(String name) {
        if (null == headers_) {
            return;
        }

        headers_.remove(name);
    }
}
