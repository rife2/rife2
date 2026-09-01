/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class DuplicateActionNameException extends CrudException {
    @Serial private static final long serialVersionUID = 4210775149277855393L;

    private final String name_;

    public DuplicateActionNameException(String name) {
        super("The action name '" + name + "' is already used, provide another one.");

        name_ = name;
    }

    public String getName() {
        return name_;
    }
}
