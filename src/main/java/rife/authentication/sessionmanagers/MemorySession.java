/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers;

public class MemorySession {
    private String authId_ = null;
    private long userId_ = -1;
    private String authData_ = null;
    private long start_ = -1;
    private boolean remembered_ = false;

    public MemorySession(String authId, long userId, String authData, boolean remembered) {
        setAuthId(authId);
        setUserId(userId);
        setAuthData(authData);
        setRemembered(remembered);
        start_ = System.currentTimeMillis();
    }

    public void setAuthId(String authId) {
        assert authId != null;
        assert !authId.isEmpty();

        authId_ = authId;
    }

    public String getAuthId() {
        return authId_;
    }

    public void setUserId(long userId) {
        assert userId >= 0;

        userId_ = userId;
    }

    public long getUserId() {
        return userId_;
    }

    public void setAuthData(String authData) {
        assert authData != null;
        assert !authData.isEmpty();

        authData_ = authData;
    }

    public String getAuthData() {
        return authData_;
    }

    public void setStart(long start) {
        start_ = start;
    }

    public long getStart() {
        return start_;
    }

    public void setRemembered(boolean remembered) {
        remembered_ = remembered;
    }

    public boolean getRemembered() {
        return remembered_;
    }

    public int hashCode() {
        return authId_.hashCode();
    }

    public boolean equals(Object object) {
        if (object instanceof MemorySession other_session) {
            return null != other_session &&
                other_session.getAuthId().equals(getAuthId());
        }

        return false;
    }
}

