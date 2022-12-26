/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import rife.config.RifeConfig;
import rife.resources.ResourceFinder;
import rife.resources.ResourceFinderClasspath;
import rife.tools.StringUtils;
import rife.xml.exceptions.FatalParsingErrorsException;
import rife.xml.exceptions.ParserCreationErrorException;
import rife.xml.exceptions.ParserExecutionErrorException;
import rife.xml.exceptions.XmlErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;

public abstract class Xml2Data extends DefaultHandler {
    private boolean disableValidation_ = false;
    private boolean enableValidation_ = false;
    private XmlErrorRedirector errorRedirector_ = null;
    private String xmlPath_ = null;
    private ResourceFinder resourceFinder_ = null;

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

    public void warning(SAXParseException e) {
        errorRedirector_.warning(e);
    }

    public void error(SAXParseException e) {
        errorRedirector_.error(e);
    }

    public void fatalError(SAXParseException e) {
        errorRedirector_.fatalError(e);
    }

    protected String getXmlPath() {
        return xmlPath_;
    }

    protected ResourceFinder getResourceFinder() {
        return resourceFinder_;
    }

    protected XmlErrorRedirector getErrorRedirector() {
        return errorRedirector_;
    }

    protected XmlErrorRedirector createErrorRedirector() {
        return new ExceptionErrorRedirector(this);
    }

    public synchronized SAXParser processXml(String xmlPath)
    throws XmlErrorException {
        return processXml(xmlPath, ResourceFinderClasspath.instance(), null);
    }

    public synchronized SAXParser processXml(String xmlPath, ResourceFinder resourceFinder)
    throws XmlErrorException {
        return processXml(xmlPath, resourceFinder, null);
    }

    public synchronized SAXParser processXml(String xmlPath, ResourceFinder resourceFinder, SAXParser parser)
    throws XmlErrorException {
        if (null == xmlPath) throw new IllegalArgumentException("xmlPath can't be null.");
        if (xmlPath.length() == 0) throw new IllegalArgumentException("xmlPath can't be empty.");
        if (null == resourceFinder) throw new IllegalArgumentException("resourceFinder can't be null.");

        var input_source = new XmlInputSource(xmlPath, resourceFinder);

        xmlPath_ = xmlPath;
        resourceFinder_ = resourceFinder;
        errorRedirector_ = createErrorRedirector();

        var entity_resolver = new XmlEntityResolver(resourceFinder);

        if (null == parser) {
            try {
                parser = SAXParserFactory.newInstance().newSAXParser();

                parser.getXMLReader().setEntityResolver(entity_resolver);
                parser.getXMLReader().setErrorHandler(errorRedirector_);
            } catch (ParserConfigurationException | SAXException e) {
                throw new ParserCreationErrorException(xmlPath, e);
            }
        }

        try {
            if (enableValidation_ ||
                (!disableValidation_ && RifeConfig.xml().getXmlValidation())) {
                parser.getXMLReader().setFeature("http://xml.org/sax/features/validation", true);
            } else {
                parser.getXMLReader().setFeature("http://xml.org/sax/features/validation", false);
            }
        } catch (SAXException e) {
            Logger.getLogger("rife.xml").warning("The parser '" + parser.getClass().getName() + "' doesn't support validation.");
        }

        try {
            parser.parse(input_source, this);
        } catch (SAXException e) {
            if (e.getException() != null &&
                e.getException() instanceof RuntimeException) {
                throw (RuntimeException) e.getException();
            } else {
                throw new ParserExecutionErrorException(xmlPath, e);
            }
        } catch (IOException e) {
            throw new ParserExecutionErrorException(xmlPath, e);
        }

        if (errorRedirector_.hasWarnings()) {
            Logger.getLogger("rife.xml").warning("The following XML warnings occurred during the parsing of " + xmlPath + "'.\n" + StringUtils.join(formatExceptions(errorRedirector_.getWarnings()), "\n"));
        }
        if (errorRedirector_.hasErrors()) {
            Logger.getLogger("rife.xml").severe("The following XML errors occurred during the parsing of " + xmlPath + "'.\n" + StringUtils.join(formatExceptions(errorRedirector_.getErrors()), "\n"));
        }
        if (errorRedirector_.hasFatalErrors()) {
            throw new FatalParsingErrorsException(xmlPath, formatExceptions(errorRedirector_.getFatalErrors()));
        }

        return parser;
    }

    private Collection<String> formatExceptions(Collection<SAXParseException> exceptions) {
        if (null == exceptions) {
            return null;
        }

        var result = new ArrayList<String>();
        for (var e : exceptions) {
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

            result.add(formatted.toString());
        }

        return result;
    }
}
