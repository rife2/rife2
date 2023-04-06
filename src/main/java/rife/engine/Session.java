/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.http.HttpSession;

import java.util.*;

/**
 * Convenience class that wraps around an {@code HttpSession}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Session {
    private final HttpSession session_;

    Session(HttpSession session) {
        this.session_ = session;
    }

    /**
     * Returns the object bound with the specified name in this session.
     *
     * @param name the name of the object
     * @return the object with the specified name; or
     * <p>when no object can be found for that name
     * @since 1.0
     */
    public Object attribute(String name) {
        return session_.getAttribute(name);
    }

    /**
     * Binds an object to this session, using the name specified.
     * <p>If an object of the same name is already bound to the session,
     * the object is replaced.
     * <p>If the value passed in is {@code null}, this has the same effect as
     * calling {@link #removeAttribute}.
     *
     * @param name the name to which the object is bound; cannot be null
     * @param value the object to be bound to that name
     * @since 1.0
     */
    public void setAttribute(String name, Object value) {
        session_.setAttribute(name, value);
    }

    /**
     * Returns a set with the names of all the objects bound to this session.
     *
     * @return the set of attribute names
     * @since 1.0
     */
    public Set<String> attributeNames() {
        var attributes = new TreeSet<String>();
        var enumeration = session_.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            attributes.add(enumeration.nextElement());
        }
        return attributes;
    }

    /**
     * Removes the object bound with the specified name from this session.
     * If the session does not have an object bound with the specified name,
     * this method does nothing.
     *
     * @param name the name of the object to remove from this session
     * @since 1.0
     */
    public void removeAttribute(String name) {
        session_.removeAttribute(name);
    }

    /**
     * Returns the time when this session was created, measured in milliseconds
     * since midnight January 1, 1970 GMT.
     *
     * @return the session creation time
     * @since 1.0
     */
    public long creationTime() {
        return session_.getCreationTime();
    }

    /**
     * Returns a string containing the unique identifier assigned to this session.
     * The identifier is assigned by the servlet container and is implementation
     * dependent.
     *
     * @return this session's ID
     * @since 1.0
     */
    public String id() {
        return session_.getId();
    }

    /**
     * Returns the last time the client sent a request associated with this session,
     * as the number of milliseconds since midnight January 1, 1970 GMT, and marked by
     * the time the container received the request
     *
     * @return the last time this session was accessed
     * @since 1.0
     */
    public long lastAccessedTime() {
        return session_.getLastAccessedTime();
    }

    /**
     * Returns the maximum time interval, in seconds, that the servlet container will
     * keep this session open between client accesses. After this interval, the servlet
     * container will invalidate the session.
     *
     * @return an integer specifying the number of seconds this session remains open
     * between client requests
     * @since 1.0
     */
    public int maxInactiveInterval() {
        return session_.getMaxInactiveInterval();
    }

    /**
     * Specifies the time, in seconds, between client requests before the servlet
     * container will invalidate this session. An interval value of zero or less
     * indicates that the session should never time out.
     *
     * @param interval the number seconds before the session expires
     * @since 1.0
     */
    public void maxInactiveInterval(int interval) {
        session_.setMaxInactiveInterval(interval);
    }

    /**
     * Invalidates this session then unbinds any objects bound to it.
     *
     * @since 1.0
     */
    public void invalidate() {
        session_.invalidate();
    }

    /** Returns {@code true} if the client does not yet know about the session
     *
     * @return {@code true} if the server has created a session, but the client
     * has not yet joined
     * @since 1.0
     */
    public boolean isNew() {
        return session_.isNew();
    }
}
