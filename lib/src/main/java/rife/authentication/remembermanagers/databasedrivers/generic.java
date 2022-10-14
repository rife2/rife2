/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.databasedrivers;

import rife.authentication.exceptions.RememberManagerException;
import rife.authentication.remembermanagers.DatabaseRemember;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.CreateTable;
import rife.database.queries.Delete;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;

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

    public boolean install()
    throws RememberManagerException {
        return _install(createRemember_, createRememberMomentIndex_);
    }

    public boolean remove()
    throws RememberManagerException {
        return _remove(removeRemember_, removeRememberMomentIndex_);
    }

    public String createRememberId(long userId, String hostIp)
    throws RememberManagerException {
        return _createRememberId(createRememberId_, userId, hostIp);
    }

    public boolean eraseRememberId(String rememberId)
    throws RememberManagerException {
        return _eraseRememberId(eraseRememberId_, rememberId);
    }

    public boolean eraseUserRememberIds(long userId)
    throws RememberManagerException {
        return _eraseUserRememberIds(eraseUserRememberIds_, userId);
    }

    public void eraseAllRememberIds()
    throws RememberManagerException {
        _eraseAllRememberIds(eraseAllRememberIds_);
    }

    public long getRememberedUserId(String rememberId)
    throws RememberManagerException {
        return _getRememberedUserId(getRememberedUserId_, rememberId);
    }

    public void purgeRememberIds()
    throws RememberManagerException {
        _purgeRememberIds(purgeRememberIds_);
    }
}
