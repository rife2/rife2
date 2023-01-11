/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import rife.cmf.format.Formatter;
import rife.cmf.format.ImageFormatter;
import rife.cmf.format.PlainTextFormatter;
import rife.cmf.format.RawFormatter;
import rife.cmf.format.XhtmlFormatter;
import rife.cmf.validation.CmfPropertyValidationRule;
import rife.cmf.validation.SupportedImage;
import rife.cmf.validation.SupportedXhtml;
import rife.datastructures.EnumClass;
import rife.validation.ConstrainedProperty;

/**
 * This is a typed enumeration of all the mime types that the content
 * management framework specifically knows about.
 * <p>The types that are defined here can be validated and transformed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class MimeType extends EnumClass<String> {
    /**
     * The {@code application/xhtml+xml} mime type.
     */
    public static final MimeType APPLICATION_XHTML = new MimeType("application/xhtml+xml") {
        public CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty) {
            return new SupportedXhtml(constrainedProperty.getPropertyName(), constrainedProperty.isFragment());
        }

        public Formatter getFormatter() {
            return new XhtmlFormatter();
        }
    };
    /**
     * The {@code image/gif} mime type.
     */
    public static final MimeType IMAGE_GIF = new MimeType("image/gif") {
        public CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty) {
            return new SupportedImage(constrainedProperty.getPropertyName());
        }

        public Formatter getFormatter() {
            return new ImageFormatter();
        }
    };
    /**
     * The {@code image/jpeg} mime type.
     */
    public static final MimeType IMAGE_JPEG = new MimeType("image/jpeg") {
        public CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty) {
            return new SupportedImage(constrainedProperty.getPropertyName());
        }

        public Formatter getFormatter() {
            return new ImageFormatter();
        }
    };
    /**
     * The {@code image/png} mime type.
     */
    public static final MimeType IMAGE_PNG = new MimeType("image/png") {
        public CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty) {
            return new SupportedImage(constrainedProperty.getPropertyName());
        }

        public Formatter getFormatter() {
            return new ImageFormatter();
        }
    };
    /**
     * The {@code text/plain} mime type.
     */
    public static final MimeType TEXT_PLAIN = new MimeType("text/plain") {
        public CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty) {
            return null;
        }

        public Formatter getFormatter() {
            return new PlainTextFormatter();
        }
    };
    /**
     * The {@code text/plain} mime type.
     */
    public static final MimeType TEXT_XML = new MimeType("text/xml") {
        public CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty) {
            return null;
        }

        public Formatter getFormatter() {
            return new PlainTextFormatter();
        }
    };
    /**
     * A generic mime type indicating that the content should be stored as raw
     * data without any mime-type related processing.
     */
    public static final MimeType RAW = new MimeType("raw") {
        public CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty) {
            return null;
        }

        public Formatter getFormatter() {
            return new RawFormatter();
        }
    };

    /**
     * Constructs and returns a CMF-specific validation rule that is able to
     * validate data for this mime type.
     *
     * @param constrainedProperty an instance of the property for which the
     *                            validation rule has to be built
     * @return an instance of the validation rule
     * @since 1.0
     */
    public abstract CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty);

    /**
     * Returns an instance of the formatter for this mime type.
     *
     * @return an instance of the formatter
     * @since 1.0
     */
    public abstract Formatter getFormatter();

    /**
     * Returns the {@code MimeType} instance that corresponds to a given
     * textual identifier.
     *
     * @param identifier the identifier of the mime type that has to be
     *                   retrieved
     * @return the requested {@code MimeType}; or
     * <p>{@code null} if the {@code MimeType} is not supported
     * @since 1.0
     */
    public static MimeType getMimeType(String identifier) {
        return getMember(MimeType.class, identifier);
    }

    MimeType(String identifier) {
        super(MimeType.class, identifier);
    }
}
