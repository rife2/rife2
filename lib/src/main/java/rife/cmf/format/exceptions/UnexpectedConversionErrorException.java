/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format.exceptions;

import java.io.Serial;

public class UnexpectedConversionErrorException extends FormatException {
    @Serial private static final long serialVersionUID = 4479156837469537886L;

    public UnexpectedConversionErrorException(Throwable cause) {
        super("An unexpected conversion error occurred.", cause);
    }
}
