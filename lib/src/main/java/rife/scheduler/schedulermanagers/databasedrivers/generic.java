/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.schedulermanagers.databasedrivers;

import rife.database.Datasource;
import rife.scheduler.exceptions.SchedulerManagerException;
import rife.scheduler.schedulermanagers.DatabaseScheduling;

public class generic extends DatabaseScheduling {
    public generic(Datasource datasource) {
        super(datasource);
    }

    public boolean install()
    throws SchedulerManagerException {
        return install_();
    }

    public boolean remove()
    throws SchedulerManagerException {
        return remove_();
    }
}
