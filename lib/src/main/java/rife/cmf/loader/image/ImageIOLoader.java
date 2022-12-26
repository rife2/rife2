/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader.image;

import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.loader.ImageContentLoaderBackend;
import rife.tools.ExceptionUtils;
import rife.tools.ImageWaiter;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * This is an image loader back-end that uses ImageIO to load image files.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ImageIOLoader extends ImageContentLoaderBackend {
    public Image loadFromBytes(byte[] data, Set<String> errors)
    throws ContentManagerException {
		var is = new ByteArrayInputStream(data);
        Image image;

        try {
            // create an awt image and wait 'till it's fully loaded
            image = ImageIO.read(is);
            ImageWaiter.wait(image);
        } catch (Throwable e) {
            if (errors != null) {
                errors.add(ExceptionUtils.getExceptionStackTrace(e));
            }

            image = null;
        }

        return image;
    }

    public boolean isBackendPresent() {
        return true;
    }
}
