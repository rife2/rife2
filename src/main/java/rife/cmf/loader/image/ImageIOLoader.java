/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader.image;

import rife.cmf.MimeType;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.loader.ImageContentLoaderBackend;
import rife.cmf.loader.LoadedContent;
import rife.tools.ExceptionUtils;
import rife.tools.ImageWaiter;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.Set;

/**
 * This is an image loader back-end that uses ImageIO to load image files.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ImageIOLoader extends ImageContentLoaderBackend {
    @Override
    public LoadedContent<Image> loadFromBytes(byte[] data, Set<String> errors)
    throws ContentManagerException {
        var input = new ByteArrayInputStream(data);
        Image image;
        MimeType mime_type = null;

        try {
            var stream = ImageIO.createImageInputStream(input);
            if (stream == null) {
                throw new IIOException("Can't create an ImageInputStream");
            }

            var iter = ImageIO.getImageReaders(stream);
            if (!iter.hasNext()) {
                return null;
            }

            var reader = iter.next();

            // detect if any of the reader mimetypes corresponds to a CMF supported one
            var reader_mime_types = reader.getOriginatingProvider().getMIMETypes();
            if (reader_mime_types != null) {
                for (var reader_mime : reader_mime_types) {
                    mime_type = MimeType.getMimeType(reader_mime);
                    if (mime_type != null) {
                        break;
                    }
                }
            }

            // create an awt image
            var param = reader.getDefaultReadParam();
            reader.setInput(stream, true, true);
            try {
                image = reader.read(0, param);
            } finally {
                reader.dispose();
                stream.close();
            }

            if (image == null) {
                stream.close();
            }

            // wait until the image is fully loaded
            ImageWaiter.wait(image);
        } catch (Throwable e) {
            if (errors != null) {
                errors.add(ExceptionUtils.getExceptionStackTrace(e));
            }

            image = null;
        }

        return new LoadedContent<>(mime_type, image);
    }

    @Override
    public boolean isBackendPresent() {
        return true;
    }
}
