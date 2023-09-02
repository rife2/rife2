/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.databasedrivers;

import rife.authentication.exceptions.RememberManagerException;
import rife.authentication.remembermanagers.DatabaseRemember;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.*;

import java.util.concurrent.ThreadLocalRandom;

public class generic extends DatabaseRemember {
    protected CreateTable createRemember_;
    protected String createRememberMomentIndex_;
    protected DropTable removeRemember_;
    protected String removeRememberMomentIndex_;
    protected Insert createRememberId_;
    protected Delete eraseRememberId_;
    protected Delete eraseUserRememberIds_;
    protected Delete eraseAllRememberIds_;
    protected Select getRememberedUserId_;
    protected Delete purgeRememberIds_;

    public generic(Datasource datasource) {
        super(datasource);

        createRemember_ = new CreateTable(getDatasource())
            .table(RifeConfig.authentication().getTableRemember())
            .column("rememberId", String.class, 40, CreateTable.NOTNULL)
            .column("userId", long.class, CreateTable.NOTNULL)
            .column("moment", long.class, CreateTable.NOTNULL)
            .primaryKey(RifeConfig.authentication().getTableRemember().toUpperCase() + "_PK", "rememberId");

        createRememberMomentIndex_ = "CREATE INDEX " + RifeConfig.authentication().getTableRemember() + "_moment_IDX ON " + RifeConfig.authentication().getTableRemember() + " (moment)";

        removeRemember_ = new DropTable(getDatasource())
            .table(createRemember_.getTable());

        removeRememberMomentIndex_ = "DROP INDEX " + RifeConfig.authentication().getTableRemember() + "_moment_IDX";

        createRememberId_ = new Insert(getDatasource())
            .into(createRemember_.getTable())
            .fieldParameter("rememberId")
            .fieldParameter("userId")
            .fieldParameter("moment");

        eraseRememberId_ = new Delete(getDatasource())
            .from(createRemember_.getTable())
            .whereParameter("rememberId", "=");

        eraseUserRememberIds_ = new Delete(getDatasource())
            .from(createRemember_.getTable())
            .whereParameter("userId", "=");

        eraseAllRememberIds_ = new Delete(getDatasource())
            .from(createRemember_.getTable());

        getRememberedUserId_ = new Select(getDatasource())
            .field("userId")
            .from(createRemember_.getTable())
            .whereParameter("rememberId", "=");

        purgeRememberIds_ = new Delete(getDatasource())
            .from(createRemember_.getTable())
            .whereParameter("moment", "<=");
    }

    @Override
    public boolean install()
    throws RememberManagerException {
        return _install(createRemember_, createRememberMomentIndex_);
    }

    @Override
    public boolean remove()
    throws RememberManagerException {
        return _remove(removeRemember_, removeRememberMomentIndex_);
    }

    @Override
    public String createRememberId(long userId)
    throws RememberManagerException {
        int purge_decision = ThreadLocalRandom.current().nextInt(getRememberPurgeScale());
        if (purge_decision <= getRememberPurgeFrequency()) {
            purgeRememberIds();
        }

        return _createRememberId(createRememberId_, userId);
    }

    @Override
    public boolean eraseRememberId(String rememberId)
    throws RememberManagerException {
        return _eraseRememberId(eraseRememberId_, rememberId);
    }

    @Override
    public boolean eraseUserRememberIds(long userId)
    throws RememberManagerException {
        return _eraseUserRememberIds(eraseUserRememberIds_, userId);
    }

    @Override
    public void eraseAllRememberIds()
    throws RememberManagerException {
        _eraseAllRememberIds(eraseAllRememberIds_);
    }

    @Override
    public long getRememberedUserId(String rememberId)
    throws RememberManagerException {
        return _getRememberedUserId(getRememberedUserId_, rememberId);
    }

    @Override
    public void purgeRememberIds()
    throws RememberManagerException {
        _purgeRememberIds(purgeRememberIds_);
    }
}
