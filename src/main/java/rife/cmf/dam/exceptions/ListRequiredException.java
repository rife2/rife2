/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class ListRequiredException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 8473625190364817265L;

    private final String table_;
    private final String restrictColumn_;

    public ListRequiredException(String table, String restrictColumn) {
        super("The rows of the table '" + table + "' are partitioned into separate lists by '" + restrictColumn +
              "', provide the value that names the list this operation applies to.");

        table_ = table;
        restrictColumn_ = restrictColumn;
    }

    public String getTable() {
        return table_;
    }

    public String getRestrictColumn() {
        return restrictColumn_;
    }
}
