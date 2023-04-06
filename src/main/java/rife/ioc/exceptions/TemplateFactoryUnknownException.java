/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc.exceptions;

import java.io.Serial;

public class TemplateFactoryUnknownException extends PropertyValueException {
    @Serial private static final long serialVersionUID = 7268695167543687153L;

    private final String type_;

    public TemplateFactoryUnknownException(String type) {
        super("The template factory with type '" + type + "' isn't known by the system.");

        type_ = type;
    }

    public String getType() {
        return type_;
    }
}
