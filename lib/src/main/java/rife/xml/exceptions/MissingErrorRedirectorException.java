/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class MissingErrorRedirectorException extends XmlErrorException {
    @Serial private static final long serialVersionUID = -169087962015254692L;

    private final String xmlPath_;

    public MissingErrorRedirectorException(String xmlPath) {
        super("An error redirector couldn't be obtained from Rep for the parsing of '" + xmlPath + "'.");

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
