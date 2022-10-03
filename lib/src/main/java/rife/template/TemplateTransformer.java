/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.resources.ResourceFinder;
import rife.template.exceptions.TemplateException;

import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

public interface TemplateTransformer {
    Collection<URL> transform(String templateName, URL resource, OutputStream result, String encoding)
    throws TemplateException;

    ResourceFinder getResourceFinder();

    void setResourceFinder(ResourceFinder resourceFinder);

    String getEncoding();

    String getState();
}

