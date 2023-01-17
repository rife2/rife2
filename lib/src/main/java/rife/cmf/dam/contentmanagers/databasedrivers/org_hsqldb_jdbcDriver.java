/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers.databasedrivers;

import rife.cmf.dam.contentmanagers.DatabaseContentInfo;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.Insert;
import rife.database.queries.Select;

public class org_hsqldb_jdbcDriver extends generic {
    public org_hsqldb_jdbcDriver(Datasource datasource) {
        super(datasource);

        getVersion_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentInfo())
            .field("MAX(version)+1")
            .whereParameter("repositoryId", "=")
            .whereParameterAnd("path", "=");

        storeContentInfo_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentInfo())
            .fieldsParameters(DatabaseContentInfo.class)
            .fieldParameter("repositoryId")
            .fieldSubselect(getVersion_)
            .fieldCustom("version", "COALESCE((" + getVersion_.getSql() + "), 0)");
    }
}
