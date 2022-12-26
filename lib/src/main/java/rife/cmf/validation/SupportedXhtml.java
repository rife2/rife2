/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.validation;

import rife.cmf.loader.XhtmlContentLoader;
import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.ValidationError;

import java.util.HashSet;
import java.util.Set;

/**
 * A validation rule that checks if the data in a property can be
 * loaded as supported and valid xhtml without errors.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class SupportedXhtml extends CmfPropertyValidationRule {
    /**
     * Instantiates a new <code>SupportedXhtml</code> instance.
     *
     * @param propertyName the name of the property
     * @param fragment     <code>true</code> if the property is a fragment; or
     *                     <p><code>false</code> if it's a complete document
     * @since 1.0
     */
    public SupportedXhtml(String propertyName, boolean fragment) {
        super(propertyName, fragment);
    }

    public boolean validate() {
        Object value = null;
        try {
            value = BeanUtils.getPropertyValue(getBean(), getPropertyName());
        } catch (BeanUtilsException e) {
            // an error occurred when obtaining the value of the property
            // just consider it valid to skip over it
            return true;
        }

        if (null == value) {
            return true;
        }

        // try to load the data in the property and if that's not possible, the data
        // is considered invalid
        Set<String> errors = new HashSet<>();
        var data = new XhtmlContentLoader().load(value, getFragment(), errors);
        if (errors.size() > 0) {
            setLoadingErrors(errors);
        }
        if (null == data) {
            return false;
        }

        setCachedLoadedData(data);

        return true;
    }

    public ValidationError getError() {
        return new ValidationError.INVALID(getSubject());
    }
}


