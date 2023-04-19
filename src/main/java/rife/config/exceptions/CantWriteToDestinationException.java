/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import java.io.File;
import java.io.Serial;

public class CantWriteToDestinationException extends ConfigErrorException {
    @Serial
    private static final long serialVersionUID = 2484476576384120796L;

    private File destination_ = null;

    public CantWriteToDestinationException(File destination) {
        super("The destination file for the XML data '" + destination.getAbsolutePath() + "' is not writable.");

        destination_ = destination;
    }

    public File getDestination() {
        return destination_;
    }
}
