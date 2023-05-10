package rife.cmf;

import rife.cmf.format.*;

public final class MimeTypeFormatter {
    private MimeTypeFormatter() {
    }

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
