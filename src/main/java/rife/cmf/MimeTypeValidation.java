/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import rife.cmf.validation.CmfPropertyValidationRule;
import rife.cmf.validation.SupportedImage;
import rife.cmf.validation.SupportedXhtml;
import rife.validation.ConstrainedProperty;

/**
 * Returns appropriate validation rules for supported CMF mime types.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.7
 */
public final class MimeTypeValidation {
    private MimeTypeValidation() {
    }

    /**
     * Constructs and returns a CMF-specific validation rule that is able to validate data for this mime type.
     *
     * @param mimeType the mime type for which the validation rule has to be built
     * @param constrainedProperty an instance of the property for which the validation rule has to be built
     * @return an instance of the validation rule; or {@code null} of no validation rule could be created
     * @since 1.7
     */
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
