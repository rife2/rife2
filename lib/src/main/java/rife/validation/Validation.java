/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.*;

import rife.tools.ExceptionUtils;
import rife.tools.ObjectUtils;

import java.util.logging.Logger;

public class Validation implements ValidatedConstrained, Cloneable, Constrained, ConstrainedPropertyListener {
    private boolean activated_ = false;

    private Validated validatedBean_ = null;
    private List<ValidationRule> validationRules_ = null;
    private List<String> validatedSubjects_ = null;
    private Map<String, ConstrainedProperty> constrainedProperties_ = null;
    private Set<String> activePropertyConstraints_ = null;
    private Map<String, ValidationGroup> validationGroups_ = null;
    private ConstrainedBean constrainedBean_ = null;

    private Set<ValidationError> validationErrors_ = null;
    private List<String> errorLimitedSubjects_ = null;


    public Validation() {
    }

    /**
     * This method is called at least once and maximum once when any method
     * related to Validated rules, subjects and group or Constrained
     * properties are used.
     * <p>By overriding this method, you can thus isolate all the validation
     * setup code and don't enforce a performance penalty at each object
     * construction when doing it in the default constructor.
     *
     * @since 1.0
     */
    protected void activateValidation() {
    }

    public void provideValidatedBean(Validated bean) {
        if (validationRules_ != null) {
            for (ValidationRule rule : validationRules_) {
                if (rule.getBean() == validatedBean_) {
                    rule.setBean(bean);
                }
            }
        }

        validatedBean_ = bean;
    }

    public Validated retrieveValidatedBean() {
        if (null == validatedBean_) {
            return this;
        }

        return validatedBean_;
    }

    private void ensureActivatedValidation() {
        if (activated_) {
            return;
        }
        activated_ = true;

        activateValidation();
    }

    public ValidationGroup addGroup(String name) {
        ensureActivatedValidation();

        if (null == validationGroups_) {
            validationGroups_ = new HashMap<String, ValidationGroup>();
        }

        ValidationGroup group = new ValidationGroup(name, this);
        validationGroups_.put(name, group);
        return group;
    }

    public void focusGroup(String name) {
        ensureActivatedValidation();

        if (null == validationGroups_ ||
            null == validationErrors_ ||
            null == name) {
            return;
        }

        ValidationGroup group = validationGroups_.get(name);
        if (null == group) {
            return;
        }

        Set<ValidationError> retained_errors = new LinkedHashSet<ValidationError>();
        List<String> retained_subjects = group.getSubjects();
        for (ValidationError error : validationErrors_) {
            if (retained_subjects.contains(error.getSubject())) {
                retained_errors.add(error);
            }
        }
        validationErrors_ = retained_errors;
    }

    public void resetGroup(String name) {
        ensureActivatedValidation();

        if (null == validationGroups_ ||
            null == validationErrors_ ||
            null == name) {
            return;
        }

        ValidationGroup group = validationGroups_.get(name);
        if (null == group) {
            return;
        }

        Set<ValidationError> retained_errors = new LinkedHashSet<ValidationError>();
        List<String> group_subjects = group.getSubjects();
        for (ValidationError error : validationErrors_) {
            if (!group_subjects.contains(error.getSubject())) {
                retained_errors.add(error);
            }
        }
        validationErrors_ = retained_errors;
    }

    public void addRule(ValidationRule rule) {
        ensureActivatedValidation();

        if (null == rule) {
            return;
        }

        if (null == validationRules_) {
            validationRules_ = new ArrayList<ValidationRule>();
        }
        if (null == validatedSubjects_) {
            validatedSubjects_ = new ArrayList<String>();
        }

        if (null == rule.getBean()) {
            rule.setBean(retrieveValidatedBean());
        }

        validationRules_.add(rule);
        String subject = rule.getSubject();
        if (!validatedSubjects_.contains(subject)) {
            validatedSubjects_.add(subject);
        }
    }

    private ValidationRule addConstrainedPropertyRule(ConstrainedProperty constrainedProperty, PropertyValidationRule rule) {
        rule.setConstrainedProperty(constrainedProperty);
        rule.setSubject(constrainedProperty.getSubjectName());
        addRule(rule);

        return rule;
    }

    public List<PropertyValidationRule> generateConstrainedPropertyRules(ConstrainedProperty constrainedProperty) {
        List<PropertyValidationRule> rules = new ArrayList<PropertyValidationRule>();
        if (constrainedProperty.isNotNull()) {
            rules.add(new ValidationRuleNotNull(constrainedProperty.getPropertyName()));
        }
        if (constrainedProperty.isNotEmpty()) {
            rules.add(new ValidationRuleNotEmpty(constrainedProperty.getPropertyName()));
        }
        if (constrainedProperty.isNotEqual()) {
            rules.add(new ValidationRuleNotEqual(constrainedProperty.getPropertyName(), constrainedProperty.getNotEqual()));
        }
        if (constrainedProperty.hasLimitedLength()) {
            rules.add(new ValidationRuleLimitedLength(constrainedProperty.getPropertyName(), constrainedProperty.getMinLength(), constrainedProperty.getMaxLength()));
        }
        if (constrainedProperty.isEmail()) {
            rules.add(new ValidationRuleEmail(constrainedProperty.getPropertyName()));
        }
        if (constrainedProperty.isUrl()) {
            rules.add(new ValidationRuleUrl(constrainedProperty.getPropertyName()));
        }
        if (constrainedProperty.matchesRegexp()) {
            rules.add(new ValidationRuleRegexp(constrainedProperty.getPropertyName(), constrainedProperty.getRegexp()));
        }
        if (constrainedProperty.isLimitedDate()) {
            rules.add(new ValidationRuleLimitedDate(constrainedProperty.getPropertyName(), constrainedProperty.getMinDate(), constrainedProperty.getMaxDate()));
        }
        if (constrainedProperty.isInList()) {
            rules.add(new ValidationRuleInList(constrainedProperty.getPropertyName(), constrainedProperty.getInList()));
        }
        if (constrainedProperty.isRange()) {
            rules.add(new ValidationRuleRange(constrainedProperty.getPropertyName(), constrainedProperty.getRangeBegin(), constrainedProperty.getRangeEnd()));
        }
        if (constrainedProperty.isSameAs()) {
            rules.add(new ValidationRuleSameAs(constrainedProperty.getPropertyName(), constrainedProperty.getSameAs()));
        }
        if (constrainedProperty.isFormatted()) {
            rules.add(new ValidationRuleFormat(constrainedProperty.getPropertyName(), constrainedProperty.getFormat()));
        }
        // TODO : cmf
//        if (constrainedProperty.hasMimeType()) {
//            PropertyValidationRule rule = constrainedProperty.getMimeType().getValidationRule(constrainedProperty);
//            if (rule != null) {
//                rules.add(rule);
//            }
//        }

        return rules;
    }

    public List<PropertyValidationRule> addConstrainedPropertyRules(ConstrainedProperty constrainedProperty) {
        ensureActivatedValidation();

        if (null == constrainedProperty) {
            return null;
        }

        if (null == constrainedProperties_) {
            constrainedProperties_ = new LinkedHashMap<String, ConstrainedProperty>();
        }

        // store the constrained property and obtain the old one if it exists
        ConstrainedProperty old_constrained_property = constrainedProperties_.put(constrainedProperty.getPropertyName(), constrainedProperty);
        if (old_constrained_property != null &&
            validationRules_ != null) {
            // obtain all validation rules that were generated by the old constrained property
            ArrayList<ValidationRule> rules_to_remove = new ArrayList<ValidationRule>();
            for (ValidationRule rule : validationRules_) {
                if (rule instanceof PropertyValidationRule) {
                    if (old_constrained_property == ((PropertyValidationRule) rule).getConstrainedProperty()) {
                        rules_to_remove.add(rule);
                    }
                }
            }

            // remove all validation rules that were generated by the old constrained property
            validationRules_.removeAll(rules_to_remove);

            // merge constraints
            Map<String, Object> merged_constraints = new HashMap<String, Object>(old_constrained_property.getConstraints());
            merged_constraints.putAll(constrainedProperty.getConstraints());
            constrainedProperty.getConstraints().putAll(merged_constraints);
        }

        // add the validation rules of the new constrained property
        List<PropertyValidationRule> rules = generateConstrainedPropertyRules(constrainedProperty);
        for (PropertyValidationRule rule : rules) {
            addConstrainedPropertyRule(constrainedProperty, rule);
        }

        // register which constraint names are active
        if (null == activePropertyConstraints_) {
            activePropertyConstraints_ = new HashSet<String>();
        }
        synchronized (activePropertyConstraints_) {
            activePropertyConstraints_.addAll(constrainedProperty.getConstraints().keySet());
        }

        // register this validation object as the listener of future constraint additions to the property
        constrainedProperty.addListener(this);

        // unregister this bean from the old constrained property
        if (old_constrained_property != null) {
            old_constrained_property.removeListener(this);
        }

        return rules;
    }

    public void addConstraint(ConstrainedProperty constrainedProperty) {
        addConstrainedPropertyRules(constrainedProperty);
    }

    public void addConstraint(ConstrainedBean constrainedBean) {
        if (null == constrainedBean) {
            return;
        }

        if (constrainedBean_ != null) {
            HashMap<String, Object> merged_constraints = constrainedBean_.getConstraints();
            merged_constraints.putAll(constrainedBean.getConstraints());
            constrainedBean_.getConstraints().putAll(merged_constraints);
        }

        constrainedBean_ = constrainedBean;
    }

    public void addValidationError(ValidationError newError) {
        if (null == newError) {
            return;
        }

        if (null == validationErrors_) {
            validationErrors_ = new LinkedHashSet<ValidationError>();
        }

        if (errorLimitedSubjects_ != null &&
            errorLimitedSubjects_.contains(newError.getSubject()) &&
            !isSubjectValid(newError.getSubject())) {
            return;
        }

        // Handle the overridable errors.
        ValidationError error_to_remove = null;
        for (ValidationError error : validationErrors_) {
            if (error.getSubject().equals(newError.getSubject())) {
                // If the new error is overridable, don't add it since there's already
                // and error present for the same subject.
                if (newError.isOverridable()) {
                    return;
                }
                // If an error is present that is overridable, remember it so that it
                // can be removed when the new error is added.
                else if (error.isOverridable()) {
                    error_to_remove = error;
                    break;
                }
            }
        }

        if (error_to_remove != null) {
            validationErrors_.remove(error_to_remove);
        }

        validationErrors_.add(newError);
    }

    public List<ValidationRule> getRules() {
        ensureActivatedValidation();

        if (null == validationRules_) {
            validationRules_ = new ArrayList<ValidationRule>();
        }

        return validationRules_;
    }

    public Collection<ConstrainedProperty> getConstrainedProperties() {
        ensureActivatedValidation();

        if (null == constrainedProperties_) {
            constrainedProperties_ = new LinkedHashMap<String, ConstrainedProperty>();
        }

        return constrainedProperties_.values();
    }

    public boolean hasPropertyConstraint(String name) {
        if (null == activePropertyConstraints_) {
            return false;
        }

        return activePropertyConstraints_.contains(name);
    }

    public ConstrainedProperty getConstrainedProperty(String propertyName) {
        ensureActivatedValidation();

        if (null == propertyName ||
            0 == propertyName.length() ||
            null == constrainedProperties_) {
            return null;
        }

        return constrainedProperties_.get(propertyName);
    }

    public Collection<ValidationGroup> getGroups() {
        ensureActivatedValidation();

        if (null == validationGroups_) {
            validationGroups_ = new HashMap<String, ValidationGroup>();
        }

        return validationGroups_.values();
    }

    public ValidationGroup getGroup(String name) {
        ensureActivatedValidation();

        if (null == name ||
            0 == name.length() ||
            null == validationGroups_) {
            return null;
        }

        return validationGroups_.get(name);
    }

    public ConstrainedBean getConstrainedBean() {
        ensureActivatedValidation();

        return constrainedBean_;
    }

    private boolean validateSubjects(List<String> subjects) {
        ensureActivatedValidation();

        if (validationRules_ != null &&
            validationRules_.size() > 0) {
            for (ValidationRule rule : validationRules_) {
                if (subjects != null &&
                    !subjects.contains(rule.getSubject())) {
                    continue;
                }

                if (!rule.validate()) {
                    addValidationError(rule.getError());
                }
            }
        }

        return 0 == countValidationErrors();
    }

    public boolean validate() {
        return validateSubjects(null);
    }

    public boolean validate(ValidationContext context) {
        if (context != null) {
            context.validate(retrieveValidatedBean());
        }

        validateSubjects(null);

        return 0 == countValidationErrors();
    }

    public boolean validateGroup(String name) {
        return validateGroup(name, null);
    }

    public boolean validateGroup(String name, ValidationContext context) {
        ensureActivatedValidation();

        if (null == name ||
            null == validationGroups_) {
            return true;
        }

        List<String> subjects = null;
        ValidationGroup group = validationGroups_.get(name);
        if (group != null) {
            subjects = group.getSubjects();
        }

        if (null == subjects) {
            return true;
        }

        if (context != null) {
            context.validate(retrieveValidatedBean());
        }

        return validateSubjects(subjects);
    }

    public int countValidationErrors() {
        if (null == validationErrors_) {
            return 0;
        }

        return validationErrors_.size();
    }

    public void resetValidation() {
        if (validationErrors_ != null) {
            validationErrors_ = new LinkedHashSet<ValidationError>();
        }
    }

    public Set<ValidationError> getValidationErrors() {
        if (null == validationErrors_) {
            validationErrors_ = new LinkedHashSet<ValidationError>();
        }

        return validationErrors_;
    }

    public void replaceValidationErrors(Set<ValidationError> errors) {
        validationErrors_ = errors;
    }

    public void limitSubjectErrors(String subject) {
        if (null == subject) {
            return;
        }

        if (null == errorLimitedSubjects_) {
            errorLimitedSubjects_ = new ArrayList<String>();
        }

        if (!errorLimitedSubjects_.contains(subject)) {
            errorLimitedSubjects_.add(subject);
        }
    }

    public void unlimitSubjectErrors(String subject) {
        if (null == subject) {
            return;
        }

        if (null == errorLimitedSubjects_) {
            return;
        }

        errorLimitedSubjects_.remove(subject);
    }

    public boolean isSubjectValid(String subject) {
        if (null == subject) {
            return true;
        }

        if (null == validationErrors_) {
            return true;
        }

        boolean valid = true;

        for (ValidationError error : validationErrors_) {
            if (error.getSubject().equals(subject)) {
                valid = false;
                break;
            }
        }

        return valid;
    }

    public void makeSubjectValid(String subject) {
        if (null == subject) {
            return;
        }

        if (null == validationErrors_) {
            return;
        }

        ArrayList<ValidationError> errors_to_remove = new ArrayList<ValidationError>();

        for (ValidationError error : validationErrors_) {
            if (error.getSubject().equals(subject)) {
                errors_to_remove.add(error);
            }
        }

        for (ValidationError error_to_remove : errors_to_remove) {
            validationErrors_.remove(error_to_remove);
        }
    }

    public void makeErrorValid(String identifier, String subject) {
        if (null == subject) {
            return;
        }

        if (null == identifier) {
            return;
        }

        if (null == validationErrors_) {
            return;
        }

        ArrayList<ValidationError> errors_to_remove = new ArrayList<ValidationError>();

        for (ValidationError error : validationErrors_) {
            if (error.getSubject().equals(subject) &&
                error.getIdentifier().equals(identifier)) {
                errors_to_remove.add(error);
            }
        }

        for (ValidationError error_to_remove : errors_to_remove) {
            validationErrors_.remove(error_to_remove);
        }
    }

    public List<String> getValidatedSubjects() {
        ensureActivatedValidation();

        if (null == validatedSubjects_) {
            validatedSubjects_ = new ArrayList<String>();
        }

        return validatedSubjects_;
    }

    public static String getErrorIndication(Validated validated, String subject, String valid, String error) {
        if (null != validated &&
            !validated.isSubjectValid(subject)) {
            return error;
        } else {
            return valid;
        }
    }

    public Collection<String> getLoadingErrors(String propertyName) {
        if (null == propertyName) {
            return null;
        }

        for (ValidationRule rule : getRules()) {
            if (rule instanceof PropertyValidationRule) {
                PropertyValidationRule property_rule = (PropertyValidationRule) rule;
                if (propertyName.equals(property_rule.getPropertyName()) &&
                    property_rule.getLoadingErrors() != null &&
                    property_rule.getLoadingErrors().size() > 0) {
                    return property_rule.getLoadingErrors();
                }
            }
        }

        return null;
    }

    public void constraintSet(ConstrainedProperty property, String name, Object constraintData) {
        if (null == activePropertyConstraints_) {
            activePropertyConstraints_ = new HashSet<String>();
        }
        activePropertyConstraints_.add(name);
    }

    public Object clone()
    throws CloneNotSupportedException {
        Validation new_validation = null;
        try {
            new_validation = (Validation) super.clone();

            if (validationRules_ != null) {
                new_validation.validationRules_ = ObjectUtils.deepClone(validationRules_);
                for (ValidationRule rule : (ArrayList<ValidationRule>) new_validation.validationRules_) {
                    if (this == rule.getBean()) {
                        rule.setBean(new_validation);
                    }
                }
            }

            if (this == new_validation.validatedBean_) {
                new_validation.validatedBean_ = new_validation;
            }

            if (validationErrors_ != null) {
                new_validation.validationErrors_ = new LinkedHashSet<ValidationError>(validationErrors_);
            }

            if (constrainedProperties_ != null) {
                new_validation.constrainedProperties_ = new LinkedHashMap<String, ConstrainedProperty>();
                for (Map.Entry<String, ConstrainedProperty> entry_property : constrainedProperties_.entrySet()) {
                    new_validation.constrainedProperties_.put(entry_property.getKey(), entry_property.getValue());
                    new_validation.addConstraint(entry_property.getValue().clone());
                }
            }

            if (activePropertyConstraints_ != null) {
                new_validation.activePropertyConstraints_ = new HashSet<String>(activePropertyConstraints_);
            }

            if (errorLimitedSubjects_ != null) {
                new_validation.errorLimitedSubjects_ = new ArrayList<String>(errorLimitedSubjects_);
            }

            if (validationGroups_ != null) {
                new_validation.validationGroups_ = new HashMap<String, ValidationGroup>();
                ValidationGroup new_group;
                for (ValidationGroup group : validationGroups_.values()) {
                    new_group = group.clone();
                    new_group.setValidation(new_validation);
                    new_validation.validationGroups_.put(new_group.getName(), new_group);
                }
            }
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.validation").severe(ExceptionUtils.getExceptionStackTrace(e));
        }

        return new_validation;
    }
}
