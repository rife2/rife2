/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartMissingBoundaryException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = 834186723483021895L;

    public MultipartMissingBoundaryException() {
        super("Couldn't obtain a boundary seperator.");
    }
}
