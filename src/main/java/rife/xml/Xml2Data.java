/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import rife.config.RifeConfig;
import rife.resources.ResourceFinder;
import rife.resources.ResourceFinderClasspath;
import rife.tools.ExceptionUtils;
import rife.xml.exceptions.*;

import javax.xml.parsers.*;

public abstract class Xml2Data extends DefaultHandler {
    private boolean disableValidation_ = false;
    private boolean enableValidation_ = false;
    private XmlErrorRedirector errorRedirector_ = null;
    private XmlEntityResolver entityResolver_ = null;
    private ResourceFinder resourceFinder_ = null;
    private Set<String> errors_ = Collections.emptySet();

    public void disableValidation(boolean activation) {
        disableValidation_ = activation;
        if (activation) {
            enableValidation_ = false;
        }
    }

    public void enableValidation(boolean activation) {
        enableValidation_ = activation;
        if (activation) {
            disableValidation_ = false;
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return entityResolver_.resolveEntity(publicId, systemId);
    }

    public void warning(SAXParseException e) {
        errorRedirector_.warning(e);
    }

    public void error(SAXParseException e) {
        errorRedirector_.error(e);
    }

    public void fatalError(SAXParseException e) {
        errorRedirector_.fatalError(e);
    }

    public ResourceFinder getResourceFinder() {
        return resourceFinder_;
    }

    public XmlErrorRedirector getErrorRedirector() {
        return errorRedirector_;
    }

    public Set<String> getErrors() {
        return errors_;
    }

    protected XmlErrorRedirector createErrorRedirector() {
        return new LoggingErrorRedirector();
    }

    protected Set<String> createErrorSet() {
        return new HashSet<>();
    }

    protected XmlEntityResolver createEntityResolver() {
        return new XmlEntityResolver(resourceFinder_);
    }

    public synchronized boolean processXml(String data)
    throws XmlErrorException {
        return processXml(data, ResourceFinderClasspath.instance());
    }

    public synchronized boolean processXml(String data, ResourceFinder resourceFinder)
    throws XmlErrorException {
        if (null == data) throw new IllegalArgumentException("data can't be null.");
        if (data.length() == 0) throw new IllegalArgumentException("data can't be empty.");
        if (null == resourceFinder) throw new IllegalArgumentException("resourceFinder can't be null.");

        resourceFinder_ = resourceFinder;

        errorRedirector_ = createErrorRedirector();
        errors_ = createErrorSet();
        entityResolver_ = createEntityResolver();

        var reader = new StringReader(data);

        var sax_parse_exception = false;
        try {
            var inputsource = new InputSource(reader);

            SAXParser parser;

            try {
                parser = SAXParserFactory.newInstance().newSAXParser();
            } catch (ParserConfigurationException | SAXException e) {
                throw new XmlErrorException(e);
            }

            try {
                if (enableValidation_ ||
                    (!disableValidation_ &&
                     (RifeConfig.xml().getXmlValidation()))) {
                    parser.getXMLReader().setFeature("http://xml.org/sax/features/validation", true);
                } else {
                    parser.getXMLReader().setFeature("http://xml.org/sax/features/validation", false);
                }
            } catch (SAXException e) {
                Logger.getLogger("com.uwyn.rife.xml").warning("The parser '" + reader.getClass().getName() + "' doesn't support validation.");
            }

            try {
                parser.parse(inputsource, this);
            } catch (SAXParseException e) {
                sax_parse_exception = true;
                errors_.add(formatException(e));
            } catch (SAXException e) {
                if (e.getException() != null &&
                    e.getException() instanceof RuntimeException) {
                    throw (RuntimeException) e.getException();
                } else {
                    throw new XmlErrorException(e);
                }
            } catch (IOException e) {
                throw new XmlErrorException(e);
            }

            if (errorRedirector_.hasErrors()) {
                errors_.addAll(formatExceptions(errorRedirector_.getErrors()));
            }
            if (errorRedirector_.hasFatalErrors()) {
                errors_.addAll(formatExceptions(errorRedirector_.getFatalErrors()));
            }
        } catch (RuntimeException e) {
            errors_.add(e.getMessage());
        }


        return !sax_parse_exception &&
               errors_.isEmpty() &&
               !errorRedirector_.hasErrors() &&
               !errorRedirector_.hasFatalErrors();
    }

    private Collection<String> formatExceptions(Collection<SAXParseException> exceptions) {
        if (null == exceptions) {
            return null;
        }

        var result = new ArrayList<String>();
        for (var e : exceptions) {
            result.add(formatException(e));
        }

        return result;
    }

    private String formatException(SAXParseException e) {
        var formatted = new StringBuilder();
        if (e.getSystemId() != null) {
            formatted.append(e.getSystemId());
        }

        if (e.getPublicId() != null) {
            if (formatted.length() > 0) {
                formatted.append(", ");
            }
            formatted.append(e.getPublicId());
        }

        if (e.getLineNumber() >= 0) {
            if (formatted.length() > 0) {
                formatted.append(", ");
            }
            formatted.append("line ");
            formatted.append(e.getLineNumber());
        }

        if (e.getColumnNumber() >= 0) {
            if (formatted.length() > 0) {
                formatted.append(", ");
            }
            formatted.append("col ");
            formatted.append(e.getColumnNumber());
        }

        if (formatted.length() > 0) {
            formatted.append(" : ");
        }
        formatted.append(e.getMessage());

        return formatted.toString();
    }
}
