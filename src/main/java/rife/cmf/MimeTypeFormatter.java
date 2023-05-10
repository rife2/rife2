/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import rife.cmf.format.*;

/**
 * Returns appropriate formatter instances for supported CMF mime types.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.7
 */
public final class MimeTypeFormatter {
    private MimeTypeFormatter() {
    }

    /**
     * Returns an instance of the formatter for the provided mime type.
     *
     * @param mimeType the mime type for which a formatter should be instantiated
     * @return and appropriate formatter instance; or {@code null} if no formatter could be created
     * @since 1.7
     */
    public static Formatter getFormatter(MimeType mimeType) {
        switch (mimeType.getIdentifier()) {
            case MimeType.APPLICATION_XHTML_IDENTIFIER -> {
                return new XhtmlFormatter();
            }
            case MimeType.IMAGE_GIF_IDENTIFIER, MimeType.IMAGE_JPEG_IDENTIFIER, MimeType.IMAGE_PNG_IDENTIFIER -> {
                return new ImageFormatter();
            }
            case MimeType.TEXT_PLAIN_IDENTIFIER, MimeType.TEXT_XML_IDENTIFIER -> {
                return new PlainTextFormatter();
            }
            case MimeType.RAW_IDENTIFIER -> {
                return new RawFormatter();
            }
        }
        return null;
    }
}
