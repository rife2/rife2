/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class ElementMemberFieldUncloneableException extends CloneNotSupportedException {
    @Serial
    private static final long serialVersionUID = -5459045970180439274L;

    private String implementation_ = null;
    private String field_ = null;

    public ElementMemberFieldUncloneableException(String implementation, String field, Throwable cause) {
        super("The implementation '" + implementation + "' of element has the member field '" + field + "' which can't be cloned.");
        if (cause != null) {
            initCause(cause);
        }

        implementation_ = implementation;
        field_ = field;
    }

    public String getImplementation() {
        return implementation_;
    }

    public String getField() {
        return field_;
    }
}
