/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import rife.tools.exceptions.ControlFlowRuntimeException;
import rife.tools.exceptions.LightweightError;

import java.io.Serial;

public class RedirectException extends LightweightError implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = -4659026261646459653L;

    private final String url_;

    public RedirectException(Object url) {
        super();

        url_ = String.valueOf(url);
    }

    public String getUrl() {
        return url_;
    }
}
