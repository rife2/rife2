/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format.exceptions;

import rife.cmf.MimeType;

import java.io.Serial;
import java.util.Collection;

public class UnreadableDataFormatException extends FormatException {
    @Serial private static final long serialVersionUID = -8866969888137085698L;

    private final MimeType mimeType_;
    private final Collection<String> errors_;

    public UnreadableDataFormatException(MimeType mimeType, Collection<String> errors) {
        super("Impossible to read the data that has to be stored with the mime type '" + mimeType + "'", null);

        mimeType_ = mimeType;
        errors_ = errors;
    }

    public MimeType getMimeType() {
        return mimeType_;
    }

    public Collection<String> getErrors() {
        return errors_;
    }
}
