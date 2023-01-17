/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class XmlErrorException extends RuntimeException {
    @Serial private static final long serialVersionUID = -5984252179915986432L;

    public XmlErrorException(String message) {
        super(message);
    }

    public XmlErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlErrorException(Throwable cause) {
        super(cause);
    }
}
