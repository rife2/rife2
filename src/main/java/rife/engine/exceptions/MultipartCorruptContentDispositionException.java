/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartCorruptContentDispositionException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = 4589739604600239664L;

    private final String contentDisposition_;

    public MultipartCorruptContentDispositionException(String contentDisposition) {
        super("The content disposition line '" + contentDisposition + "' is corrupt.");

        contentDisposition_ = contentDisposition;
    }

    public String getContentType() {
        return contentDisposition_;
    }
}
