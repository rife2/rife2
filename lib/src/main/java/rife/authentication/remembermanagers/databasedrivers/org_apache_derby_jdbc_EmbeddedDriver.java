/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;

public class org_apache_derby_jdbc_EmbeddedDriver extends generic {
    public org_apache_derby_jdbc_EmbeddedDriver(Datasource datasource) {
        super(datasource);

        createRememberMomentIndex_ = "CREATE INDEX " + RifeConfig.authentication().getTableRemember() + "_IDX ON " + RifeConfig.authentication().getTableRemember() + " (moment)";
        removeRememberMomentIndex_ = "DROP INDEX " + RifeConfig.authentication().getTableRemember() + "_IDX";
    }
}
