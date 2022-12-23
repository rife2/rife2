/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.http.HttpSession;

import java.util.*;

public class Session {
    private final HttpSession session_;

    Session(HttpSession session) {
        this.session_ = session;
    }

    public Object attribute(String name) {
        return session_.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        session_.setAttribute(name, value);
    }

    public Set<String> attributes() {
        var attributes = new TreeSet<String>();
        var enumeration = session_.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            attributes.add(enumeration.nextElement());
        }
        return attributes;
    }

    public long creationTime() {
        return session_.getCreationTime();
    }

    public String id() {
        return session_.getId();
    }

    public long lastAccessedTime() {
        return session_.getLastAccessedTime();
    }

    public int maxInactiveInterval() {
        return session_.getMaxInactiveInterval();
    }

    public void maxInactiveInterval(int interval) {
        session_.setMaxInactiveInterval(interval);
    }

    public void invalidate() {
        session_.invalidate();
    }

    public boolean isNew() {
        return session_.isNew();
    }

    public void removeAttribute(String name) {
        session_.removeAttribute(name);
    }
}
