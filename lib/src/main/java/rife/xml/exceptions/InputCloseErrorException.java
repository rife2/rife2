/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class InputCloseErrorException extends XmlErrorException {
    @Serial private static final long serialVersionUID = -6802885054707628583L;

    private final String xmlPath_;

    public InputCloseErrorException(String xmlPath, Throwable cause) {
        super("Error while closing the reader of '" + xmlPath + "'.", cause);

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
