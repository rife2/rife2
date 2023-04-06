/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument.exceptions;

import java.io.Serial;

/**
 * This exception is thrown and when the bytecode for a class couldn't be
 * found.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ClassBytesNotFoundException extends Exception {
    @Serial private static final long serialVersionUID = -7419114745142128756L;

    private final String filename_;

    public ClassBytesNotFoundException(String filename, Throwable cause) {
        super("Unexpected exception while loading the bytes of class file '" + filename + "'.", cause);

        filename_ = filename;
    }

    public String getFilename() {
        return filename_;
    }
}
