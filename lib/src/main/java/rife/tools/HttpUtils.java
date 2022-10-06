/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.regex.Pattern;

public abstract class HttpUtils {
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";

    private static final String HEADER_COOKIE = "Cookie";
    private static final String HEADER_CONTENTTYPE = "Content-Type";
    private static final String HEADER_CONTENTLENGTH = "Content-Length";
    private static final String HEADER_SETCOOKIE = "Set-Cookie";

    private static final String CONTENTTYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static final String CHARSET = "charset=";
    public static final Pattern CHARSET_PATTERN = Pattern.compile(";\\s*" + CHARSET + "(.*)$");

    /**
     * Extracts only the mime-type from a Content-Type HTTP header. Thus a header
     * like this: <code>text/html;charset=UTF-8</code> will return: <code>text/html</code>
     *
     * @param contentType the Content-Type header
     * @return the content type header without the charset
     * @since 1.6
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

