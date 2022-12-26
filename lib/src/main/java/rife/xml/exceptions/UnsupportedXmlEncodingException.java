/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class UnsupportedXmlEncodingException extends XmlErrorException {
    @Serial private static final long serialVersionUID = 1760212231607214310L;

    private final String xmlPath_;

    public UnsupportedXmlEncodingException(String xmlPath, Throwable e) {
        super("Error while creation input stream reader for '" + xmlPath + "'.", e);

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
