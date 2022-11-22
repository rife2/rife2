/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers;

import rife.authentication.sessionmanagers.exceptions.*;
import rife.database.queries.*;

import rife.authentication.ListSessions;
import rife.authentication.SessionManager;
import rife.authentication.exceptions.SessionManagerException;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.DbQueryManager;
import rife.database.DbRowProcessor;
import rife.database.exceptions.DatabaseException;
import rife.tools.UniqueIDGenerator;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DatabaseSessions extends DbQueryManager implements SessionManager {
    private long sessionDuration_ = RifeConfig.authentication().getSessionDuration();
    private boolean restrictHostIp_ = RifeConfig.authentication().getSessionRestrictHostIp();

    protected DatabaseSessions(Datasource datasource) {
        super(datasource);
    }

    public long getSessionDuration() {
        return sessionDuration_;
    }

    public void setSessionDuration(long milliseconds) {
        sessionDuration_ = milliseconds;
    }

    public boolean getRestrictHostIp() {
        return restrictHostIp_;
    }

    public void setRestrictHostIp(boolean flag) {
        restrictHostIp_ = flag;
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

        executeUpdate(createAuthentication);
        executeUpdate(createAuthenticationSessStartIndex);

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
            executeUpdate(purgeSession, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement.setLong(1, System.currentTimeMillis() - getSessionDuration());
                }
            });
        } catch (DatabaseException e) {
            throw new PurgeSessionsErrorException(e);
        }
    }

    protected String _startSession(Insert startSession, final long userId, final String hostIp, final boolean remembered)
    throws SessionManagerException {
        assert startSession != null;

        if (userId < 0 ||
            null == hostIp ||
            0 == hostIp.length()) {
            throw new StartSessionErrorException(userId, hostIp);
        }

        final String auth_id_string = UniqueIDGenerator.generate().toString();

        try {
            if (0 == executeUpdate(startSession, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("authId", auth_id_string)
                        .setLong("userId", userId)
                        .setString("hostIp", hostIp)
                        .setLong("sessStart", System.currentTimeMillis())
                        .setBoolean("remembered", remembered);
                }
            })) {
                throw new StartSessionErrorException(userId, hostIp);
            }
        } catch (DatabaseException e) {
            throw new StartSessionErrorException(userId, hostIp, e);
        }

        return auth_id_string;
    }

    protected boolean _isSessionValid(Select sessionValidity, Select sessionValidityRestrictHostIp, final String authId, final String hostIp)
    throws SessionManagerException {
        assert sessionValidity != null;
        assert sessionValidityRestrictHostIp != null;

        if (null == authId ||
            0 == authId.length() ||
            null == hostIp ||
            0 == hostIp.length()) {
            return false;
        }

        boolean result = false;

        try {
            Select query;
            if (restrictHostIp_) {
                query = sessionValidityRestrictHostIp;
            } else {
                query = sessionValidity;
            }
            result = executeHasResultRows(query, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("authId", authId)
                        .setLong("sessStart", System.currentTimeMillis() - getSessionDuration());
                    if (restrictHostIp_) {
                        statement
                            .setString("hostIp", hostIp);
                    }
                }
            });
        } catch (DatabaseException e) {
            throw new IsSessionValidErrorException(authId, hostIp, e);
        }

        return result;
    }

    public boolean _continueSession(Update continueSession, final String authId)
    throws SessionManagerException {
        assert continueSession != null;

        if (null == authId ||
            0 == authId.length()) {
            return false;
        }

        boolean result = false;
        try {
            if (0 != executeUpdate(continueSession, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("sessStart", System.currentTimeMillis())
                        .setString("authId", authId);
                }
            })) {
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
            0 == authId.length()) {
            return false;
        }

        boolean result = false;
        try {
            if (0 != executeUpdate(eraseSession, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("authId", authId);
                }
            })) {
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
            0 == authId.length()) {
            return false;
        }

        boolean result = false;

        try {
            result = executeGetFirstBoolean(wasRemembered, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("authId", authId);
                }
            });
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
            if (0 != executeUpdate(eraseUserSessions, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("userId", userId);
                }
            })) {
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
            result = executeGetFirstLong(countSessions, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("sessStart", System.currentTimeMillis() - getSessionDuration());
                }
            });

        } catch (DatabaseException e) {
            throw new CountSessionsErrorException(e);
        }

        return result;
    }

    protected long _getSessionUserId(Select getSessionUserId, final String authId)
    throws SessionManagerException {
        assert getSessionUserId != null;

        if (null == authId ||
            0 == authId.length()) {
            return -1;
        }

        long result = -1;

        try {
            result = executeGetFirstLong(getSessionUserId, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("authId", authId);
                }
            });
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
            result = executeFetchAll(listSessions, new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    return processor.foundSession(
                        resultSet.getInt("userId"),
                        resultSet.getString("hostIp"),
                        resultSet.getString("authId"));
                }
            }, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("sessStart", System.currentTimeMillis() - getSessionDuration());
                }
            });

        } catch (DatabaseException e) {
            throw new CountSessionsErrorException(e);
        }

        return result;
    }
}

