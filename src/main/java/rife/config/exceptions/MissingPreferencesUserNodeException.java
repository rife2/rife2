/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import java.io.Serial;

public class MissingPreferencesUserNodeException extends ConfigErrorException {
    @Serial
    private static final long serialVersionUID = -22983055927535074L;

    public MissingPreferencesUserNodeException() {
        super("No preferences user node has been specified, therefore it's impossible to store the configuration through Java's preferences mechanism.");
    }
}
