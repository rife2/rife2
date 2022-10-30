/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class TransformedResultConversionException extends ProcessingException {
    @Serial private static final long serialVersionUID = -4796743001867603162L;

    private String path_ = null;

    public TransformedResultConversionException(String path, Throwable cause) {
        super("Error while converting the transformed result of the template '" + path + "'.", cause);
        path_ = path;
    }

    public String getPath() {
        return path_;
    }
}
