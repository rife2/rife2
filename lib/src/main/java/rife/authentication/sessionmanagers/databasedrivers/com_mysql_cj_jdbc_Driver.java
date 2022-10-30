/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;

public class com_mysql_cj_jdbc_Driver extends generic {
    public com_mysql_cj_jdbc_Driver(Datasource datasource) {
        super(datasource);

        removeAuthenticationSessStartIndex_ = "DROP INDEX " + RifeConfig.authentication().getTableAuthentication() + "_IDX ON " + RifeConfig.authentication().getTableAuthentication();
    }
}
