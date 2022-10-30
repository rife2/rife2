/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import rife.tools.exceptions.ControlFlowRuntimeException;
import rife.tools.exceptions.LightweightError;

import java.io.Serial;

public class DeferException extends LightweightError implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = 2247712070129524306L;

    public DeferException() {
        super();
    }
}
