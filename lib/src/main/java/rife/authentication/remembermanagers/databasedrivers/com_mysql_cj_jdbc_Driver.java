/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;

public class com_mysql_cj_jdbc_Driver extends generic {
    public com_mysql_cj_jdbc_Driver(Datasource datasource) {
        super(datasource);

        removeRememberMomentIndex_ = "DROP INDEX " + RifeConfig.authentication().getTableRemember() + "_moment_IDX ON " + RifeConfig.authentication().getTableRemember();
    }
}
