/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.IOException;
import java.io.Serial;

public class MultipartFileErrorException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = 7862529899155331130L;

    private final String fileName_;

    public MultipartFileErrorException(String fileName, IOException e) {
        super("Unexpected error while saving the contents of file '" + fileName + "'.", e);

        fileName_ = fileName;
    }

    public String getFileName() {
        return fileName_;
    }
}
