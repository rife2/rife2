/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.*;

// a maximum inactive interval of zero or less means that the session only
// ends when it's invalidated
class SiteSession implements HttpSession {
    private final SiteRunner runner_;
    private final String id_;
    private final Map<String, Object> attributes_ = new HashMap<>();
    private final long creationTime_;
    private volatile long lastAccessTime_;
    private volatile int maxInactiveInterval_;
    private volatile boolean isNew_ = true;
    private volatile boolean invalid_ = false;

    SiteSession(SiteRunner runner, String id, int maxInactiveInterval) {
        runner_ = runner;
        id_ = id;
        creationTime_ = runner.now();
        lastAccessTime_ = creationTime_;
        maxInactiveInterval_ = maxInactiveInterval;
    }

    private void ensureValid() {
        if (invalid_) throw new IllegalStateException("the session has been invalidated");
    }

    public long getCreationTime() {
        ensureValid();
        return creationTime_;
    }

    public String getId() {
        return id_;
    }

    public long getLastAccessedTime() {
        ensureValid();
        return lastAccessTime_;
    }

    public ServletContext getServletContext() {
        return null;
    }

    public void setMaxInactiveInterval(int interval) {
        ensureValid();
        maxInactiveInterval_ = interval;
    }

    public int getMaxInactiveInterval() {
        ensureValid();
        return maxInactiveInterval_;
    }

    public Object getAttribute(String name) {
        ensureValid();
        synchronized (attributes_) {
            return attributes_.get(name);
        }
    }

    public Enumeration<String> getAttributeNames() {
        ensureValid();
        synchronized (attributes_) {
            return Collections.enumeration(new ArrayList<>(attributes_.keySet()));
        }
    }

    public void setAttribute(String name, Object value) {
        ensureValid();
        if (null == value) {
            removeAttribute(name);
            return;
        }
        synchronized (attributes_) {
            attributes_.put(name, value);
        }
    }

    public void removeAttribute(String name) {
        ensureValid();
        synchronized (attributes_) {
            attributes_.remove(name);
        }
    }

    public void invalidate() {
        if (invalid_) {
            return;
        }
        runner_.removeSession(id_);
        expire();
    }

    public boolean isNew() {
        ensureValid();
        return isNew_;
    }

    boolean isValid() {
        return !invalid_ && !isExpired(runner_.now());
    }

    boolean isExpired(long now) {
        var interval = maxInactiveInterval_;
        return interval > 0 && now - lastAccessTime_ > interval * 1000L;
    }

    // the session isn't new anymore once the client sent its id back
    void access() {
        isNew_ = false;
        lastAccessTime_ = runner_.now();
    }

    void expire() {
        invalid_ = true;
        synchronized (attributes_) {
            attributes_.clear();
        }
    }
}
