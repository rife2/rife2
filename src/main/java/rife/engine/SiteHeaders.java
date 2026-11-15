/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

// header names aren't case-sensitive, like in HTTP
class SiteHeaders {
    private static final String[] DATE_FORMAT_SYNTAXES =
        {
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy",
            "dd MMM yyyy HH:mm:ss",
            "dd-MMM-yy HH:mm:ss",
        };
    private static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT");
    private static final DateTimeFormatter SET_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).withZone(ZoneOffset.UTC);

    private final Map<String, List<String>> headers_ = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static String formatDate(long date) {
        return SET_DATE_FORMAT.format(Instant.ofEpochMilli(date));
    }

    // the formats aren't thread-safe, so they're created for each date
    static long parseDate(String header) {
        var candidates = header.endsWith(" GMT") ? List.of(header, header.substring(0, header.length() - 4)) : List.of(header);
        for (var candidate : candidates) {
            for (var syntax : DATE_FORMAT_SYNTAXES) {
                var format = new SimpleDateFormat(syntax, Locale.US);
                format.setTimeZone(TIMEZONE_GMT);
                try {
                    return format.parse(candidate).getTime();
                } catch (ParseException e) {
                    // try the next format
                }
            }
        }

        throw new IllegalArgumentException(header);
    }

    synchronized long getDateHeader(String name) {
        var header = getHeader(name);
        if (null == header) {
            return -1;
        }

        return parseDate(header);
    }

    synchronized String getHeader(String name) {
        var headers = headers_.get(name);
        if (null == headers ||
            headers.isEmpty()) {
            return null;
        }

        return headers.get(0);
    }

    synchronized Set<String> getHeaderNames() {
        return new LinkedHashSet<>(headers_.keySet());
    }

    synchronized List<String> getHeaders(String name) {
        var headers = headers_.get(name);
        if (null == headers ||
            headers.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(headers);
    }

    synchronized Map<String, List<String>> getAll() {
        var result = new LinkedHashMap<String, List<String>>();
        for (var entry : headers_.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    synchronized int getIntHeader(String name) {
        var header = getHeader(name);
        if (null == header) {
            return -1;
        }

        try {
            return Integer.parseInt(header);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(header, e);
        }
    }

    synchronized void addHeader(String name, String value) {
        headers_.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    void addDateHeader(String name, long date) {
        addHeader(name, formatDate(date));
    }

    void addIntHeader(String name, int integer) {
        addHeader(name, String.valueOf(integer));
    }

    synchronized boolean containsHeader(String name) {
        return headers_.containsKey(name);
    }

    void setDateHeader(String name, long date) {
        setHeader(name, formatDate(date));
    }

    synchronized void setHeader(String name, String value) {
        var values = new ArrayList<String>();
        values.add(value);
        headers_.remove(name);
        headers_.put(name, values);
    }

    void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    synchronized void removeHeader(String name) {
        headers_.remove(name);
    }
}
