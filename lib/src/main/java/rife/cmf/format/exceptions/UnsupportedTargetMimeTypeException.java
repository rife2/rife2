/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format.exceptions;

import rife.cmf.MimeType;

import java.io.Serial;

public class UnsupportedTargetMimeTypeException extends FormatException {
    @Serial private static final long serialVersionUID = -8908623401198633615L;

    private final MimeType mimeType_;

    public UnsupportedTargetMimeTypeException(MimeType mimeType) {
        super("Unsupported target mime type '" + mimeType + "'", null);

        mimeType_ = mimeType;
    }

    public MimeType getMimeType() {
        return mimeType_;
    }
}
