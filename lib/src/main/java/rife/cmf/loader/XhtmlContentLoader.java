/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader;

import rife.cmf.loader.xhtml.SAXLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads raw content as xhtml data. The internal type to which everything will
 * be converted is <code>java.lang.String</code>.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.cmf.loader.ContentLoader
 * @since 1.0
 */
public class XhtmlContentLoader extends ContentLoader<String> {
    private static final List<ContentLoaderBackend<String>> sBackends;

    static {
        sBackends = new ArrayList<>();
        sBackends.add(new SAXLoader());
    }

    public List<ContentLoaderBackend<String>> getBackends() {
        return sBackends;
    }
}
