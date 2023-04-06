/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartInvalidContentDispositionException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = 6757637387303238042L;

    private final String contentDisposition_;

    public MultipartInvalidContentDispositionException(String contentDisposition) {
        super("The content disposition '" + contentDisposition + "' isn't valid.");

        contentDisposition_ = contentDisposition;
    }

    public String getContentType() {
        return contentDisposition_;
    }
}
