/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

public final class HttpUtils {
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String CHARSET = "charset=";

    private HttpUtils() {
        // no-op
    }

    /**
     * Extracts only the mime-type from a Content-Type HTTP header. Thus a header
     * like this: {@code text/html;charset=UTF-8} will return: {@code text/html}
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

    /**
     * Generates the header value for basic HTTP authorization.
     *
     * @param username the authenticated username
     * @param password the authenticated password
     * @return the header value for basic HTTP authorization
     * @since 1.5.7
     */
    public static String basicAuthorizationHeader(String username, String password) {
        var valueToEncode = username + ":" + password;
        return "Basic " + StringUtils.encodeBase64(valueToEncode.getBytes());
    }
}

