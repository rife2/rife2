/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class ResourceBundleNotFoundException extends ProcessingException {
    @Serial private static final long serialVersionUID = -4573009433238119230L;

    private final String templateName_;
    private final String valueTag_;
    private final String bundleName_;

    public ResourceBundleNotFoundException(String templateName, String valueTag, String bundleName) {
        super("Couldn't find the resource bundle '" + bundleName + "' in template '" + templateName + "' while processing the filtered value tag '" + valueTag + "'.");

        templateName_ = templateName;
        valueTag_ = valueTag;
        bundleName_ = bundleName;
    }

    public String getTemplateName() {
        return templateName_;
    }

    public String getValueTag() {
        return valueTag_;
    }

    public String getBundleName() {
        return bundleName_;
    }
}