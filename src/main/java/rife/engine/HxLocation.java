/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.json.Json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes an htmx client-side navigation with its context, for the JSON
 * form of the {@code HX-Location} response header.
 * <p>A location that only carries a path is sent as the plain path string,
 * identical to {@link Context#hxLocation(String)}. Adding any piece of
 * context switches the header to htmx's JSON object form:
 * <pre>c.hxLocation(new HxLocation(books).target("#main").swap("outerHTML"));</pre>
 * <p>sends:
 * <pre>HX-Location: {"path":"/books","target":"#main","swap":"outerHTML"}</pre>
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Context#hxLocation(HxLocation)
 * @since 1.10
 */
public class HxLocation {
    private final String path_;
    private final Route route_;

    private String source_;
    private String event_;
    private String target_;
    private String swap_;
    private String select_;
    private Object values_;
    private Map<String, String> headers_;

    /**
     * Creates a location for a URL.
     *
     * @param path the URL to navigate to
     * @since 1.10
     */
    public HxLocation(String path) {
        if (null == path) throw new IllegalArgumentException("path can't be null");

        path_ = path;
        route_ = null;
    }

    /**
     * Creates a location for a route.
     * <p>The route's URL is resolved when the header is set, so it survives
     * renaming and refactoring.
     *
     * @param route the route to navigate to
     * @since 1.10
     */
    public HxLocation(Route route) {
        if (null == route) throw new IllegalArgumentException("route can't be null");

        route_ = route;
        path_ = null;
    }

    /**
     * Sets the source element of the navigation request.
     *
     * @param cssSelector the CSS selector of the source element
     * @return this {@code HxLocation} instance, so calls can be chained
     * @since 1.10
     */
    public HxLocation source(String cssSelector) {
        source_ = cssSelector;
        return this;
    }

    /**
     * Sets the event that triggers the navigation request.
     *
     * @param event the name of the event
     * @return this {@code HxLocation} instance, so calls can be chained
     * @since 1.10
     */
    public HxLocation event(String event) {
        event_ = event;
        return this;
    }

    /**
     * Sets the element the response is swapped into.
     *
     * @param cssSelector the CSS selector of the target element
     * @return this {@code HxLocation} instance, so calls can be chained
     * @since 1.10
     */
    public HxLocation target(String cssSelector) {
        target_ = cssSelector;
        return this;
    }

    /**
     * Sets how the response is swapped in, with an {@code hx-swap} value
     * such as {@code outerHTML} or {@code beforeend}.
     *
     * @param swapStyle the swap style to use
     * @return this {@code HxLocation} instance, so calls can be chained
     * @since 1.10
     */
    public HxLocation swap(String swapStyle) {
        swap_ = swapStyle;
        return this;
    }

    /**
     * Selects only part of the response for swapping.
     *
     * @param cssSelector the CSS selector to select from the response
     * @return this {@code HxLocation} instance, so calls can be chained
     * @since 1.10
     */
    public HxLocation select(String cssSelector) {
        select_ = cssSelector;
        return this;
    }

    /**
     * Sets the values submitted with the navigation request.
     * <p>The values are serialized to JSON with RIFE2's own {@link Json}
     * support, so records, beans and maps all work.
     *
     * @param values the values to submit
     * @return this {@code HxLocation} instance, so calls can be chained
     * @since 1.10
     */
    public HxLocation values(Object values) {
        values_ = values;
        return this;
    }

    /**
     * Sets the headers submitted with the navigation request.
     *
     * @param headers the headers to submit
     * @return this {@code HxLocation} instance, so calls can be chained
     * @since 1.10
     */
    public HxLocation headers(Map<String, String> headers) {
        headers_ = headers;
        return this;
    }

    // resolves the path and renders either the plain path string or htmx's
    // JSON object form, depending on whether any context was added
    String headerValue(Context context) {
        var path = route_ != null ? context.urlFor(route_).toString() : path_;
        if (null == source_ && null == event_ && null == target_ &&
            null == swap_ && null == select_ && null == values_ && null == headers_) {
            return path;
        }

        var location = new LinkedHashMap<String, Object>();
        location.put("path", path);
        if (source_ != null) location.put("source", source_);
        if (event_ != null) location.put("event", event_);
        if (target_ != null) location.put("target", target_);
        if (swap_ != null) location.put("swap", swap_);
        if (select_ != null) location.put("select", select_);
        if (values_ != null) location.put("values", values_);
        if (headers_ != null) location.put("headers", headers_);
        return Json.toString(location);
    }
}
