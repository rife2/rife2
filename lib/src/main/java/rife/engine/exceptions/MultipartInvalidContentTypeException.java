/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class MultipartInvalidContentTypeException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = 780518501619092791L;

    private final String contentType_;

    public MultipartInvalidContentTypeException(String contentType) {
        super("The content type '" + contentType + "' isn't valid for a multipart request.");

        contentType_ = contentType;
    }

    public String getContentType() {
        return contentType_;
    }
}
