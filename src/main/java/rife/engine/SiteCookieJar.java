/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.http.Cookie;
import rife.tools.ExceptionUtils;

import java.util.*;
import java.util.logging.Logger;

/**
 * Acts as the browser for the cookies of a {@link SiteRunner}. It
 * keeps the cookies that the responses set and sends them back with the
 * requests they apply to, by host, domain, path, expiry and secure flag,
 * and leaves out the {@code SameSite} ones that a request of another site
 * shouldn't get, as a browser does.
 * <p>When the web view of your application keeps the cookies of the origin
 * itself, you don't need a jar. With a {@link SiteCookieStore}, the cookies
 * that have an expiry also survive a restart of your application, which is
 * useful for a remembered login for instance.
 * <p>Cookies that are set for a domain are also sent to its subdomains.
 * Since the list of public suffixes isn't known here, a jar shouldn't be
 * shared by origins that don't trust each other.
 * <p>A jar can be used from several threads at the same time.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SiteRunner#cookieJar(SiteCookieJar)
 * @since 1.11.0
 */
public class SiteCookieJar {
    private static final String HEADER_COOKIE = "Cookie";

    private final SiteCookieStore store_;
    private final Map<String, SiteCookie> cookies_ = new LinkedHashMap<>();
    private List<SiteCookie> saved_ = List.of();

    /**
     * Creates a jar that keeps its cookies in memory only.
     *
     * @since 1.11.0
     */
    public SiteCookieJar() {
        this(null);
    }

    /**
     * Creates a jar that restores its cookies from a store and saves the
     * ones that last there each time they change.
     *
     * @param store the store of the cookies, or {@code null} to keep them
     *              in memory only
     * @since 1.11.0
     */
    public SiteCookieJar(SiteCookieStore store) {
        store_ = store;
        if (store != null) {
            var loaded = store.load();
            if (loaded != null) {
                var now = System.currentTimeMillis();
                for (var cookie : loaded) {
                    // a cookie without a name or a path can't be matched
                    if (cookie != null && cookie.name() != null && cookie.path() != null &&
                        !cookie.isExpired(now)) {
                        cookies_.put(id(cookie), cookie);
                    }
                }
                saved_ = lasting();
            }
        }
    }

    // like browsers, a cookie for the domain of its own host replaces the
    // cookie of that host
    private static String id(SiteCookie cookie) {
        var domain = null == cookie.domain() ? lower(cookie.host()) : cookie.domain();
        return domain + "\n" + cookie.path() + "\n" + cookie.name();
    }

    private static String lower(String value) {
        return null == value ? "" : value.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Retrieves all the cookies that haven't expired.
     *
     * @return the cookies of this jar
     * @since 1.11.0
     */
    public List<SiteCookie> getCookies() {
        synchronized (cookies_) {
            removeExpired();
            return new ArrayList<>(cookies_.values());
        }
    }

    /**
     * Retrieves the cookies that would be sent with a request, ordered with
     * the longest path first.
     *
     * @param host   the host of the request
     * @param path   the path of the request
     * @param secure whether the request is secure
     * @return the matching cookies
     * @since 1.11.0
     */
    public List<SiteCookie> getCookies(String host, String path, boolean secure) {
        var result = new ArrayList<SiteCookie>();
        synchronized (cookies_) {
            removeExpired();
            for (var cookie : cookies_.values()) {
                if (cookie.secure() && !secure) {
                    continue;
                }
                if (cookie.domain() != null ? !domainMatches(host, cookie.domain()) : !lower(cookie.host()).equals(lower(host))) {
                    continue;
                }
                if (!pathMatches(path, cookie.path())) {
                    continue;
                }
                result.add(cookie);
            }
        }
        result.sort(Comparator.comparingInt((SiteCookie c) -> c.path().length()).reversed());
        return result;
    }

    /**
     * Removes all the cookies.
     *
     * @since 1.11.0
     */
    public void clear() {
        synchronized (cookies_) {
            cookies_.clear();
            save();
        }
    }

    // the jar is the browser, so the cookies that came with the request
    // itself, from a script in the web view for instance, are left out
    void apply(SiteRequest request, String path) {
        request.removeHeader(HEADER_COOKIE);
        var cookies = getCookies(request.getServerName(), path, request.isSecure());
        if (cookies.isEmpty()) {
            return;
        }

        // as a browser does, a request that another site made, from a frame
        // inside a page for instance, gets no cookie that's strictly for the
        // site, and a lax one, which is also what one without SameSite is,
        // only when it shows a page without changing something
        var cross_site = isCrossSite(request);
        var safe = switch (request.getMethod()) {
            case GET, HEAD, OPTIONS, TRACE -> true;
            default -> false;
        };
        var mode = request.getHeader("Sec-Fetch-Mode");
        // without that, a page is opened without an Origin, which a script that asks sends
        var page = null == mode ? null == request.getHeader("Origin") : lower(mode).equals("navigate") && "document".equals(lower(request.getHeader("Sec-Fetch-Dest")));
        var header = new StringBuilder();
        for (var cookie : cookies) {
            if (cross_site && !"none".equals(lower(cookie.sameSite()))) {
                if ("strict".equals(lower(cookie.sameSite())) || !safe || !page) {
                    continue;
                }
            }
            if (!header.isEmpty()) {
                header.append("; ");
            }
            header.append(cookie.name()).append('=').append(null == cookie.value() ? "" : cookie.value());
        }
        if (!header.isEmpty()) {
            request.addHeader(HEADER_COOKIE, header.toString());
        }
    }

    // what the web view says of who made the request, or else where it came from
    private static boolean isCrossSite(SiteRequest request) {
        var fetch_site = request.getHeader("Sec-Fetch-Site");
        if (fetch_site != null) {
            return lower(fetch_site).equals("cross-site");
        }
        var origin = request.getHeader("Origin");
        if (null == origin) {
            return false;
        }
        // read here, since java.net.URI has no host for a name with an
        // underscore, like the one of an Android package; and an origin of
        // null, from a sandboxed frame, is another site
        var said = origin.trim();
        var scheme_end = said.indexOf("://");
        if (scheme_end < 0) {
            return true;
        }
        var rest = said.substring(scheme_end + 3);
        String host;
        if (rest.startsWith("[")) {
            var close = rest.indexOf(']');
            host = close < 0 ? rest : rest.substring(0, close + 1);
        } else {
            var end = rest.length();
            for (var separator : new char[]{':', '/'}) {
                var at = rest.indexOf(separator);
                if (at >= 0 && at < end) {
                    end = at;
                }
            }
            host = rest.substring(0, end);
        }
        return !lower(host).equals(lower(request.getServerName()));
    }

    void store(SiteRequest request, Collection<Cookie> cookies) {
        if (cookies.isEmpty()) {
            return;
        }

        var host = request.getServerName();
        var path = request.getRequestPath();
        var secure = request.isSecure();
        var now = System.currentTimeMillis();
        synchronized (cookies_) {
            for (var cookie : cookies) {
                if (cookie.getSecure() && !secure) {
                    continue;
                }
                if (!SiteResponse.isValidCookieValue(cookie.getValue())) {
                    continue;
                }

                var domain = cookie.getDomain();
                if (domain != null) {
                    domain = domain.toLowerCase(Locale.ENGLISH);
                    if (domain.startsWith(".")) {
                        domain = domain.substring(1);
                    }
                    if (domain.isEmpty() || !domainMatches(host, domain)) {
                        continue;
                    }
                }

                var cookie_path = cookie.getPath();
                if (null == cookie_path || !cookie_path.startsWith("/")) {
                    cookie_path = defaultPath(path);
                }

                long expires = -1;
                var max_age = cookie.getMaxAge();
                if (max_age >= 0) {
                    expires = max_age == 0 ? 0 : now + max_age * 1000L;
                } else {
                    var expires_attribute = cookie.getAttribute("Expires");
                    if (expires_attribute != null) {
                        try {
                            // a date before 1970 is in the past too
                            expires = Math.max(0, SiteHeaders.parseDate(expires_attribute));
                        } catch (IllegalArgumentException e) {
                            // an invalid expiry leaves it a session cookie
                        }
                    }
                }

                var kept = new SiteCookie(cookie.getName(), cookie.getValue(), host, domain, cookie_path,
                    expires, cookie.getSecure(), cookie.isHttpOnly(), cookie.getAttribute("SameSite"));
                // an insecure origin can't replace or remove a secure cookie
                var existing = cookies_.get(id(kept));
                if (existing != null && existing.secure() && !secure) {
                    continue;
                }
                if (kept.isExpired(now)) {
                    cookies_.remove(id(kept));
                } else {
                    cookies_.put(id(kept), kept);
                }
            }
            save();
        }
    }

    private void removeExpired() {
        var now = System.currentTimeMillis();
        if (cookies_.values().removeIf(cookie -> cookie.isExpired(now))) {
            save();
        }
    }

    private List<SiteCookie> lasting() {
        var lasting = new ArrayList<SiteCookie>();
        for (var cookie : cookies_.values()) {
            if (!cookie.isSessionCookie()) {
                lasting.add(cookie);
            }
        }
        return lasting;
    }

    // the store is only told when the cookies that last have changed, and a
    // failure in the application's store shouldn't fail the requests
    private void save() {
        if (null == store_) {
            return;
        }

        var lasting = lasting();
        if (!lasting.equals(saved_)) {
            try {
                store_.save(lasting);
                saved_ = lasting;
            } catch (RuntimeException e) {
                Logger.getLogger("rife.engine").warning("Error while saving cookies\n" + ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    // an IP address only matches itself, and a domain needs a dot so that it
    // can't be a top-level domain, since the public suffixes aren't known here
    static boolean domainMatches(String host, String domain) {
        if (null == host) {
            return false;
        }
        host = host.toLowerCase(Locale.ENGLISH);
        if (host.equals(domain)) {
            return true;
        }
        if (isIpAddress(host) || domain.indexOf('.') == -1) {
            return false;
        }
        return host.endsWith("." + domain);
    }

    private static boolean isIpAddress(String host) {
        return host.indexOf(':') != -1 || host.matches("[0-9.]+");
    }

    static boolean pathMatches(String requestPath, String cookiePath) {
        if (null == requestPath || requestPath.isEmpty()) {
            requestPath = "/";
        }
        if (requestPath.equals(cookiePath)) {
            return true;
        }
        if (requestPath.startsWith(cookiePath)) {
            return cookiePath.endsWith("/") || requestPath.charAt(cookiePath.length()) == '/';
        }
        return false;
    }

    static String defaultPath(String requestPath) {
        if (null == requestPath || !requestPath.startsWith("/")) {
            return "/";
        }
        var last_slash = requestPath.lastIndexOf('/');
        if (last_slash <= 0) {
            return "/";
        }
        return requestPath.substring(0, last_slash);
    }
}
