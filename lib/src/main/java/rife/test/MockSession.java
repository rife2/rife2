/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
/*
 * Copyright 2001-2008 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

class MockSession implements HttpSession {
    public static final String SESSION_COOKIE_NAME = "JSESSION";

    private static int sNextId = 1;

    private final MockConversation mockConversation_;
    private String mId = Integer.toString(sNextId++);
	private final Map<String, Object> attributes_ = new HashMap<>();
	private final long creationTime_ = System.currentTimeMillis();
	private long lastAccessTime_ = System.currentTimeMillis();
	private int maxInactiveInterval_;
    private boolean isNew_ = true;
    private boolean invalid_ = false;

    MockSession(MockConversation conversation) {
        mockConversation_ = conversation;
    }

    public long getCreationTime() {
        if (invalid_) throw new IllegalStateException();

        return creationTime_;
    }

    public String getId() {
        if (invalid_) throw new IllegalStateException();

        return mId;
    }

    public long getLastAccessedTime() {
        if (invalid_) throw new IllegalStateException();

        return lastAccessTime_;
    }

    public void setMaxInactiveInterval(int interval) {
        if (invalid_) throw new IllegalStateException();

        maxInactiveInterval_ = interval;
    }

    public int getMaxInactiveInterval() {
        if (invalid_) throw new IllegalStateException();

        return maxInactiveInterval_;
    }

    public Object getAttribute(String name) {
        if (invalid_) throw new IllegalStateException();

        return attributes_.get(name);
    }

    public Enumeration getAttributeNames() {
        if (invalid_) throw new IllegalStateException();

        return Collections.enumeration(attributes_.keySet());
    }

    public void setAttribute(String name, Object value) {
        if (invalid_) throw new IllegalStateException();

        attributes_.put(name, value);
    }

    public void removeAttribute(String name) {
        if (invalid_) throw new IllegalStateException();

        attributes_.remove(name);
    }

    public void invalidate() {
        mockConversation_.removeSession(mId);
        invalid_ = true;
        attributes_.clear();
        mId = null;
    }

    public boolean isNew() {
        return isNew_;
    }

    public Object getValue(String name) {
        if (invalid_) throw new IllegalStateException();

        return getAttribute(name);
    }

    public String[] getValueNames() {
        if (invalid_) throw new IllegalStateException();

        String[] names_array = new String[attributes_.size()];
        attributes_.keySet().toArray(names_array);
        return names_array;
    }

    public void putValue(String name, Object value) {
        if (invalid_) throw new IllegalStateException();

        setAttribute(name, value);
    }

    public void removeValue(String name) {
        if (invalid_) throw new IllegalStateException();

        removeAttribute(name);
    }

    public ServletContext getServletContext() {
        return null;
    }

    public HttpSessionContext getSessionContext() {
        return null;
    }

    void access() {
        isNew_ = false;
        lastAccessTime_ = System.currentTimeMillis();
    }

    boolean isValid() {
        return !invalid_;
    }
}
