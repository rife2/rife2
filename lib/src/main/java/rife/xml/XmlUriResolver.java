/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import rife.resources.ResourceFinder;

import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

public class XmlUriResolver implements URIResolver {
    private final ResourceFinder resourceFinder_;

    public XmlUriResolver(ResourceFinder resourcefinder) {
        resourceFinder_ = resourcefinder;
    }

    public Source resolve(String href, String base)
    throws TransformerException {
        var resource = resourceFinder_.getResource(href);
        if (null == resource) {
            return null;
        }

        return new SAXSource(new XmlInputSource(resource));
    }
}

