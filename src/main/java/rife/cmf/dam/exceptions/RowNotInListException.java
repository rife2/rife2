/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class RowNotInListException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 7226109384057712635L;

    private final String table_;
    private final int id_;
    private final long restriction_;

    public RowNotInListException(String table, int id, long restriction) {
        super("The row '" + id + "' of the table '" + table + "' isn't in the '" + restriction + "' list that is being ordered.");

        table_ = table;
        id_ = id;
        restriction_ = restriction;
    }

    public String getTable() {
        return table_;
    }

    public int getId() {
        return id_;
    }

    public long getRestriction() {
        return restriction_;
    }
}
