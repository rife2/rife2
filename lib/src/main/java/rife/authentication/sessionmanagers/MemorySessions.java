/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers;

import rife.authentication.ListSessions;
import rife.authentication.SessionManager;
import rife.authentication.exceptions.SessionManagerException;
import rife.authentication.sessionmanagers.exceptions.StartSessionErrorException;
import rife.config.RifeConfig;
import rife.tools.UniqueIDGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MemorySessions implements SessionManager {
    private long sessionDuration_ = RifeConfig.authentication().getSessionDuration();
    private boolean restrictAuthData_ = RifeConfig.authentication().getSessionRestrictAuthData();
    private int sessionPurgeFrequency_ = RifeConfig.authentication().getSessionPurgeFrequency();
    private int sessionPurgeScale_ = RifeConfig.authentication().getSessionPurgeScale();

    private final Map<String, MemorySession> sessions_ = new HashMap<>();

    public MemorySessions() {
    }

    public long getSessionDuration() {
        return sessionDuration_;
    }

    public void setSessionDuration(long milliseconds) {
        sessionDuration_ = milliseconds;
    }

    public boolean getRestrictAuthData() {
        return restrictAuthData_;
    }

    public void setRestrictAuthData(boolean flags) {
        restrictAuthData_ = flags;
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

    public void purgeSessions() {
        new PurgeSessions().start();
    }

    private class PurgeSessions extends Thread {
        public void run() {
            synchronized (sessions_) {
                var stale_sessions = new ArrayList<String>();
                var expiration = System.currentTimeMillis() - getSessionDuration();
                for (var session : sessions_.values()) {
                    if (session.getStart() <= expiration) {
                        stale_sessions.add(session.getAuthId());
                    }
                }

                if (stale_sessions != null) {
                    for (var authid : stale_sessions) {
                        sessions_.remove(authid);
                    }
                }
            }
        }
    }

    public String startSession(long userId, String authData, boolean remembered)
    throws SessionManagerException {
        if (userId < 0 ||
            null == authData ||
            0 == authData.length()) {
            throw new StartSessionErrorException(userId, authData);
        }

        int purge_decision = ThreadLocalRandom.current().nextInt(getSessionPurgeScale());
        if (purge_decision <= getSessionPurgeFrequency()) {
            purgeSessions();
        }

        var auth_id_string = UniqueIDGenerator.generate().toString();

        var session = new MemorySession(auth_id_string, userId, authData, remembered);

        synchronized (sessions_) {
            sessions_.put(auth_id_string, session);
        }

        return auth_id_string;
    }

    public boolean isSessionValid(String authId, String authData)
    throws SessionManagerException {
        if (null == authId ||
            0 == authId.length() ||
            null == authData ||
            0 == authData.length()) {
            return false;
        }

        var session = getSession(authId);

        if (session != null) {
            return session.getStart() > System.currentTimeMillis() - getSessionDuration() &&
                   (!restrictAuthData_ || session.getAuthData().equals(authData));
        }

        return false;
    }

    public long getSessionUserId(String authId)
    throws SessionManagerException {
        var session = sessions_.get(authId);

        if (null == session) {
            return -1;
        }

        return session.getUserId();
    }

    public boolean continueSession(String authId)
    throws SessionManagerException {
        if (null == authId ||
            0 == authId.length()) {
            return false;
        }

        synchronized (sessions_) {
            if (sessions_.containsKey(authId)) {
                var session = sessions_.get(authId);
                session.setStart(System.currentTimeMillis());
                sessions_.put(authId, session);

                return true;
            }
        }

        return false;
    }

    public boolean eraseSession(String authId)
    throws SessionManagerException {
        if (null == authId ||
            0 == authId.length()) {
            return false;
        }

        synchronized (sessions_) {
            if (sessions_.containsKey(authId)) {
                sessions_.remove(authId);

                return true;
            }
        }

        return false;
    }

    public boolean wasRemembered(String authId)
    throws SessionManagerException {
        if (null == authId ||
            0 == authId.length()) {
            return false;
        }

        synchronized (sessions_) {
            var session = sessions_.get(authId);
            if (null == session) {
                return false;
            }

            return session.getRemembered();
        }
    }

    public boolean eraseUserSessions(long userId)
    throws SessionManagerException {
        if (userId < 0) {
            return false;
        }

        var result = false;

        synchronized (sessions_) {
            var sessions_to_erase = new ArrayList<String>();

            // collect the sessions that have to be erased
            for (var sessions_entry : sessions_.entrySet()) {
                if (userId == sessions_entry.getValue().getUserId()) {
                    sessions_to_erase.add(sessions_entry.getKey());
                }
            }

            // erased the collected sessions
            for (var authid : sessions_to_erase) {
                sessions_.remove(authid);
            }

            if (sessions_to_erase.size() > 0) {
                result = true;
            }
        }

        return result;
    }

    public void eraseAllSessions()
    throws SessionManagerException {
        sessions_.clear();
    }

    public MemorySession getSession(String authId) {
        return sessions_.get(authId);
    }

    public long countSessions() {
        long valid_session_count = 0;
        synchronized (sessions_) {
            var expiration = System.currentTimeMillis() - getSessionDuration();
            for (var session : sessions_.values()) {
                if (session.getStart() > expiration) {
                    valid_session_count++;
                }
            }
        }
        return valid_session_count;
    }

    public boolean listSessions(ListSessions processor) {
        if (null == processor) throw new IllegalArgumentException("processor can't be null");

        var result = false;

        synchronized (sessions_) {
            var expiration = System.currentTimeMillis() - getSessionDuration();
            for (var session : sessions_.values()) {
                if (session.getStart() > expiration) {
                    result = true;
                    if (!processor.foundSession(session.getUserId(), session.getAuthData(), session.getAuthId())) {
                        break;
                    }
                }
            }
        }

        return result;
    }
}

