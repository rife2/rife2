/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import java.util.*;

import org.xml.sax.SAXParseException;

public class LoggingErrorRedirector extends XmlErrorRedirector {
    private final List<SAXParseException> warnings_ = new ArrayList<>();
    private final List<SAXParseException> errors_ = new ArrayList<>();
    private final List<SAXParseException> fatalErrors_ = new ArrayList<>();

    public LoggingErrorRedirector() {
    }

    public void warning(SAXParseException e) {
        warnings_.add(e);
    }

    public void error(SAXParseException e) {
        errors_.add(e);
    }

    public void fatalError(SAXParseException e) {
        fatalErrors_.add(e);
    }

    public Collection<SAXParseException> getWarnings() {
        return warnings_;
    }

    public Collection<SAXParseException> getErrors() {
        return errors_;
    }

    public Collection<SAXParseException> getFatalErrors() {
        return fatalErrors_;
    }

    public boolean hasWarnings() {
        return warnings_ != null &&
               !warnings_.isEmpty();
    }

    public boolean hasErrors() {
        return errors_ != null &&
               !errors_.isEmpty();
    }

    public boolean hasFatalErrors() {
        return fatalErrors_ != null &&
               !fatalErrors_.isEmpty();
    }
}



