/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class ParserExecutionErrorException extends XmlErrorException {
    @Serial private static final long serialVersionUID = -1818881432299217047L;

    private final String xmlPath_;

    public ParserExecutionErrorException(String xmlPath, Throwable cause) {
        super("Error during the parsing of '" + xmlPath + "'.", cause);

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
