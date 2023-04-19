/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import rife.xml.exceptions.XmlErrorException;

import java.io.Serial;

public class InitializationErrorException extends ConfigErrorException {
    @Serial
    private static final long serialVersionUID = 6946315757564943787L;

    private String xmlPath_ = null;

    public InitializationErrorException(String xmlPath, Exception cause) {
        super("Fatal error during the generation of the config from the XML document '" + xmlPath + "'.", cause);

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
