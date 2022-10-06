/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;

public interface Query {
    public void clear();

    public String getSql()
    throws DbQueryException;

    public QueryParameters getParameters();

    public Capabilities getCapabilities();

    public void setExcludeUnsupportedCapabilities(boolean flag);
}
