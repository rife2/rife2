/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.Locale;

/**
 * A single byte range of a {@code Range} request header, resolved against
 * the length of the content it applies to.
 *
 * @param start the position of the first byte
 * @param end   the position of the last byte, inclusive
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11.0
 */
public record ByteRange(long start, long end) {
    /**
     * The result of parsing a range that the content can't satisfy, which
     * should be answered with a {@code 416} status. Compare a parsed range
     * to it with {@code ==}.
     *
     * @since 1.11.0
     */
    public static final ByteRange UNSATISFIABLE = new ByteRange(-1, -1);

    /**
     * Parses the value of a {@code Range} request header.
     * <p>Only a single range of bytes is supported, for anything else
     * {@code null} is returned so that the whole content can be served, as
     * HTTP allows.
     *
     * @param header the value of the {@code Range} header
     * @param length the length of the content in bytes
     * @return the requested range;
     * <p>{@link #UNSATISFIABLE} if the content can't satisfy it; or
     * <p>{@code null} if the whole content should be served
     * @since 1.11.0
     */
    public static ByteRange parse(String header, long length) {
        var value = header.trim();
        if (!value.toLowerCase(Locale.ENGLISH).startsWith("bytes=")) {
            return null;
        }
        value = value.substring(6).trim();
        if (value.indexOf(',') != -1) {
            return null;
        }

        var dash = value.indexOf('-');
        if (-1 == dash) {
            return null;
        }

        try {
            var first = value.substring(0, dash).trim();
            var last = value.substring(dash + 1).trim();
            // positions are plain digits, an invalid range is ignored
            if (!isDigits(first) || !isDigits(last)) {
                return null;
            }
            long start;
            long end;
            if (first.isEmpty()) {
                if (last.isEmpty()) {
                    return null;
                }
                var suffix = Long.parseLong(last);
                if (suffix <= 0) {
                    return UNSATISFIABLE;
                }
                start = Math.max(0, length - suffix);
                end = length - 1;
            } else {
                start = Long.parseLong(first);
                end = length - 1;
                if (!last.isEmpty()) {
                    var requested_end = Long.parseLong(last);
                    // a range that ends before it starts is invalid and ignored
                    if (requested_end < start) {
                        return null;
                    }
                    end = Math.min(requested_end, length - 1);
                }
            }
            if (start >= length || 0 == length) {
                return UNSATISFIABLE;
            }
            return new ByteRange(start, end);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isDigits(String value) {
        for (var i = 0; i < value.length(); i++) {
            var ch = value.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the number of bytes in this range.
     *
     * @return the length of this range
     * @since 1.11.0
     */
    public long length() {
        return end - start + 1;
    }
}
