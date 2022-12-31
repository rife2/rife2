/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

public final class HttpUtils {
    public static final String CHARSET = "charset=";

    private HttpUtils() {
        // no-op
    }

    /**
     * Extracts only the mime-type from a Content-Type HTTP header. Thus a header
     * like this: <code>text/html;charset=UTF-8</code> will return: <code>text/html</code>
     *
     * @param contentType the Content-Type header
     * @return the content type header without the charset
     * @since 1.0
     */
    public static String extractMimeTypeFromContentType(String contentType) {
        var charset_index = contentType.indexOf(CHARSET);
        if (charset_index != -1) {
            char indexed_char;
            do {
                indexed_char = contentType.charAt(--charset_index);
            }
            while (' ' == indexed_char);

            if (';' == indexed_char) {
                return contentType.substring(0, charset_index);
            }
        }

        return contentType;
    }
}

