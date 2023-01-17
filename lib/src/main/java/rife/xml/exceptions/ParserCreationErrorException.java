/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class ParserCreationErrorException extends XmlErrorException {
    @Serial private static final long serialVersionUID = 4369908122970192903L;

    private final String xmlPath_;

    public ParserCreationErrorException(String xmlPath, Throwable cause) {
        super("Error during the creation of the parser for '" + xmlPath + "'.", cause);

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
