/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.*;

/**
 * This interface defines methods for bean-centric data validation.
 * <p>Validation is bound to subjects that have distinct names. Each subject
 * corresponds to a different variable, for example a property of a bean. When
 * a subject is found to be invalid, a corresponding instance of
 * <code>ValidationError</code> has to be registered.
 * <p><code>ValidationError</code>s indicate in detail why a
 * <code>Validated</code> object doesn't contain valid data. They should be
 * stored internally and can be manipulated by other classes that are able to
 * work with <code>Validated</code> objects. This makes it possible to collect
 * errors incrementally in one central place and to allow each component in a
 * system to perform its own part of the validation.
 * <p>A <code>Validated</code> object has a {@link #validate() validate()}
 * method which should be used to perform mandatory validation on subjects and
 * data that the object itself knows about. This validation has to perform all
 * checks that guarantee a coherent internal state of the data. Note that this
 * method should not reset the validation, but instead add new validation
 * errors to an already existing collection.
 * <p>Since it's possible that subjects generate multiple
 * <code>ValidationError</code>s, it's possible to limit their number and only
 * store the first error that occurs for a particular subject.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ValidationError
 * @see ValidationContext
 * @since 1.0
 */
public interface Validated {
    /**
     * Validates the internal subjects.
     * <p>This method is not supposed to reset the validation errors or to
     * start the validation from scratch, but it's intended to add additional
     * errors to an existing collection.
     *
     * @return <code>true</code> if no validation errors are present after the
     * validation; or
     * <p><code>false</code> if validation errors are available.
     * @see #validate(ValidationContext)
     * @see #resetValidation()
     * @since 1.0
     */
    boolean validate();

    /**
     * Validates the internal subjects and also validates the bean within the
     * provided <code>ValidationContext</code>
     * <p>This method is not supposed to reset the validation errors or to
     * start the validation from scratch, but it's intended to add additional
     * errors to an existing collection.
     *
     * @param context the <code>ValidationContext</code> in which this bean
     *                instance will be additionally validated
     * @return <code>true</code> if no validation errors are present after the
     * validation; or
     * <p><code>false</code> if validation errors are available.
     * @see #validate()
     * @see #resetValidation()
     * @since 1.0
     */
    boolean validate(ValidationContext context);

    /**
     * <p>Adds a new validation rule.
     * <p>The collection of rules is what is supposed to perform the
     * validation, though any other additional method could be used. At least
     * those rules that have been registered will be evaluated.
     *
     * @param rule the rule that will be added
     * @see #validate()
     * @see #getRules()
     * @since 1.0
     */
    void addRule(ValidationRule rule);

    /**
     * Retrieves that validation rules that have been registered.
     *
     * @see #validate()
     * @see #addRule
     * @since 1.0
     */
    List<ValidationRule> getRules();

    /**
     * <p>Resets the validation by removing all validation errors that are
     * currently present.
     * <p>This method is typically used to start a new validation from scratch
     * or to re-validate until all errors have been solved.
     *
     * @see #validate()
     * @since 1.0
     */
    void resetValidation();

    /**
     * Add a new validation error explicitly to the collection of already
     * existing errors.
     * <p>Note that this method should respect subjects with a limited error
     * amount and only store the first error for these subjects.
     *
     * @param error the <code>ValidationError</code> to add
     * @see #limitSubjectErrors(String)
     * @see #unlimitSubjectErrors(String)
     * @since 1.0
     */
    void addValidationError(ValidationError error);

    /**
     * Returns a set with all the stored <code>ValidationError</code>s.
     *
     * @return A <code>Set</code> instance with all the stored
     * <code>ValidationError</code>s. Note that when no errors are available
     * an empty set is returned, not <code>null</code>.
     * @since 1.0
     */
    Set<ValidationError> getValidationErrors();

    /**
     * Counts the number of stored <code>ValidationError</code>s.
     *
     * @return The number of stored <code>ValidationError</code>s.
     * @since 1.0
     */
    int countValidationErrors();

    /**
     * Replaces the stored <code>ValidationError</code>s with a new set of
     * errors.
     *
     * @param errors the <code>Set</code> instance that contains all the
     *               <code>ValidationError</code>s that have to be stored.
     * @since 1.0
     */
    void replaceValidationErrors(Set<ValidationError> errors);

    /**
     * Limits the number of errors for a particular subject so that maximum
     * one <code>ValidationError</code> can be stored for it.
     *
     * @param subject the name of the subject that has to be limited.
     * @since 1.0
     */
    void limitSubjectErrors(String subject);

    /**
     * Unlimits the number of errors for a particular subject so that any
     * number of <code>ValidationError</code>s can be stored for it.
     *
     * @param subject the name of the subject that has to be unlimited.
     * @since 1.0
     */
    void unlimitSubjectErrors(String subject);

    /**
     * Returns the list of subjects that this object is able to validate
     * internally through the {@link #validate() validate()} method.
     *
     * @return a List instance with the names of the internally validated
     * subjects
     * @since 1.0
     */
    List<String> getValidatedSubjects();

    /**
     * Checks if a subject is valid.
     * <p>This is determined by verifying if there are
     * <code>ValidationError</code>s present for it. This method will thus not
     * execute a validation action.
     *
     * @param subject the name of the subject that has to be checked.
     * @return <code>true</code> when no errors could be found for the
     * subject; or
     * <p><code>false</code> when errors are present for the subject.
     * @see #validate()
     * @since 1.0
     */
    boolean isSubjectValid(String subject);

    /**
     * Makes errors for a particular subject and identifier valid.
     * <p>This is done by removing all <code>ValidationError</code>s that are
     * stored with this identifier and subject.
     *
     * @param identifier the name of the error identifier that has to be made
     * @param subject    the name of the subject that has to be made valid.
     *                   valid.
     * @since 1.0
     */
    void makeErrorValid(String identifier, String subject);

    /**
     * Makes a subject valid.
     * <p>This is done by removing all <code>ValidationError</code>s that are
     * stored for it.
     *
     * @param subject the name of the subject that has to be made valid.
     * @since 1.0
     */
    void makeSubjectValid(String subject);

    /**
     * Provide the bean instance that will be validated.
     * <p>By default '<code>this</code>' will be used.
     *
     * @param bean the bean instance that will be validated
     * @since 1.0
     */
    void provideValidatedBean(Validated bean);

    /**
     * Retrieves the bean instance that will be validated.
     *
     * @since 1.0
     */
    Validated retrieveValidatedBean();

    /**
     * Adds a new validation group.
     *
     * @param name the name of the validation group that needs to be created
     *             and added
     * @return the newly created <code>ValidationGroup</code>
     * @since 1.0
     */
    ValidationGroup addGroup(String name);

    /**
     * Focuses on one particular validation group, showing only the
     * <code>ValidationError</code>s that were generated by its
     * <code>ValidationRule</code>s.
     *
     * @param name the name of the validation group that will be focused
     * @since 1.0
     */
    void focusGroup(String name);

    /**
     * Removed all the <code>ValidationError</code>s of a particular
     * validation group.
     *
     * @param name the name of the validation group that will be focused
     * @since 1.0
     */
    void resetGroup(String name);

    /**
     * Retrieves all validation groups.
     *
     * @return the collection of all registered validation groups
     * @since 1.0
     */
    Collection<ValidationGroup> getGroups();

    /**
     * Retrieve a particular validation group.
     *
     * @param name the name of the validation group that will be retrieved
     * @return the requested <code>ValidationGroup</code>; or
     * <p><code>null</code> if no such validation group exists
     * @since 1.0
     */
    ValidationGroup getGroup(String name);

    /**
     * Validate the <code>ValidationRule</code>s of a particular validation
     * group.
     *
     * @param name the name of the validation group that will be retrieved
     * @return <code>true</code> if no validation errors were generated; or
     * <p><code>false</code> otherwise
     * @since 1.0
     */
    boolean validateGroup(String name);

    /**
     * Validate the <code>ValidationRule</code>s of a particular validation
     * group and also validates the entire bean within the provided
     * <code>ValidationContext</code>
     *
     * @param name    the name of the validation group
     * @param context the <code>ValidationContext</code> in which this bean
     *                instance will be additionally validated
     * @return <code>true</code> if no validation errors were generated; or
     * <p><code>false</code> otherwise
     * @since 1.0
     */
    boolean validateGroup(String name, ValidationContext context);

    /**
     * Adds the validation rules that are related to a particular {@link
     * ConstrainedProperty}.
     * <p>If the rules of this property name have already been added before
     * through another <code>ConstrainedProperty</code> instance, its existing
     * <code>ValidationRule</code>s will be erased and the previous
     * constraints will be merged into the new
     * <code>ConstrainedProperty</code> before adding its validation rules.
     *
     * @param constrainedProperty the ConstrainedProperty that will be
     *                            inspected
     * @return the list of generated {@link ValidationRule}s
     * @since 1.0
     */
    List<PropertyValidationRule> addConstrainedPropertyRules(ConstrainedProperty constrainedProperty);

    /**
     * Generates the validation rules that are related to a particular {@link
     * ConstrainedProperty}.
     *
     * @param constrainedProperty the ConstrainedProperty that will be
     *                            inspected
     * @return the list of generated {@link ValidationRule}s
     * @since 1.0
     */
    List<PropertyValidationRule> generateConstrainedPropertyRules(ConstrainedProperty constrainedProperty);

    /**
     * Returns the collection of error messages that occurred during the
     * loading of the content of a certain property.
     *
     * @param propertyName the name of the property whose loading errors
     *                     should be obtained
     * @return null if no errors occurred during the loading of the content of
     * the provided property or if the property doesn't exist; or
     * <p>the requested collection of error messages
     * @since 1.0
     */
    Collection<String> getLoadingErrors(String propertyName);
}
