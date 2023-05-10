package rife.cmf;

import rife.cmf.validation.CmfPropertyValidationRule;
import rife.cmf.validation.SupportedImage;
import rife.cmf.validation.SupportedXhtml;
import rife.validation.ConstrainedProperty;

public final class MimeTypeValidation {
    private MimeTypeValidation() {
    }

    public static CmfPropertyValidationRule getValidationRule(MimeType mimeType, ConstrainedProperty constrainedProperty) {
        switch (mimeType.getIdentifier()) {
            case MimeType.APPLICATION_XHTML_IDENTIFIER -> {
                return new SupportedXhtml(constrainedProperty.getPropertyName(), constrainedProperty.isFragment());
            }
            case MimeType.IMAGE_GIF_IDENTIFIER, MimeType.IMAGE_JPEG_IDENTIFIER, MimeType.IMAGE_PNG_IDENTIFIER -> {
                return new SupportedImage(constrainedProperty.getPropertyName());
            }
            case MimeType.TEXT_PLAIN_IDENTIFIER, MimeType.TEXT_XML_IDENTIFIER, MimeType.RAW_IDENTIFIER -> {
                return null;
            }
        }
        return null;
    }
}
