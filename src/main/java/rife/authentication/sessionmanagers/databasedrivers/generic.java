/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.databasedrivers;

import rife.database.queries.*;

import rife.authentication.ListSessions;
import rife.authentication.exceptions.SessionManagerException;
import rife.authentication.sessionmanagers.DatabaseSessions;
import rife.config.RifeConfig;
import rife.database.Datasource;

import java.util.concurrent.ThreadLocalRandom;

public class generic extends DatabaseSessions {
    protected CreateTable createAuthentication_;
    protected String createAuthenticationSessStartIndex_;
    protected Delete purgeSessions_;
    protected Insert startSession_;
    protected Select isSessionValid_;
    protected Select isSessionValidRestrictAuthData_;
    protected Update continueSession_;
    protected Delete eraseSession_;
    protected Select wasRemembered_;
    protected Delete eraseAllSessions_;
    protected Delete eraseUserSessions_;
    protected DropTable removeAuthentication_;
    protected String removeAuthenticationSessStartIndex_;
    protected Select countSessions_;
    protected Select getSessionUserId_;
    protected Select listSessions_;

    public generic(Datasource datasource) {
        super(datasource);

        var table_authentication = RifeConfig.authentication().getTableAuthentication();
        createAuthentication_ = new CreateTable(getDatasource())
            .table(table_authentication)
            .column("authId", String.class, 40, CreateTable.NOTNULL)
            .column("userId", long.class, CreateTable.NOTNULL)
            .column("authData", String.class, 40, CreateTable.NULL)
            .column("sessStart", long.class, CreateTable.NOTNULL)
            .column("remembered", boolean.class, CreateTable.NOTNULL)
            .defaultValue("remembered", false)
            .primaryKey(table_authentication.toUpperCase() + "_PK", "authId");

        createAuthenticationSessStartIndex_ = "CREATE INDEX " + table_authentication + "_IDX ON " + table_authentication + " (sessStart)";

        purgeSessions_ = new Delete(getDatasource())
            .from(createAuthentication_.getTable())
            .whereParameter("sessStart", "<=");

        startSession_ = new Insert(getDatasource())
            .into(createAuthentication_.getTable())
            .fieldParameter("authId")
            .fieldParameter("userId")
            .fieldParameter("authData")
            .fieldParameter("sessStart")
            .fieldParameter("remembered");

        isSessionValid_ = new Select(getDatasource())
            .from(createAuthentication_.getTable())
            .whereParameter("authId", "=")
            .whereParameterAnd("sessStart", ">");

        isSessionValidRestrictAuthData_ = new Select(getDatasource())
            .from(createAuthentication_.getTable())
            .whereParameter("authId", "=")
            .whereParameterAnd("authData", "=")
            .whereParameterAnd("sessStart", ">");

        continueSession_ = new Update(getDatasource())
            .table(createAuthentication_.getTable())
            .fieldParameter("sessStart")
            .whereParameter("authId", "=");

        eraseSession_ = new Delete(getDatasource())
            .from(createAuthentication_.getTable())
            .whereParameter("authId", "=");

        wasRemembered_ = new Select(getDatasource())
            .from(createAuthentication_.getTable())
            .field("remembered")
            .whereParameter("authId", "=");

        eraseAllSessions_ = new Delete(getDatasource())
            .from(createAuthentication_.getTable());

        eraseUserSessions_ = new Delete(getDatasource())
            .from(createAuthentication_.getTable())
            .whereParameter("userId", "=");

        removeAuthentication_ = new DropTable(getDatasource())
            .table(createAuthentication_.getTable());

        removeAuthenticationSessStartIndex_ = "DROP INDEX " + table_authentication + "_IDX";

        countSessions_ = new Select(getDatasource())
            .field("count(*)")
            .from(createAuthentication_.getTable())
            .whereParameter("sessStart", ">");

        getSessionUserId_ = new Select(getDatasource())
            .field("userId")
            .from(createAuthentication_.getTable())
            .whereParameter("authId", "=");

        listSessions_ = new Select(getDatasource())
            .from(createAuthentication_.getTable())
            .whereParameter("sessStart", ">");
    }

    public boolean install()
    throws SessionManagerException {
        return _install(createAuthentication_, createAuthenticationSessStartIndex_);
    }

    public boolean remove()
    throws SessionManagerException {
        return _remove(removeAuthentication_, removeAuthenticationSessStartIndex_);
    }

    public void purgeSessions()
    throws SessionManagerException {
        _purgeSessions(purgeSessions_);
    }

    public String startSession(long userId, String authData, boolean remembered)
    throws SessionManagerException {
        int purge_decision = ThreadLocalRandom.current().nextInt(getSessionPurgeScale());
        if (purge_decision <= getSessionPurgeFrequency()) {
            purgeSessions();
        }

        return _startSession(startSession_, userId, authData, remembered);
    }

    public boolean isSessionValid(String authId, String authData)
    throws SessionManagerException {
        return _isSessionValid(isSessionValid_, isSessionValidRestrictAuthData_, authId, authData);
    }

    public boolean continueSession(String authId)
    throws SessionManagerException {
        return _continueSession(continueSession_, authId);
    }

    public boolean eraseSession(String authId)
    throws SessionManagerException {
        return _eraseSession(eraseSession_, authId);
    }

    public boolean wasRemembered(String authId)
    throws SessionManagerException {
        return _wasRemembered(wasRemembered_, authId);
    }

    public void eraseAllSessions()
    throws SessionManagerException {
        _eraseAllSessions(eraseAllSessions_);
    }

    public boolean eraseUserSessions(long userId)
    throws SessionManagerException {
        return _eraseUserSessions(eraseUserSessions_, userId);
    }

    public long countSessions()
    throws SessionManagerException {
        return _countSessions(countSessions_);
    }

    public long getSessionUserId(String authId)
    throws SessionManagerException {
        return _getSessionUserId(getSessionUserId_, authId);
    }

    public boolean listSessions(ListSessions processor)
    throws SessionManagerException {
        return _listSessions(listSessions_, processor);
    }
}
