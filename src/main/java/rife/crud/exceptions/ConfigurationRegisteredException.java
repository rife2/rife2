/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class ConfigurationRegisteredException extends IllegalStateException {
    @Serial private static final long serialVersionUID = 6261035477979328119L;

    private final String subject_;

    public ConfigurationRegisteredException(String subject) {
        super("The " + subject + " has been registered and can't be changed anymore, since the routes have already been set up.");

        subject_ = subject;
    }

    public String getSubject() {
        return subject_;
    }
}
