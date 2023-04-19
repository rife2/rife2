/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import java.io.File;
import java.io.Serial;

public class StoreXmlErrorException extends ConfigErrorException {
    @Serial
    private static final long serialVersionUID = -1414121432509201830L;

    private File destination_ = null;

    public StoreXmlErrorException(File destination, Throwable cause) {
        super("An error occurred while storing the XML data to the destination file '" + destination.getAbsolutePath() + "'.", cause);

        destination_ = destination;
    }

    public File getDestination() {
        return destination_;
    }
}
