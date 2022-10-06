/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

import java.io.Serial;
import java.io.Serializable;

public class SerializationErrorException extends SerializationUtilsErrorException {
    @Serial private static final long serialVersionUID = -5423479498545742611L;

    private final Serializable serializable_;

    public SerializationErrorException(Serializable serializable, Throwable cause) {
        super("Errors occurred during the serialization of '" + serializable + "'.", cause);

        serializable_ = serializable;
    }

    public Serializable getSerializable() {
        return serializable_;
    }
}
