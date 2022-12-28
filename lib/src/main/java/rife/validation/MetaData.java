/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.ExceptionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This abstract base class can be conveniently used to added {@link
 * Constrained} and {@link Validated} metadata to a POJO.
 * <p>Besides implementing all the required interfaces for you, it also sets
 * up the underlying data structures in a lazy fashion. This allows you to
 * benefit from a rich API without the memory overhead when the metadata
 * isn't used.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class MetaData implements Validated, Constrained, MetaDataMerged, MetaDataBeanAware, Cloneable {
    private Validated metaDataBean_ = this;
    private Validation validation_ = null;

    /**
     * This method is called at least once and maximum once when any meta-data
     * introspection logic is executed.
     * <p>You need to implement this method since it will be called after the
     * underlying validation context has been initialized. Incidentally, by
     * doing all your metadata setup here, you don't enforce a performance
     * penalty at each object construction like when you do this in the
     * default constructor.
     *
     * @since 1.0
     */
    public void activateMetaData() {
    }

    public final void setMetaDataBean(Object bean) {
        metaDataBean_ = (Validated) bean;

        if (validation_ != null) {
            validation_.provideValidatedBean(metaDataBean_);
        }
    }

    public Object retrieveMetaDataBean() {
        return metaDataBean_;
    }

    private void ensureActivatedMetaData() {
        if (null == validation_) {
            validation_ = new Validation();
            validation_.provideValidatedBean(metaDataBean_);
            activateMetaData();
        }
    }

    public void provideValidatedBean(Validated bean) {
        ensureActivatedMetaData();
        validation_.provideValidatedBean(bean);
    }

    public final Validated retrieveValidatedBean() {
        ensureActivatedMetaData();
        return validation_.retrieveValidatedBean();
    }

    public void addConstraint(ConstrainedBean constrainedBean) {
        ensureActivatedMetaData();
        validation_.addConstraint(constrainedBean);
    }

    public void addConstraint(ConstrainedProperty constrainedProperty) {
        ensureActivatedMetaData();
        validation_.addConstraint(constrainedProperty);
    }

    public ConstrainedBean getConstrainedBean() {
        ensureActivatedMetaData();
        return validation_.getConstrainedBean();
    }

    public Collection<ConstrainedProperty> getConstrainedProperties() {
        ensureActivatedMetaData();
        return validation_.getConstrainedProperties();
    }

    public boolean hasPropertyConstraint(String name) {
        ensureActivatedMetaData();
        return validation_.hasPropertyConstraint(name);
    }

    public ConstrainedProperty getConstrainedProperty(String propertyName) {
        ensureActivatedMetaData();
        return validation_.getConstrainedProperty(propertyName);
    }

    public boolean validate() {
        ensureActivatedMetaData();
        return validation_.validate();
    }

    public boolean validate(ValidationContext context) {
        ensureActivatedMetaData();
        return validation_.validate(context);
    }

    public void resetValidation() {
        ensureActivatedMetaData();
        validation_.resetValidation();
    }

    public void addValidationError(ValidationError error) {
        ensureActivatedMetaData();
        validation_.addValidationError(error);
    }

    public Set<ValidationError> getValidationErrors() {
        ensureActivatedMetaData();
        return validation_.getValidationErrors();
    }

    public int countValidationErrors() {
        ensureActivatedMetaData();
        return validation_.countValidationErrors();
    }

    public void replaceValidationErrors(Set<ValidationError> errors) {
        ensureActivatedMetaData();
        validation_.replaceValidationErrors(errors);
    }

    public void limitSubjectErrors(String subject) {
        ensureActivatedMetaData();
        validation_.limitSubjectErrors(subject);
    }

    public void unlimitSubjectErrors(String subject) {
        ensureActivatedMetaData();
        validation_.unlimitSubjectErrors(subject);
    }

    public List<String> getValidatedSubjects() {
        ensureActivatedMetaData();
        return validation_.getValidatedSubjects();
    }

    public boolean isSubjectValid(String subject) {
        ensureActivatedMetaData();
        return validation_.isSubjectValid(subject);
    }

    public void makeErrorValid(String identifier, String subject) {
        ensureActivatedMetaData();
        validation_.makeErrorValid(identifier, subject);
    }

    public void makeSubjectValid(String subject) {
        ensureActivatedMetaData();
        validation_.makeSubjectValid(subject);
    }

    public ValidationGroup addGroup(String name) {
        ensureActivatedMetaData();
        return validation_.addGroup(name);
    }

    public void focusGroup(String name) {
        ensureActivatedMetaData();
        validation_.focusGroup(name);
    }

    public void resetGroup(String name) {
        ensureActivatedMetaData();
        validation_.resetGroup(name);
    }

    public void addRule(ValidationRule rule) {
        ensureActivatedMetaData();
        validation_.addRule(rule);
    }

    public List<PropertyValidationRule> addConstrainedPropertyRules(ConstrainedProperty constrainedProperty) {
        ensureActivatedMetaData();
        return validation_.addConstrainedPropertyRules(constrainedProperty);
    }

    public List<PropertyValidationRule> generateConstrainedPropertyRules(ConstrainedProperty constrainedProperty) {
        ensureActivatedMetaData();
        return validation_.generateConstrainedPropertyRules(constrainedProperty);
    }

    public List<ValidationRule> getRules() {
        ensureActivatedMetaData();
        return validation_.getRules();
    }

    public Collection<ValidationGroup> getGroups() {
        ensureActivatedMetaData();
        return validation_.getGroups();
    }

    public ValidationGroup getGroup(String name) {
        ensureActivatedMetaData();
        return validation_.getGroup(name);
    }

    public boolean validateGroup(String name) {
        ensureActivatedMetaData();
        return validation_.validateGroup(name);
    }

    public boolean validateGroup(String name, ValidationContext context) {
        ensureActivatedMetaData();
        return validation_.validateGroup(name, context);
    }

    public Collection<String> getLoadingErrors(String propertyName) {
        ensureActivatedMetaData();
        return validation_.getLoadingErrors(propertyName);
    }

    public Object clone()
    throws CloneNotSupportedException {
        MetaData new_metadata = null;
        try {
            new_metadata = (MetaData) super.clone();

            if (validation_ != null) {
                new_metadata.validation_ = (Validation) validation_.clone();
            }

            if (this == new_metadata.metaDataBean_) {
                if (validation_ != null) {
                    new_metadata.validation_.provideValidatedBean(new_metadata);
                }
                new_metadata.metaDataBean_ = new_metadata;
            }
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.validation").severe(ExceptionUtils.getExceptionStackTrace(e));
        }

        return new_metadata;
    }
}
