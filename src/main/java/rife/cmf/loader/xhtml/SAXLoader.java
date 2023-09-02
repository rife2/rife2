/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader.xhtml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import rife.cmf.MimeType;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.loader.LoadedContent;
import rife.cmf.loader.XhtmlContentLoaderBackend;
import rife.resources.ResourceFinderClasspath;
import rife.template.TemplateFactory;
import rife.xml.LoggingErrorRedirector;
import rife.xml.XmlEntityResolver;
import rife.xml.XmlErrorRedirector;
import rife.xml.exceptions.XmlErrorException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class SAXLoader extends XhtmlContentLoaderBackend {
    public LoadedContent<String> loadFromString(String data, boolean fragment, Set<String> errors)
    throws ContentManagerException {
        return new LoaderDelegate().load(data, fragment, errors);
    }

    public boolean isBackendPresent() {
        return true;
    }

    private static class LoaderDelegate extends DefaultHandler {
        private final XmlErrorRedirector errorRedirector_ = new LoggingErrorRedirector();
        private final XmlEntityResolver entityResolver_ = new XmlEntityResolver(ResourceFinderClasspath.instance())
            .addToCatalog("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", "/dtd/cmf/xhtml1-transitional.dtd")
            .addToCatalog("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "/dtd/cmf/xhtml1-strict.dtd")
            .addToCatalog("http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd", "/dtd/cmf/xhtml1-frameset.dtd")
            .addToCatalog("http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent", "/dtd/cmf/xhtml-lat1.ent")
            .addToCatalog("http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent", "/dtd/cmf/xhtml-symbol.ent")
            .addToCatalog("http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent", "/dtd/cmf/xhtml-special.ent")
            .restrictToCatalog(true);

        public InputSource resolveEntity(String publicId, String systemId) {
            return entityResolver_.resolveEntity(publicId, systemId);
        }

        public void warning(SAXParseException e) {
            errorRedirector_.warning(e);
        }

        public void fatalError(SAXParseException e) {
            errorRedirector_.fatalError(e);
        }

        public void error(SAXParseException e) {
            errorRedirector_.error(e);
        }

        public LoadedContent<String> load(String data, boolean fragment, Set<String> errors)
        throws ContentManagerException {
            var complete_page = data;

            if (fragment) {
                var t = TemplateFactory.HTML.get("cmf.container.template");
                t.setValue("fragment", data);
                complete_page = t.getContent();
            }

            Reader reader = new StringReader(complete_page);

            var sax_parse_exception = false;
            try {
                var inputsource = new InputSource(reader);

                SAXParser parser = null;

                try {
                    parser = SAXParserFactory.newInstance().newSAXParser();
                } catch (ParserConfigurationException | SAXException e) {
                    throw new XmlErrorException(e);
                }

                try {
                    parser.getXMLReader().setFeature("http://xml.org/sax/features/validation", true);
                } catch (SAXException e) {
                    throw new XmlErrorException("The parser '" + parser.getClass().getName() + "' doesn't support validation.", e);
                }

                try {
                    parser.parse(inputsource, this);
                } catch (SAXParseException e) {
                    sax_parse_exception = true;
                    if (errors != null) {
                        errors.add(formatException(fragment, e));
                    }
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

                if (errors != null) {
                    if (errorRedirector_.hasErrors()) {
                        errors.addAll(formatExceptions(fragment, errorRedirector_.getErrors()));
                    }
                    if (errorRedirector_.hasFatalErrors()) {
                        errors.addAll(formatExceptions(fragment, errorRedirector_.getFatalErrors()));
                    }
                }
            } catch (RuntimeException e) {
                if (errors != null) {
                    errors.add(e.getMessage());
                }
                return null;
            }

            if (sax_parse_exception ||
                (errors != null &&
                    !errors.isEmpty()) ||
                (errorRedirector_.hasErrors() ||
                    errorRedirector_.hasFatalErrors())) {
                return null;
            }

            return new LoadedContent<>(MimeType.APPLICATION_XHTML, data);
        }

        private Collection<String> formatExceptions(boolean fragment, Collection<SAXParseException> exceptions) {
            if (null == exceptions) {
                return null;
            }

            var result = new ArrayList<String>();
            for (var e : exceptions) {
                result.add(formatException(fragment, e));
            }

            return result;
        }

        private String formatException(boolean fragment, SAXParseException e) {
            var formatted = new StringBuilder();
            if (e.getSystemId() != null) {
                formatted.append(e.getSystemId());
            }

            if (e.getPublicId() != null) {
                if (!formatted.isEmpty()) {
                    formatted.append(", ");
                }
                formatted.append(e.getPublicId());
            }

            if (e.getLineNumber() >= 0) {
                if (!formatted.isEmpty()) {
                    formatted.append(", ");
                }
                formatted.append("line ");
                if (fragment) {
                    formatted.append(e.getLineNumber() - 3);
                } else {
                    formatted.append(e.getLineNumber());
                }
            }

            if (e.getColumnNumber() >= 0) {
                if (!formatted.isEmpty()) {
                    formatted.append(", ");
                }
                formatted.append("col ");
                formatted.append(e.getColumnNumber());
            }

            if (!formatted.isEmpty()) {
                formatted.append(" : ");
            }
            formatted.append(e.getMessage());

            return formatted.toString();
        }
    }
}
