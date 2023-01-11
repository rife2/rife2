/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import rife.xml.exceptions.ParserExecutionErrorException;
import org.xml.sax.SAXParseException;

public class ExceptionErrorRedirector extends XmlErrorRedirector {
    private String xmlPath_ = null;

    public ExceptionErrorRedirector(Xml2Data xml2Data) {
        super();

        xmlPath_ = xml2Data.getXmlPath();
    }

    public void warning(SAXParseException e) {
        throw new ParserExecutionErrorException(xmlPath_, e);
    }

    public void error(SAXParseException e) {
        throw new ParserExecutionErrorException(xmlPath_, e);
    }

    public void fatalError(SAXParseException e) {
        throw new ParserExecutionErrorException(xmlPath_, e);
    }
}

