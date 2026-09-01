/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class DuplicateSectionException extends CrudException {
    @Serial private static final long serialVersionUID = -8267412059877382156L;

    private final String label_;

    public DuplicateSectionException(String label) {
        super("The section '" + label + "' is already declared, provide another label.");

        label_ = label;
    }

    public String getLabel() {
        return label_;
    }
}
