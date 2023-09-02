/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers;

import rife.authentication.ListSessions;
import rife.authentication.SessionManager;
import rife.authentication.exceptions.SessionManagerException;
import rife.authentication.sessionmanagers.exceptions.*;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.*;
import rife.tools.ExceptionUtils;
import rife.tools.UniqueIDGenerator;

public abstract class DatabaseSessions extends DbQueryManager implements SessionManager {
    private long sessionDuration_ = RifeConfig.authentication().getSessionDuration();
    private boolean restrictAuthData_ = RifeConfig.authentication().getSessionRestrictAuthData();
    private int sessionPurgeFrequency_ = RifeConfig.authentication().getSessionPurgeFrequency();
    private int sessionPurgeScale_ = RifeConfig.authentication().getSessionPurgeScale();

    protected DatabaseSessions(Datasource datasource) {
        super(datasource);
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

    public void setRestrictAuthData(boolean flag) {
        restrictAuthData_ = flag;
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

    public abstract boolean install()
    throws SessionManagerException;

    public abstract boolean remove()
    throws SessionManagerException;

    public abstract long countSessions()
    throws SessionManagerException;

    protected boolean _install(CreateTable createAuthentication, String createAuthenticationSessStartIndex) {
        assert createAuthentication != null;
        assert createAuthenticationSessStartIndex != null;
        try {
            executeUpdate(createAuthentication);
            executeUpdate(createAuthenticationSessStartIndex);
        } catch (DatabaseException e) {
            final String trace = ExceptionUtils.getExceptionStackTrace(e);
            if (!trace.contains("already exists")) {
                throw new InstallSessionsErrorException(e);
            }
        }

        return true;
    }

    protected boolean _remove(DropTable removeAuthentication, String removeAuthenticationSessStartIndex) {
        assert removeAuthentication != null;
        assert removeAuthenticationSessStartIndex != null;

        executeUpdate(removeAuthenticationSessStartIndex);
        executeUpdate(removeAuthentication);

        return true;
    }

    protected void _purgeSessions(Delete purgeSession)
    throws SessionManagerException {
        try {
            executeUpdate(purgeSession, s -> s.setLong(1, System.currentTimeMillis() - getSessionDuration()));
        } catch (DatabaseException e) {
            throw new PurgeSessionsErrorException(e);
        }
    }

    protected String _startSession(Insert startSession, final long userId, final String authData, final boolean remembered)
    throws SessionManagerException {
        assert startSession != null;

        if (userId < 0 ||
            null == authData ||
            authData.isEmpty()) {
            throw new StartSessionErrorException(userId, authData);
        }

        final String auth_id_string = UniqueIDGenerator.generate().toString();

        try {
            if (0 == executeUpdate(startSession, s ->
                s.setString("authId", auth_id_string)
                    .setLong("userId", userId)
                    .setString("authData", authData)
                    .setLong("sessStart", System.currentTimeMillis())
                    .setBoolean("remembered", remembered))) {
                throw new StartSessionErrorException(userId, authData);
            }
        } catch (DatabaseException e) {
            throw new StartSessionErrorException(userId, authData, e);
        }

        return auth_id_string;
    }

    protected boolean _isSessionValid(Select sessionValidity, Select sessionValidityRestrictAuthData, final String authId, final String authData)
    throws SessionManagerException {
        assert sessionValidity != null;
        assert sessionValidityRestrictAuthData != null;

        if (null == authId ||
            authId.isEmpty() ||
            null == authData ||
            authData.isEmpty()) {
            return false;
        }

        boolean result = false;

        try {
            Select query;
            if (restrictAuthData_) {
                query = sessionValidityRestrictAuthData;
            } else {
                query = sessionValidity;
            }
            result = executeHasResultRows(query, s -> {
                    s.setString("authId", authId)
                        .setLong("sessStart", System.currentTimeMillis() - getSessionDuration());
                    if (restrictAuthData_) {
                        s.setString("authData", authData);
                    }
                }
            );
        } catch (
            DatabaseException e) {
            throw new IsSessionValidErrorException(authId, authData, e);
        }

        return result;
    }

    public boolean _continueSession(Update continueSession, final String authId)
    throws SessionManagerException {
        assert continueSession != null;

        if (null == authId ||
            authId.isEmpty()) {
            return false;
        }

        boolean result = false;
        try {
            if (0 != executeUpdate(continueSession, s ->
                s.setLong("sessStart", System.currentTimeMillis())
                    .setString("authId", authId))) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new ContinueSessionErrorException(authId, e);
        }

        return result;
    }

    protected boolean _eraseSession(Delete eraseSession, final String authId)
    throws SessionManagerException {
        assert eraseSession != null;

        if (null == authId ||
            authId.isEmpty()) {
            return false;
        }

        boolean result = false;
        try {
            if (0 != executeUpdate(eraseSession, s -> s.setString("authId", authId))) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new EraseSessionErrorException(authId, e);
        }

        return result;
    }

    protected boolean _wasRemembered(Select wasRemembered, final String authId)
    throws SessionManagerException {
        assert wasRemembered != null;

        if (null == authId ||
            authId.isEmpty()) {
            return false;
        }

        boolean result;

        try {
            result = executeGetFirstBoolean(wasRemembered, s -> s.setString("authId", authId));
        } catch (DatabaseException e) {
            throw new SessionRememberedCheckErrorException(authId, e);
        }

        return result;
    }

    protected boolean _eraseUserSessions(Delete eraseUserSessions, final long userId)
    throws SessionManagerException {
        assert eraseUserSessions != null;

        if (userId < 0) {
            return false;
        }

        boolean result = false;
        try {
            if (0 != executeUpdate(eraseUserSessions, s -> s.setLong("userId", userId))) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new EraseUserSessionsErrorException(userId, e);
        }

        return result;
    }

    protected void _eraseAllSessions(Delete eraseAllSessions)
    throws SessionManagerException {
        assert eraseAllSessions != null;

        try {
            executeUpdate(eraseAllSessions);
        } catch (DatabaseException e) {
            throw new EraseAllSessionsErrorException(e);
        }
    }

    protected long _countSessions(Select countSessions)
    throws SessionManagerException {
        assert countSessions != null;

        long result = -1;

        try {
            result = executeGetFirstLong(countSessions, s ->
                s.setLong("sessStart", System.currentTimeMillis() - getSessionDuration()));
        } catch (DatabaseException e) {
            throw new CountSessionsErrorException(e);
        }

        return result;
    }

    protected long _getSessionUserId(Select getSessionUserId, final String authId)
    throws SessionManagerException {
        assert getSessionUserId != null;

        if (null == authId ||
            authId.isEmpty()) {
            return -1;
        }

        long result = -1;

        try {
            result = executeGetFirstLong(getSessionUserId, s -> s.setString("authId", authId));
        } catch (DatabaseException e) {
            throw new GetSessionUserIdErrorException(authId, e);
        }

        return result;
    }

    protected boolean _listSessions(Select listSessions, final ListSessions processor)
    throws SessionManagerException {
        if (null == processor) throw new IllegalArgumentException("processor can't be null");

        boolean result = false;

        try {
            result = executeFetchAll(listSessions, resultSet ->
                processor.foundSession(
                    resultSet.getInt("userId"),
                    resultSet.getString("authData"),
                    resultSet.getString("authId")), s ->
                s.setLong("sessStart", System.currentTimeMillis() - getSessionDuration()));
        } catch (DatabaseException e) {
            throw new CountSessionsErrorException(e);
        }

        return result;
    }
}

