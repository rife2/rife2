/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.exceptions;

import java.io.Serial;

public class OrdinalsOutOfRoomException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 3841027265518846722L;

    private final String table_;

    public OrdinalsOutOfRoomException(String table) {
        super("The ordinals of the table '" + table + "' have no room left to be moved through, tighten them to make room.");

        table_ = table;
    }

    public String getTable() {
        return table_;
    }
}
