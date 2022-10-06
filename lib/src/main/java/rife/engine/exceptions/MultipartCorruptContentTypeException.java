/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartCorruptContentTypeException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = 4867933809123299599L;

    private final String contentType_;

    public MultipartCorruptContentTypeException(String contentType) {
        super("The content type line '" + contentType + "' is corrupt.");

        contentType_ = contentType;
    }

    public String getContentType() {
        return contentType_;
    }
}
