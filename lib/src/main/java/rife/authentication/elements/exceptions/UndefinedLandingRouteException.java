/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements.exceptions;

import rife.engine.exceptions.EngineException;

import java.io.Serial;

public class UndefinedLandingRouteException extends EngineException {
    @Serial private static final long serialVersionUID = -7894213816631306187L;

    public UndefinedLandingRouteException() {
        super("The landing route for this AuthConfig is missing.");
    }
}
