/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class InputSourceCreationErrorException extends XmlErrorException {
    @Serial private static final long serialVersionUID = -2195826702300143715L;

    private final String xmlPath_;

    public InputSourceCreationErrorException(String xmlPath) {
        super("Can't get input source for '" + xmlPath + "'.");

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
