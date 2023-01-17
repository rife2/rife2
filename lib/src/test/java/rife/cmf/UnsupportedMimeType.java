/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import rife.cmf.format.Formatter;
import rife.cmf.validation.CmfPropertyValidationRule;
import rife.validation.ConstrainedProperty;

public abstract class UnsupportedMimeType extends MimeType {
    public static final MimeType UNSUPPORTED = new MimeType("unsupported/unknown") {
        public CmfPropertyValidationRule getValidationRule(ConstrainedProperty constrainedProperty) {
            return null;
        }

        public Formatter getFormatter() {
            return null;
        }
    };

    UnsupportedMimeType(String identifier) {
        super(identifier);
    }
}
