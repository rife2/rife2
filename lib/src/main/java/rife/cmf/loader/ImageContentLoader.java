/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader;

import rife.cmf.loader.image.*;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads raw content as image data. The internal type to which everything will
 * be converted is <code>java.awt.Image</code>.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.cmf.loader.ContentLoader
 * @since 1.0
 */
public class ImageContentLoader extends ContentLoader<Image> {
    private static final List<ContentLoaderBackend<Image>> sBackends;

    static {
        sBackends = new ArrayList<>();
        sBackends.add(new ImageIOLoader());
        sBackends.add(new ImageJLoader());
    }

    public List<ContentLoaderBackend<Image>> getBackends() {
        return sBackends;
    }
}
