/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements.exceptions;

import rife.engine.exceptions.EngineException;

import java.io.Serial;

public class UndefinedAuthenticationRememberManagerException extends EngineException {
    @Serial private static final long serialVersionUID = 5888560019892891803L;

    public UndefinedAuthenticationRememberManagerException() {
        super("The RememberManager is null, maybe this authentication type doesn't support remember Remember Me functionalities.");
    }
}
