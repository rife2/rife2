/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import rife.authentication.SessionAttributes;
import rife.authentication.SessionValidator;
import rife.authentication.credentialsmanagers.DatabaseUsers;
import rife.authentication.credentialsmanagers.DatabaseUsersFactory;
import rife.authentication.exceptions.SessionValidatorException;
import rife.authentication.remembermanagers.DatabaseRemember;
import rife.authentication.remembermanagers.DatabaseRememberFactory;
import rife.authentication.sessionmanagers.DatabaseSessions;
import rife.authentication.sessionmanagers.DatabaseSessionsFactory;
import rife.authentication.sessionvalidators.exceptions.SessionValidityCheckErrorException;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.Select;

public abstract class DatabaseSessionValidator extends DbQueryManager implements SessionValidator<DatabaseUsers, DatabaseSessions, DatabaseRemember> {
    public static final int SESSION_INVALID = 0;
    public static final int SESSION_VALID = 1;

    protected DatabaseUsers credentialsManager_;
    protected DatabaseSessions sessionManager_;
    protected DatabaseRemember rememberManager_;

    protected DatabaseSessionValidator(Datasource datasource) {
        super(datasource);

        credentialsManager_ = DatabaseUsersFactory.instance(datasource);
        sessionManager_ = DatabaseSessionsFactory.instance(datasource);
        rememberManager_ = DatabaseRememberFactory.instance(datasource);
    }

    @Override
    public void setCredentialsManager(DatabaseUsers credentialsManager) {
        assert credentialsManager != null;

        credentialsManager_ = credentialsManager;
    }

    @Override
    public DatabaseUsers getCredentialsManager() {
        return credentialsManager_;
    }

    @Override
    public void setSessionManager(DatabaseSessions sessionManager) {
        assert sessionManager != null;

        sessionManager_ = sessionManager;
    }

    @Override
    public DatabaseSessions getSessionManager() {
        return sessionManager_;
    }

    @Override
    public void setRememberManager(DatabaseRemember rememberManager) {
        assert rememberManager != null;

        rememberManager_ = rememberManager;
    }

    @Override
    public DatabaseRemember getRememberManager() {
        return rememberManager_;
    }

    @Override
    public boolean isAccessAuthorized(int id) {
        return SESSION_VALID == id;
    }

    protected int _validateSession(Select sessionValidityNoRole, Select sessionValidityNoRoleRestrictAuthData, Select sessionValidityRole, Select sessionValidityRoleRestrictAuthData, ProcessSessionValidity processSessionValidity, final String authId, final String authData, final SessionAttributes attributes)
    throws SessionValidatorException {
        if (null == authId ||
            authId.isEmpty() ||
            null == authData ||
            authData.isEmpty() ||
            null == attributes) {
            return SESSION_INVALID;
        }

        int result;

        Select query;

        // select which query to use according to the role attribute
        if (attributes.hasAttribute("role")) {
            if (sessionManager_.getRestrictAuthData()) {
                query = sessionValidityRoleRestrictAuthData;
            } else {
                query = sessionValidityRole;
            }
        } else {
            if (sessionManager_.getRestrictAuthData()) {
                query = sessionValidityNoRoleRestrictAuthData;
            } else {
                query = sessionValidityNoRole;
            }
        }

        // role has been specified, use optimized validity check to limit the amount of db queries
        try {
            executeFetchFirst(query, processSessionValidity, statement -> {
                statement
                    .setString("authId", authId)
                    .setLong("sessStart", System.currentTimeMillis() - sessionManager_.getSessionDuration());

                if (attributes.hasAttribute("role")) {
                    statement
                        .setString("role", attributes.getAttribute("role"));
                }
                if (sessionManager_.getRestrictAuthData()) {
                    statement
                        .setString("authData", authData);
                }
            });
            result = processSessionValidity.getValidity();
        } catch (DatabaseException e) {
            throw new SessionValidityCheckErrorException(authId, authData, e);
        }

        return result;
    }
}



