/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.HashMap;
import java.util.Map;

public final class HttpUtils {
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_USER_AGENT = "User-Agent";

    public static final String CHARSET = "charset=";

    public static final Map<Integer, String> HTTP_STATUS_REASONS = new HashMap<>();

    static {
        HTTP_STATUS_REASONS.put(100, "Continue");
        HTTP_STATUS_REASONS.put(101, "Switching Protocols");
        HTTP_STATUS_REASONS.put(102, "Processing");
        HTTP_STATUS_REASONS.put(103, "Early Hints");
        HTTP_STATUS_REASONS.put(200, "OK");
        HTTP_STATUS_REASONS.put(201, "Created");
        HTTP_STATUS_REASONS.put(202, "Accepted");
        HTTP_STATUS_REASONS.put(203, "Non-Authoritative Information");
        HTTP_STATUS_REASONS.put(204, "No Content");
        HTTP_STATUS_REASONS.put(205, "Reset Content");
        HTTP_STATUS_REASONS.put(206, "Partial Content");
        HTTP_STATUS_REASONS.put(207, "Multi-Status");
        HTTP_STATUS_REASONS.put(208, "Already Reported");
        HTTP_STATUS_REASONS.put(218, "This is fine");
        HTTP_STATUS_REASONS.put(226, "IM Used");
        HTTP_STATUS_REASONS.put(300, "Multiple Choices");
        HTTP_STATUS_REASONS.put(301, "Moved Permanently");
        HTTP_STATUS_REASONS.put(302, "Found/Moved Temporarily");
        HTTP_STATUS_REASONS.put(303, "See Other");
        HTTP_STATUS_REASONS.put(304, "Not Modified");
        HTTP_STATUS_REASONS.put(305, "Use Proxy");
        HTTP_STATUS_REASONS.put(306, "Switch Proxy");
        HTTP_STATUS_REASONS.put(307, "Temporary Redirect");
        HTTP_STATUS_REASONS.put(308, "Permanent Redirect");
        HTTP_STATUS_REASONS.put(400, "Bad Request");
        HTTP_STATUS_REASONS.put(401, "Unauthorized");
        HTTP_STATUS_REASONS.put(402, "Payment Required");
        HTTP_STATUS_REASONS.put(403, "Forbidden");
        HTTP_STATUS_REASONS.put(404, "Not Found");
        HTTP_STATUS_REASONS.put(405, "Method Not Allowed");
        HTTP_STATUS_REASONS.put(406, "Not Acceptable");
        HTTP_STATUS_REASONS.put(407, "Proxy Authentication Required");
        HTTP_STATUS_REASONS.put(408, "Request Timeout");
        HTTP_STATUS_REASONS.put(409, "Conflict");
        HTTP_STATUS_REASONS.put(410, "Gone");
        HTTP_STATUS_REASONS.put(411, "Length Required");
        HTTP_STATUS_REASONS.put(412, "Precondition Failed");
        HTTP_STATUS_REASONS.put(413, "Request Entity/Payload Too Large");
        HTTP_STATUS_REASONS.put(414, "Request-URI Too Long");
        HTTP_STATUS_REASONS.put(415, "Unsupported Media Type");
        HTTP_STATUS_REASONS.put(416, "Requested Range Not Satisfiable");
        HTTP_STATUS_REASONS.put(417, "Expectation Failed");
        HTTP_STATUS_REASONS.put(418, "I'm A Teapot");
        HTTP_STATUS_REASONS.put(419, "Insufficient Space on Resource");
        HTTP_STATUS_REASONS.put(420, "Method Failure");
        HTTP_STATUS_REASONS.put(421, "Misdirected Request");
        HTTP_STATUS_REASONS.put(422, "Unprocessable Entity");
        HTTP_STATUS_REASONS.put(423, "Locked");
        HTTP_STATUS_REASONS.put(424, "Failed Dependency");
        HTTP_STATUS_REASONS.put(426, "Upgrade Required");
        HTTP_STATUS_REASONS.put(428, "Precondition Required");
        HTTP_STATUS_REASONS.put(429, "Too Many Requests");
        HTTP_STATUS_REASONS.put(430, "Request Header Fields Too Large");
        HTTP_STATUS_REASONS.put(431, "Request Header Fields Too Large");
        HTTP_STATUS_REASONS.put(440, "Login Timeout");
        HTTP_STATUS_REASONS.put(444, "No Response");
        HTTP_STATUS_REASONS.put(449, "Retry With");
        HTTP_STATUS_REASONS.put(450, "Blocked by Windows Parental Controls");
        HTTP_STATUS_REASONS.put(451, "Unavailable For Legal Reasons");
        HTTP_STATUS_REASONS.put(460, "Client Closed Connection Before Load Balancer Idle Timeout");
        HTTP_STATUS_REASONS.put(463, "X-Forwarded-For Header with More than 30 IP Addresses");
        HTTP_STATUS_REASONS.put(494, "Request Header Too Large");
        HTTP_STATUS_REASONS.put(495, "SSL Certificate Error");
        HTTP_STATUS_REASONS.put(496, "No SSL Certificate");
        HTTP_STATUS_REASONS.put(497, "HTTP Request Sent to HTTPS Port");
        HTTP_STATUS_REASONS.put(498, "Token Expired/Invalid");
        HTTP_STATUS_REASONS.put(499, "Client Closed Request");
        HTTP_STATUS_REASONS.put(500, "Internal Server Error");
        HTTP_STATUS_REASONS.put(501, "Not Implemented");
        HTTP_STATUS_REASONS.put(502, "Bad Gateway");
        HTTP_STATUS_REASONS.put(503, "Service Unavailable");
        HTTP_STATUS_REASONS.put(504, "Gateway Timeout");
        HTTP_STATUS_REASONS.put(505, "HTTP Version Not Supported");
        HTTP_STATUS_REASONS.put(506, "Variant Also Negotiates");
        HTTP_STATUS_REASONS.put(507, "Insufficient Storage");
        HTTP_STATUS_REASONS.put(508, "Loop Detected");
        HTTP_STATUS_REASONS.put(509, "Bandwidth Limit Exceeded");
        HTTP_STATUS_REASONS.put(510, "Not Extended");
        HTTP_STATUS_REASONS.put(511, "Network Authentication Required");
        HTTP_STATUS_REASONS.put(520, "Unknown Error");
        HTTP_STATUS_REASONS.put(521, "Web Server Is Down");
        HTTP_STATUS_REASONS.put(522, "Origin Connection Time-out");
        HTTP_STATUS_REASONS.put(523, "Origin Is Unreachable");
        HTTP_STATUS_REASONS.put(524, "A Timeout Occurred");
        HTTP_STATUS_REASONS.put(525, "SSL Handshake Failed");
        HTTP_STATUS_REASONS.put(526, "Invalid SSL Certificate");
        HTTP_STATUS_REASONS.put(527, "Railgun Error");
        HTTP_STATUS_REASONS.put(529, "Site is overloaded");
        HTTP_STATUS_REASONS.put(530, "Site is frozen");
        HTTP_STATUS_REASONS.put(598, "Network Read Timeout Error");
        HTTP_STATUS_REASONS.put(599, "Network Connect Timeout Error");
    }

    private HttpUtils() {
        // no-op
    }

    /**
     * Returns a textual reason for an HTTP status code.
     *
     * @param statusCode the status code to get the reason for
     * @return the textual reason for the provided HTTP status code
     * @since 1.5.7
     */
    public static String statusReason(int statusCode) {
        return HTTP_STATUS_REASONS.getOrDefault(statusCode, "Unknown reason");
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

