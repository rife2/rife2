/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.Collection;

/**
 * This abstract class extends the <code>AbstractValidationRule</code> class
 * to provide common functionality that is useful for all bean property
 * validation rules.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class PropertyValidationRule extends AbstractValidationRule {
    private String propertyName_ = null;
    private String subject_ = null;
    private Collection<String> loadingErrors_ = null;
    private ConstrainedProperty constrainedProperty_ = null;

    /**
     * Instantiates a new <code>PropertyValidationRule</code> instance.
     *
     * @param propertyName the name of the property
     * @since 1.0
     */
    protected PropertyValidationRule(String propertyName) {
        setPropertyName(propertyName);
    }

    /**
     * Set the name of the property.
     *
     * @param propertyName the name of the property
     * @see #getPropertyName
     * @since 1.0
     */
    public <T extends PropertyValidationRule> T setPropertyName(String propertyName) {
        propertyName_ = propertyName;
        if (null == subject_) {
            subject_ = propertyName;
        }

        return (T) this;
    }

    /**
     * Retrieves the name of the property.
     *
     * @return the name of the property
     * @see #setPropertyName
     * @since 1.0
     */
    public String getPropertyName() {
        return propertyName_;
    }

    /**
     * Set the subject that the property refers to.
     *
     * @param subjectName the subject name of the property
     * @see #getSubject
     * @since 1.0
     */
    public PropertyValidationRule setSubject(String subjectName) {
        if (null == subjectName) {
            subject_ = propertyName_;
        } else {
            subject_ = subjectName;
        }

        return this;
    }

    /**
     * Retrieves the subject name of the property.
     *
     * @return the subject name of the property
     * @see #setSubject
     * @since 1.0
     */
    public String getSubject() {
        return subject_;
    }

    /**
     * Set the list of error messages that occurred during the loading of
     * content data.
     *
     * @param errors the collection of errors messages
     * @since 1.0
     */
    protected void setLoadingErrors(Collection<String> errors) {
        loadingErrors_ = errors;
    }

    /**
     * Retrieves the list of error messages that occurred during the loading
     * of content data.
     *
     * @return the collection of errors messages; or
     * <p><code>null</code> if the data was <code>null</code> or the property
     * didn't exist
     * @since 1.0
     */
    public Collection<String> getLoadingErrors() {
        return loadingErrors_;
    }

    void setConstrainedProperty(ConstrainedProperty constrainedProperty) {
        constrainedProperty_ = constrainedProperty;
    }

    ConstrainedProperty getConstrainedProperty() {
        return constrainedProperty_;
    }
}
