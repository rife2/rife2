/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements.exceptions;

import rife.engine.exceptions.EngineException;

import java.io.Serial;

public class UndefinedLogoutRememberManagerException extends EngineException {
    @Serial private static final long serialVersionUID = 2230068458914504593L;

    public UndefinedLogoutRememberManagerException() {
        super("The RememberManager is null, maybe this type of logout element doesn't support remember Remember Me functionalities.");
    }
}
