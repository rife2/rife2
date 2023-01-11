/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import java.util.ArrayList;
import java.util.Collection;

import org.xml.sax.SAXParseException;

public class LoggingErrorRedirector extends XmlErrorRedirector {
    private ArrayList<SAXParseException> warnings_ = new ArrayList<>();
    private ArrayList<SAXParseException> errors_ = new ArrayList<>();
    private ArrayList<SAXParseException> fatalErrors_ = new ArrayList<>();

    public LoggingErrorRedirector() {
    }

    public synchronized void warning(SAXParseException e) {
        if (null == warnings_) {
            warnings_ = new ArrayList<>();
        }
        warnings_.add(e);
    }

    public synchronized void error(SAXParseException e) {
        if (null == errors_) {
            errors_ = new ArrayList<>();
        }
        errors_.add(e);
    }

    public synchronized void fatalError(SAXParseException e) {
        if (null == fatalErrors_) {
            fatalErrors_ = new ArrayList<>();
        }
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



