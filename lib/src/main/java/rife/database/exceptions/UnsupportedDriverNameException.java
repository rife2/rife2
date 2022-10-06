/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class UnsupportedDriverNameException extends DatabaseException {
    @Serial private static final long serialVersionUID = 6993103229317879655L;

    private final String name_;

    public UnsupportedDriverNameException(String name) {
        super("Couldn't find a supported driver class for the driver name '" + name + "'.");
        name_ = name;
    }

    public String getName() {
        return name_;
    }
}
