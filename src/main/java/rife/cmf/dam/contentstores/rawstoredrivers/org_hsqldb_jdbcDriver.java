/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.rawstoredrivers;

import rife.database.Datasource;
import rife.database.queries.Insert;

import java.io.IOException;
import java.io.InputStream;

public class org_hsqldb_jdbcDriver extends generic {
    public org_hsqldb_jdbcDriver(Datasource datasource) {
        super(datasource);
    }

    protected int storeChunks(Insert storeContentChunk, final int id, InputStream data)
    throws IOException {
        return storeChunksNoStream(storeContentChunk, id, data);
    }
}
