/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class ResourceFinderErrorException extends Exception {
    @Serial
    private static final long serialVersionUID = 1319480895691642864L;

    public ResourceFinderErrorException(String message) {
        super(message);
    }

    public ResourceFinderErrorException(String message, Throwable e) {
        super(message, e);
    }
}
