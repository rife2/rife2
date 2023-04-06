/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import rife.tools.exceptions.ControlFlowRuntimeException;
import rife.tools.exceptions.LightweightError;

import java.io.Serial;

public class RespondException extends LightweightError implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = -6488668631370269522L;

    public RespondException() {
        super();
    }
}
