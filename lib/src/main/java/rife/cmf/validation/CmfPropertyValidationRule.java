/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.validation;

import rife.validation.Constrained;
import rife.validation.ConstrainedProperty;
import rife.validation.ConstrainedUtils;
import rife.validation.PropertyValidationRule;

/**
 * This abstract class extends the <code>PropertyValidationRule</code> class
 * to provide common functionality that is useful for all concrete CMF
 * validation rules.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class CmfPropertyValidationRule extends PropertyValidationRule {
    private final boolean fragment_;

    /**
     * Instantiates a new <code>CmfPropertyValidationRule</code> instance.
     *
     * @param propertyName the name of the property
     * @param fragment     <code>true</code> if the property is a fragment; or
     *                     <p><code>false</code> if it's a complete document
     * @since 1.0
     */
    public CmfPropertyValidationRule(String propertyName, boolean fragment) {
        super(propertyName);

        fragment_ = fragment;
    }

    /**
     * Indicates whether the property that is validated is a fragment.
     *
     * @return <code>true</code> if the property is a fragment; or
     * <p><code>false</code> if it's a complete document
     * @since 1.0
     */
    public boolean getFragment() {
        return fragment_;
    }

    /**
     * Sets the cached loaded data to a {@link rife.validation.ConstrainedProperty
     * ConstrainedProperty} if the content data has been successfully loaded during
     * validation. This prevents the data of having to be loaded again
     * elsewhere.
     * <p>If the validation rule's bean is not {@link
     * rife.validation.Constrained Constrained} or if it doesn't contain a
     * corresponding <code>ConstrainedProperty</code>, this method does nothing.
     *
     * @param data the loaded data
     * @see rife.validation.ConstrainedProperty#setCachedLoadedData(Object)
     * @since 1.0
     */
    protected void setCachedLoadedData(Object data) {
        // if the bean is constrained and a CmfProperty exists that corresponds to
        // the property name that's being checked, store the loaded data
        // and prevent it from loading twice
		var constrained = ConstrainedUtils.makeConstrainedInstance(getBean());
        if (constrained != null) {
			var property = constrained.getConstrainedProperty(getPropertyName());
            property.setCachedLoadedData(data);
        }
    }
}

