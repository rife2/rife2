/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers.exceptions;

import rife.cmf.MimeType;
import rife.cmf.dam.exceptions.ContentManagerException;

import java.io.Serial;

public class UnsupportedMimeTypeException extends ContentManagerException {
    @Serial private static final long serialVersionUID = 2504591167417035649L;

    private final MimeType mimeType_;

    public UnsupportedMimeTypeException(MimeType mimeType) {
        super("The mime type '" + mimeType + "' isn't supported by the content manager.");

        mimeType_ = mimeType;
    }

    public MimeType getMimeType() {
        return mimeType_;
    }
}
