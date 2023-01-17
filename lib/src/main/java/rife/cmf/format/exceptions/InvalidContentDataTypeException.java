/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format.exceptions;

import rife.cmf.MimeType;
import rife.cmf.format.Formatter;

import java.io.Serial;

public class InvalidContentDataTypeException extends FormatException {
    @Serial private static final long serialVersionUID = -8080073439600024252L;

    private final Formatter formatter_;
    private final MimeType mimeType_;
    private final Class expectedType_;
    private final Class receivedType_;

    public InvalidContentDataTypeException(Formatter formatter, MimeType mimeType, Class expectedType, Class receivedType) {
        super("The formatter '" + formatter.getClass().getName() + "' received content with mime type '" + mimeType + "' and the data should have been of type '" + expectedType.getName() + "', instead it was '" + receivedType.getName() + "'.", null);

        formatter_ = formatter;
        mimeType_ = mimeType;
        expectedType_ = expectedType;
        receivedType_ = receivedType;
    }

    public Formatter getFormatter() {
        return formatter_;
    }

    public MimeType getMimeType() {
        return mimeType_;
    }

    public Class getExpectedType() {
        return expectedType_;
    }

    public Class getReceivedType() {
        return receivedType_;
    }
}
