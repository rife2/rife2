/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.validation;

import rife.cmf.loader.ImageContentLoader;
import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.ValidationError;

import java.util.HashSet;
import java.util.Set;

/**
 * A validation rule that checks if the data in a property can be
 * loaded as a supported image format without errors.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class SupportedImage extends CmfPropertyValidationRule {
    /**
     * Creates a new {@code SupportedImage} instance.
     *
     * @param propertyName the name of the property that has to be validated
     * @since 1.0
     */
    public SupportedImage(String propertyName) {
        super(propertyName, false);
    }

    @Override
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
		var loaded = new ImageContentLoader().load(value, getFragment(), errors);
        if (!errors.isEmpty()) {
            setLoadingErrors(errors);
        }
        if (null == loaded) {
            return false;
        }

        setCachedLoadedData(loaded);

        return true;
    }

    @Override
    public ValidationError getError() {
        return new ValidationError.INVALID(getSubject());
    }
}
