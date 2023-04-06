/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartFileTooBigException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = -649024432766475910L;

    private final String fileName_;
    private final long sizeLimit_;

    public MultipartFileTooBigException(String fileName, long sizeLimit) {
        super("The size of the uploaded file '" + fileName + "' exceeds " + sizeLimit + " which is the maximum.");

        fileName_ = fileName;
        sizeLimit_ = sizeLimit;
    }

    public String getFileName() {
        return fileName_;
    }

    public long getSizeLimit() {
        return sizeLimit_;
    }
}
