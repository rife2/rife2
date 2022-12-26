/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.Select;

public class oracle_jdbc_driver_OracleDriver extends generic {
    public oracle_jdbc_driver_OracleDriver(Datasource datasource) {
        super(datasource);

        getLatestContentInfo_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentInfo())
            .join(RifeConfig.cmf().getTableContentRepository())
            .field(RifeConfig.cmf().getTableContentInfo() + ".*")
            .where(RifeConfig.cmf().getTableContentInfo() + ".repositoryId = " + RifeConfig.cmf().getTableContentRepository() + ".repositoryId")
            .whereParameterAnd(RifeConfig.cmf().getTableContentRepository() + ".name", "repository", "=")
            .startWhereAnd()
                .whereParameter("path", "=")
                .startWhereOr()
                    .whereParameter("path", "pathpart", "=")
                    .whereParameterAnd(RifeConfig.cmf().getTableContentInfo() + ".name", "namepart", "=")
                .end()
            .end()
            .orderBy("version", Select.DESC);
    }
}
