/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class CouldntAccessResourceFileException extends ResourceFinderErrorException {
    @Serial
    private static final long serialVersionUID = -5803514478814762581L;

    private final String fileName_;

    public CouldntAccessResourceFileException(String fileName) {
        super("The resource file '" + fileName + "' couldn't be found.");

        fileName_ = fileName;
    }

    public String getFileName() {
        return fileName_;
    }
}
