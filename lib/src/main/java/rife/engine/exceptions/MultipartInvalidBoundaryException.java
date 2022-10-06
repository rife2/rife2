/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartInvalidBoundaryException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = -4310903799969602620L;

    private final String boundary_;
    private final String line_;

    public MultipartInvalidBoundaryException(String boundary, String line) {
        super("The boundary '" + boundary + "' wasn't found at the beginning of line '" + line + "'.");

        boundary_ = boundary;
        line_ = line;
    }

    public String getBoundary() {
        return boundary_;
    }

    public String getLine() {
        return line_;
    }
}
