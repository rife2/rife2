/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader.image;

import ij.io.Opener;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.loader.ImageContentLoaderBackend;
import rife.cmf.loader.LoadedContent;
import rife.tools.ExceptionUtils;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.Set;

/**
 * This is an image loader back-end that uses ImageJ to load TIFF files, if
 * its classes are present in the classpath.
 * <p>More information about ImageJ can be obtained from
 * <a href="https://imagej.net">https://imagej.net</a>.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ImageJLoader extends ImageContentLoaderBackend {
    public LoadedContent<Image> loadFromBytes(byte[] data, Set<String> errors)
    throws ContentManagerException {
        return new LoaderDelegate().load(data, errors);
    }

    public boolean isBackendPresent() {
        try {
            return null != Class.forName("ij.io.Opener");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static class LoaderDelegate {
        public LoadedContent<Image> load(byte[] data, Set<String> errors)
        throws ContentManagerException {
            var in = new ByteArrayInputStream(data);

            try {
                var imagej = new Opener().openTiff(in, "cmfdata");
                if (imagej != null) {
                    return new LoadedContent<>(null, imagej.getImage());
                }
            } catch (Throwable e) {
                if (errors != null) {
                    errors.add(ExceptionUtils.getExceptionStackTrace(e));
                }
            }

            return null;
        }
    }
}
