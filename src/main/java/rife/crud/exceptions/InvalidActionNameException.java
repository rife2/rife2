/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class InvalidActionNameException extends CrudException {
    @Serial private static final long serialVersionUID = 468726089508834835L;

    private final String name_;

    public InvalidActionNameException(String name) {
        super("The action name '" + name + "' isn't usable in a URL, use letters, digits, underscores and dashes.");

        name_ = name;
    }

    public String getName() {
        return name_;
    }
}
