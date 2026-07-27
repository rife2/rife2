/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.json.Json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes an htmx client-side navigation together with its context, for
 * the JSON form of the {@code HX-Location} response header.
 * <p>When a location only carries a path, it will be sent as the plain
 * path string, exactly like {@link Context#hxLocation(String)} does. As
 * soon as you add any piece of context, the header switches over to htmx's
 * JSON object form:
 * <pre>c.hxLocation(new HxLocation(books).target("#main").swap("outerHTML"));</pre>
 * <p>which sends:
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
     * Creates a location that navigates to a URL.
     *
     * @param path the URL to navigate to
     * @see #HxLocation(Route)
     * @since 1.10
     */
    public HxLocation(String path) {
        if (null == path) throw new IllegalArgumentException("path can't be null");

        path_ = path;
        route_ = null;
    }

    /**
     * Creates a location that navigates to a route.
     * <p>The URL of the route will only be resolved when the header is set,
     * so that the navigation survives renaming and refactoring.
     *
     * @param route the route to navigate to
     * @see #HxLocation(String)
     * @since 1.10
     */
    public HxLocation(Route route) {
        if (null == route) throw new IllegalArgumentException("route can't be null");

        route_ = route;
        path_ = null;
    }

    /**
     * Sets the element that is the source of the navigation request.
     *
     * @param cssSelector the CSS selector of the source element
     * @return this {@code HxLocation} instance, so that method calls can be
     * chained
     * @since 1.10
     */
    public HxLocation source(String cssSelector) {
        source_ = cssSelector;
        return this;
    }

    /**
     * Sets the event that will trigger the navigation request.
     *
     * @param event the name of the event
     * @return this {@code HxLocation} instance, so that method calls can be
     * chained
     * @since 1.10
     */
    public HxLocation event(String event) {
        event_ = event;
        return this;
    }

    /**
     * Sets the element that the response will be swapped into.
     *
     * @param cssSelector the CSS selector of the target element
     * @return this {@code HxLocation} instance, so that method calls can be
     * chained
     * @since 1.10
     */
    public HxLocation target(String cssSelector) {
        target_ = cssSelector;
        return this;
    }

    /**
     * Sets the way in which the response will be swapped in.
     * <p>This takes an {@code hx-swap} value, for instance
     * {@code outerHTML} or {@code beforeend}.
     *
     * @param swapStyle the swap style to use
     * @return this {@code HxLocation} instance, so that method calls can be
     * chained
     * @since 1.10
     */
    public HxLocation swap(String swapStyle) {
        swap_ = swapStyle;
        return this;
    }

    /**
     * Selects only a part of the response for swapping.
     *
     * @param cssSelector the CSS selector to select from the response
     * @return this {@code HxLocation} instance, so that method calls can be
     * chained
     * @since 1.10
     */
    public HxLocation select(String cssSelector) {
        select_ = cssSelector;
        return this;
    }

    /**
     * Sets the values that will be submitted with the navigation request.
     * <p>The values will be serialized to JSON with RIFE2's own
     * {@link Json} support, so that records, beans and maps all work.
     *
     * @param values the values to submit
     * @return this {@code HxLocation} instance, so that method calls can be
     * chained
     * @since 1.10
     */
    public HxLocation values(Object values) {
        values_ = values;
        return this;
    }

    /**
     * Sets the headers that will be submitted with the navigation request.
     *
     * @param headers the headers to submit
     * @return this {@code HxLocation} instance, so that method calls can be
     * chained
     * @since 1.10
     */
    public HxLocation headers(Map<String, String> headers) {
        headers_ = headers;
        return this;
    }

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
