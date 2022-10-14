/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers;

import rife.authentication.ListSessions;
import rife.authentication.SessionManager;
import rife.authentication.exceptions.SessionManagerException;
import rife.config.RifeConfig;

import java.util.Random;

public class PurgingSessionManager implements SessionManager {
    private int sessionPurgeFrequency_ = RifeConfig.authentication().getSessionPurgeFrequency();
    private int sessionPurgeScale_ = RifeConfig.authentication().getSessionPurgeScale();

    private final Random random_ = new Random();

    private SessionManager mSessionManager = null;

    public PurgingSessionManager(SessionManager sessionManager) {
        if (null == sessionManager) throw new IllegalArgumentException("sessionManager can't be null");

        mSessionManager = sessionManager;
    }

    public SessionManager getSessionManager() {
        return mSessionManager;
    }

    public int getSessionPurgeFrequency() {
        return sessionPurgeFrequency_;
    }

    public void setSessionPurgeFrequency(int frequency) {
        sessionPurgeFrequency_ = frequency;
    }

    public int getSessionPurgeScale() {
        return sessionPurgeScale_;
    }

    public void setSessionPurgeScale(int scale) {
        sessionPurgeScale_ = scale;
    }

    public String startSession(long userId, String hostIp, boolean remembered)
    throws SessionManagerException {
        int purge_decision = -1;
        synchronized (random_) {
            purge_decision = random_.nextInt(sessionPurgeScale_);
        }
        if (purge_decision <= sessionPurgeFrequency_) {
            purgeSessions();
        }

        return mSessionManager.startSession(userId, hostIp, remembered);
    }

    public void setSessionDuration(final long milliseconds) {
        mSessionManager.setSessionDuration(milliseconds);
    }

    public long getSessionDuration() {
        return mSessionManager.getSessionDuration();
    }

    public boolean getRestrictHostIp() {
        return mSessionManager.getRestrictHostIp();
    }

    public void setRestrictHostIp(boolean flag) {
        mSessionManager.setRestrictHostIp(flag);
    }

    public void eraseAllSessions()
    throws SessionManagerException {
        mSessionManager.eraseAllSessions();
    }

    public boolean isSessionValid(final String authId, final String hostIp)
    throws SessionManagerException {
        return mSessionManager.isSessionValid(authId, hostIp);
    }

    public boolean continueSession(final String authId)
    throws SessionManagerException {
        return mSessionManager.continueSession(authId);
    }

    public long getSessionUserId(final String authId)
    throws SessionManagerException {
        return mSessionManager.getSessionUserId(authId);
    }

    public void purgeSessions()
    throws SessionManagerException {
        mSessionManager.purgeSessions();
    }

    public boolean eraseSession(String authId)
    throws SessionManagerException {
        return mSessionManager.eraseSession(authId);
    }

    public boolean wasRemembered(String authId)
    throws SessionManagerException {
        return mSessionManager.wasRemembered(authId);
    }

    public boolean eraseUserSessions(long userId)
    throws SessionManagerException {
        return mSessionManager.eraseUserSessions(userId);
    }

    public long countSessions()
    throws SessionManagerException {
        return mSessionManager.countSessions();
    }

    public boolean listSessions(ListSessions processor)
    throws SessionManagerException {
        return mSessionManager.listSessions(processor);
    }
}

