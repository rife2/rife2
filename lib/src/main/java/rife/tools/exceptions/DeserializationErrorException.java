/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

import java.io.Serial;

public class DeserializationErrorException extends SerializationUtilsErrorException {
    @Serial private static final long serialVersionUID = -2436363754313490329L;

    public DeserializationErrorException(Throwable cause) {
        super("Errors occurred during deserialization.", cause);
    }
}
