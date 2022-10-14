/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers;

import rife.authentication.ListSessions;
import rife.authentication.SessionManager;
import rife.authentication.exceptions.SessionManagerException;
import rife.authentication.sessionmanagers.exceptions.StartSessionErrorException;
import rife.config.RifeConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemorySessions implements SessionManager {
    private long mSessionDuration = RifeConfig.authentication().getSessionDuration();
    private boolean mRestrictHostIp = RifeConfig.authentication().getSessionRestrictHostIp();

    private final Map<String, MemorySession> sessions_ = new HashMap<String, MemorySession>();

    public MemorySessions() {
    }

    public long getSessionDuration() {
        return mSessionDuration;
    }

    public void setSessionDuration(long milliseconds) {
        mSessionDuration = milliseconds;
    }

    public boolean getRestrictHostIp() {
        return mRestrictHostIp;
    }

    public void setRestrictHostIp(boolean flags) {
        mRestrictHostIp = flags;
    }

    public void purgeSessions() {
        new PurgeSessions().start();
    }

    private class PurgeSessions extends Thread {
        public void run() {
            synchronized (sessions_) {
                ArrayList<String> stale_sessions = new ArrayList<String>();
                long expiration = System.currentTimeMillis() - getSessionDuration();
                for (MemorySession session : sessions_.values()) {
                    if (session.getStart() <= expiration) {
                        stale_sessions.add(session.getAuthId());
                    }
                }

                if (stale_sessions != null) {
                    for (String authid : stale_sessions) {
                        sessions_.remove(authid);
                    }
                }
            }
        }
    }

    public String startSession(long userId, String hostIp, boolean remembered)
    throws SessionManagerException {
        if (userId < 0 ||
            null == hostIp ||
            0 == hostIp.length()) {
            throw new StartSessionErrorException(userId, hostIp);
        }

        String auth_id_string = UUID.randomUUID().toString();

        MemorySession session = new MemorySession(auth_id_string, userId, hostIp, remembered);

        synchronized (sessions_) {
            sessions_.put(auth_id_string, session);
        }

        return auth_id_string;
    }

    public boolean isSessionValid(String authId, String hostIp)
    throws SessionManagerException {
        if (null == authId ||
            0 == authId.length() ||
            null == hostIp ||
            0 == hostIp.length()) {
            return false;
        }

        MemorySession session = getSession(authId);

        if (session != null) {
            if (session.getStart() > System.currentTimeMillis() - getSessionDuration() &&
                (!mRestrictHostIp || session.getHostIp().equals(hostIp))) {
                return true;
            }
        }

        return false;
    }

    public long getSessionUserId(String authId)
    throws SessionManagerException {
        MemorySession session = sessions_.get(authId);

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
                MemorySession session = sessions_.get(authId);
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
            MemorySession session = sessions_.get(authId);
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

        boolean result = false;

        synchronized (sessions_) {
            ArrayList<String> sessions_to_erase = new ArrayList<String>();

            // collect the sessions that have to be erased
            for (Map.Entry<String, MemorySession> sessions_entry : sessions_.entrySet()) {
                if (userId == sessions_entry.getValue().getUserId()) {
                    sessions_to_erase.add(sessions_entry.getKey());
                }
            }

            // erased the collected sessions
            for (String authid : sessions_to_erase) {
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
            long expiration = System.currentTimeMillis() - getSessionDuration();
            for (MemorySession session : sessions_.values()) {
                if (session.getStart() > expiration) {
                    valid_session_count++;
                }
            }
        }
        return valid_session_count;
    }

    public boolean listSessions(ListSessions processor) {
        if (null == processor) throw new IllegalArgumentException("processor can't be null");

        boolean result = false;

        synchronized (sessions_) {
            long expiration = System.currentTimeMillis() - getSessionDuration();
            for (MemorySession session : sessions_.values()) {
                if (session.getStart() > expiration) {
                    result = true;
                    if (!processor.foundSession(session.getUserId(), session.getHostIp(), session.getAuthId())) {
                        break;
                    }
                }
            }
        }

        return result;
    }
}

