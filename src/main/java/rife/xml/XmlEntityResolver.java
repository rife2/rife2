/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import rife.resources.ResourceFinder;
import rife.resources.ResourceFinderClasspath;
import rife.xml.exceptions.CantFindEntityException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class XmlEntityResolver implements EntityResolver {
    private final ResourceFinder resourceFinder_;
    private HashMap<String, String> catalog_ = null;
    private boolean restrictToCatalog_ = false;

    public XmlEntityResolver(ResourceFinder resourcefinder) {
        resourceFinder_ = resourcefinder;
    }

    public XmlEntityResolver addToCatalog(String original, String alias) {
        if (null == catalog_) {
            catalog_ = new HashMap<>();
        }

        catalog_.put(original, alias);

        return this;
    }

    public XmlEntityResolver restrictToCatalog(boolean restrict) {
        restrictToCatalog_ = restrict;

        return this;
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        assert systemId != null;
        assert systemId.length() > 0;

        if (catalog_ != null) {
            var alias = catalog_.get(systemId);
            if (alias != null) {
                systemId = alias;
            } else if (restrictToCatalog_) {
                throw new CantFindEntityException(systemId, null);
            }
        } else if (restrictToCatalog_) {
            throw new CantFindEntityException(systemId, null);
        }

        URL resource = null;

        if (systemId.startsWith("http://") || systemId.startsWith("https://")) {
            try {
                resource = new URL(systemId);
                return new XmlInputSource(resource);
            } catch (MalformedURLException e) {
                resource = null;
            }
        }

        // fix around Resin's incompatible classloader resource urls
        resource = resourceFinder_.getResource(systemId);

        if (resource != null) {
            return new XmlInputSource(resource);
        }

        // support orion's classloader resource url
        if (systemId.startsWith("classloader:/")) {
            systemId = systemId.substring("classloader:/".length());
        }
        // support weblogic's classloader resource url
        if (systemId.startsWith("zip:/")) {
            systemId = systemId.substring("zip:/".length());
        }
        if (systemId.startsWith("jar:/")) {
            systemId = systemId.substring("jar:/".length());
        }
        if (systemId.startsWith("tx:/")) {
            systemId = systemId.substring("tx:/".length());
        }
        if (systemId.startsWith("file:/")) {
            systemId = systemId.substring("file:/".length());
        }
        if (systemId.startsWith("//")) {
            systemId = systemId.substring("//".length());
        }
        var jar_entry_index = systemId.lastIndexOf("!/");
        if (jar_entry_index != -1) {
            systemId = systemId.substring(jar_entry_index + "!/".length());
        }

        resource = resourceFinder_.getResource(systemId);

        if (resource != null) {
            return new XmlInputSource(resource);
        }

        resource = ResourceFinderClasspath.instance().getResource(systemId);

        if (null == resource) {
            throw new CantFindEntityException(systemId, null);
        }

        return new XmlInputSource(resource);
    }
}


