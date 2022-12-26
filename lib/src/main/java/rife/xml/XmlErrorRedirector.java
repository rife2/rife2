/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import java.util.Collection;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public abstract class XmlErrorRedirector implements ErrorHandler {
    XmlErrorRedirector() {
    }

    public abstract void warning(SAXParseException e);

    public abstract void error(SAXParseException e);

    public abstract void fatalError(SAXParseException e);

    public Collection<SAXParseException> getWarnings() {
        return null;
    }

    public Collection<SAXParseException> getErrors() {
        return null;
    }

    public Collection<SAXParseException> getFatalErrors() {
        return null;
    }

    public boolean hasWarnings() {
        return false;
    }

    public boolean hasErrors() {
        return false;
    }

    public boolean hasFatalErrors() {
        return false;
    }
}



