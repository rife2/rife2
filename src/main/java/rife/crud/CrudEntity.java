/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.cmf.dam.OrdinalSequence;
import rife.database.Datasource;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.crud.exceptions.*;
import rife.tools.BeanUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.Constrained;
import rife.validation.ConstrainedProperty;
import rife.validation.ConstrainedUtils;
import rife.database.querymanagers.generic.Callbacks;
import rife.database.querymanagers.generic.CallbacksProvider;
import rife.validation.Validated;
import rife.validation.ValidationRuleSameAs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Describes the administration of a single bean class, as it has been
 * derived from the constraints of that bean.
 * <p>The constraints are interpreted once when an entity is registered with
 * a {@link CrudAdmin}, so that the elements and the templates can simply
 * consume the resulting model. This is the only place where the constraint
 * vocabulary is translated into the structure of the administration.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudAdmin
 * @since 1.10
 */
public class CrudEntity<T> {
    private static final List<Class<?>> INTEGER_TYPES = List.of(int.class, Integer.class);
    private static final List<Class<?>> INTEGRAL_TYPES = List.of(int.class, Integer.class, long.class, Long.class,
        short.class, Short.class, byte.class, Byte.class);

    private final Class<T> beanClass_;
    private final CrudEntityOptions<T> options_;
    private final String slug_;
    private final String identifier_;
    private final List<String> listed_;
    private final List<String> addable_;
    private final List<String> editable_;
    private final List<String> searchable_;
    private final List<String> uneditedSaved_;
    private final String ordinal_;
    private final String ordinalRestriction_;
    /**
     * The identifier that an instance carries while it's being added, which
     * is what says that it isn't stored yet.
     *
     * @since 1.10
     */
    static final int NEW_IDENTIFIER = -1;

    private final Map<String, ConstrainedProperty> constraints_;
    private final Map<String, String> subjects_;
    private final Map<String, String> undisplayable_;
    private final java.util.Set<String> persisted_;
    private final java.util.Set<String> saved_;

    CrudEntity(Class<T> beanClass, CrudEntityOptions<T> options) {
        beanClass_ = beanClass;
        // the routes are set up once from these options, so a later change
        // would describe an administration that isn't mounted anywhere
        options_ = new CrudEntityOptions<>(options);

        // what an administration refuses is reported through the validation of
        // the instance that it was given, so a bean that validates nothing has
        // nowhere to say that a submitted value couldn't be read at all and
        // every operation on it would report that it succeeded
        // a bean with a metadata class of its own is merged with it, so this
        // is only about the ones that carry no metadata anywhere
        if (!Validated.class.isAssignableFrom(beanClass)) {
            throw new UnvalidatedBeanException(beanClass);
        }

        // the instance is created here rather than being asked for, so that
        // what went wrong while creating it is still known. Every operation
        // of an administration constructs one, so a bean that can't be built
        // at all is refused where it's registered instead of on the first
        // request that reaches it
        Validated instance;
        try {
            instance = (Validated) beanClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new InstanceCreationException(beanClass, e);
        }
        var constrained = instance instanceof Constrained c ? c : null;

        constraints_ = new LinkedHashMap<>();
        if (constrained != null) {
            // the constraints are copied, so that what this entity was
            // derived from stays what it keeps describing
            for (var property : constrained.getConstrainedProperties()) {
                constraints_.put(property.getPropertyName(), property.clone());
            }
        }

        slug_ = options_.slug() != null ? options_.slug() : defaultSlug(beanClass);
        if (!isValidUrlPart(slug_)) {
            throw new InvalidSlugException(beanClass, slug_);
        }

        String identifier = null;
        String ordinal = null;
        String ordinal_restriction = null;
        for (var entry : constraints_.entrySet()) {
            if (entry.getValue().isIdentifier()) {
                // which one of them would be the identifier isn't something
                // that this and the query manager would agree on
                if (identifier != null) {
                    throw new DuplicateConstraintException(beanClass, "identifier", identifier, entry.getKey());
                }
                identifier = entry.getKey();
            }
            if (entry.getValue().isOrdinal()) {
                if (ordinal != null) {
                    throw new DuplicateConstraintException(beanClass, "ordinal", ordinal, entry.getKey());
                }
                ordinal = entry.getKey();
                ordinal_restriction = entry.getValue().hasOrdinalRestriction() ? entry.getValue().getOrdinalRestriction() : null;
            }
        }
        identifier_ = identifier != null ? identifier : "id";
        ordinal_ = ordinal;
        ordinalRestriction_ = ordinal_restriction;
        // what the database keeps is decided once, before anything asks
        var properties = propertyNames(beanClass);

        // a constraint can be given a subject of its own, and everything
        // that reports about a property then names that subject instead of
        // the property itself
        // a subject that more than one property answers to, or that any
        // property of the bean is literally called, doesn't say which one it
        // belongs to, so it isn't translated at all rather than being
        // translated to whichever one happened to claim it
        subjects_ = new LinkedHashMap<>();
        var claimed = new HashSet<String>();
        for (var constraint : constraints_.values()) {
            var subject = constraint.getSubjectName();
            if (null == subject || subject.equals(constraint.getPropertyName())) {
                continue;
            }
            if (!claimed.add(subject) || properties.contains(subject)) {
                subjects_.remove(subject);
                continue;
            }
            subjects_.put(subject, constraint.getPropertyName());
        }
        persisted_ = new java.util.HashSet<String>();
        saved_ = new java.util.HashSet<String>();
        for (var property : properties) {
            if (ConstrainedUtils.persistConstrainedProperty(constrained, property, null)) {
                persisted_.add(property);
            }
            if (ConstrainedUtils.saveConstrainedProperty(constrained, property, null)) {
                saved_.add(property);
            }
        }

        verifyProperties(beanClass);

        // help that names a property nobody has would never render, which
        // reads as help that doesn't work rather than as the typo it is
        for (var helped : options_.getHelp().keySet()) {
            if (!properties.contains(helped)) {
                throw new UnknownPropertyException(beanClass, helped, "help");
            }
        }
        for (var section : options_.getSections().values()) {
            for (var sectioned : section) {
                if (!properties.contains(sectioned)) {
                    throw new UnknownPropertyException(beanClass, sectioned, "section");
                }
            }
        }

        // a property that this has nothing to show is left out of every page,
        // which is nothing that anybody is able to look for without being
        // told that it happened
        var undisplayable = new LinkedHashMap<String, String>();
        for (var property : properties) {
            var reason = undisplayable(property);
            if (reason != null) {
                undisplayable.put(property, reason);
            }
        }
        undisplayable_ = Collections.unmodifiableMap(undisplayable);
        if (!undisplayable_.isEmpty()) {
            var message = new StringBuilder("The administration of '").append(beanClass.getName()).append("' doesn't show or enter:");
            for (var entry : undisplayable_.entrySet()) {
                message.append("\n  ").append(entry.getKey()).append(" : ").append(entry.getValue());
            }
            Logger.getLogger("rife.crud").info(message.toString());
        }

        listed_ = Collections.unmodifiableList(listedProperties(properties));
        addable_ = Collections.unmodifiableList(formProperties(properties, true));
        editable_ = Collections.unmodifiableList(formProperties(properties, false));

        // a search only matches what the table shows, so a match never has
        // to be explained by a column that isn't there, and the database
        // does the matching, which takes text that it keeps
        var searchable = new ArrayList<String>();
        for (var property : listed_) {
            var type = propertyType(beanClass, property);
            if (isPersisted(property) && type != null && CharSequence.class.isAssignableFrom(type)) {
                searchable.add(property);
            }
        }
        searchable_ = Collections.unmodifiableList(searchable);

        // an update writes every saved column while the edit form only
        // enters the editable ones, so these are the ones it writes without
        // asking, and what it writes for them is carried over from the row
        // inside the update's own transaction
        // the identifier and the ordinals aren't among them, since they have
        // handling of their own that reads them where their locks are held
        var unedited = new ArrayList<String>();
        for (var property : properties) {
            if (!isSaved(property) || editable_.contains(property) ||
                property.equals(identifier_) || property.equals(ordinal_) || property.equals(ordinalRestriction_)) {
                continue;
            }
            unedited.add(property);
        }
        uneditedSaved_ = Collections.unmodifiableList(unedited);

        // a renderer for a column that the browse table doesn't show would
        // never run, which reads as a renderer that doesn't work rather than
        // as the mistake it is
        for (var column : options_.getColumns().keySet()) {
            if (!properties.contains(column)) {
                throw new UnknownPropertyException(beanClass, column, "column");
            }
            if (!listed_.contains(column)) {
                throw new UnlistedColumnException(beanClass, column);
            }
        }
        // the instance that the constraints were read from is the pristine
        // one, since nothing was stored on it and it isn't kept afterwards
        verifyAddable(beanClass, properties, instance);
    }

    // refuses the entities that this administration can't administer: the
    // identifier and the ordinals are worked with as numbers of a particular
    // width, an identifier is assigned instead of provided, and the list of
    // an instance whose restriction is null can't be named by a value, so
    // its ordinals couldn't be kept apart from those of the other lists, nor
    // renumbered or locked together with them
    private void verifyProperties(Class<T> beanClass) {
        // the query manager works with an int identifier, the ordinals are
        // int as well, while the value that names a list is a long
        verifyType(beanClass, identifier_, "identifier", INTEGER_TYPES, "an int");
        verifyType(beanClass, ordinal_, "ordinal", INTEGER_TYPES, "an int");
        verifyType(beanClass, ordinalRestriction_, "ordinal restriction", INTEGRAL_TYPES, "a whole number");

        verifyStored(beanClass, identifier_, "identifier");
        verifyStored(beanClass, ordinal_, "ordinal");
        verifyStored(beanClass, ordinalRestriction_, "ordinal restriction");

        // an identifier that the query manager expects to be provided can't
        // be assigned by an addition that doesn't ask for one
        var identifier_constraint = constraints_.get(identifier_);
        if (identifier_constraint != null && identifier_constraint.isSparse()) {
            throw new SparseIdentifierException(beanClass, identifier_);
        }

        verifyNotNullable(beanClass, ordinal_, "ordinal");
        verifyNotNullable(beanClass, ordinalRestriction_, "ordinal restriction");
    }

    // refuses a property that orders the instances while it can be null,
    // since the database keeps such a row out of the comparisons that decide
    // where it sits
    private void verifyNotNullable(Class<T> beanClass, String property, String role) {
        if (null == property) {
            return;
        }
        var type = propertyType(beanClass, property);
        if (type != null && type.isPrimitive()) {
            return;
        }
        var constraint = constraints_.get(property);
        if (constraint != null && constraint.isNotNull()) {
            return;
        }

        throw new NullableOrderingPropertyException(beanClass, property, role);
    }

    // refuses a property that this administration works with as a column
    // while the database never sees it
    private void verifyStored(Class<T> beanClass, String property, String role) {
        if (null == property) {
            return;
        }
        if (isPersisted(property) && isSaved(property)) {
            return;
        }
        throw new UnstoredPropertyException(beanClass, property, role);
    }

    // checks whether the query manager keeps a property in the database,
    // decided the same way the query manager decides it itself rather than
    // by looking at a constraint or two
    private boolean isPersisted(String property) {
        return persisted_.contains(property);
    }

    private boolean isSaved(String property) {
        return saved_.contains(property);
    }

    private void verifyType(Class<T> beanClass, String property, String role, List<Class<?>> accepted, String description) {
        if (null == property) {
            return;
        }
        var type = propertyType(beanClass, property);
        if (null == type) {
            throw new UnknownPropertyException(beanClass, property, role);
        }
        if (accepted.contains(type)) {
            return;
        }
        throw new InvalidPropertyTypeException(beanClass, property, role, type, description);
    }

    private static Class<?> propertyType(Class<?> beanClass, String property) {
        try {
            return BeanUtils.getPropertyType(beanClass, property);
        } catch (BeanUtilsException e) {
            // a property that isn't there has no type, and the roles are
            // verified against that rather than this being a failure
            return null;
        }
    }

    private static String defaultSlug(Class<?> beanClass) {
        var name = beanClass.getSimpleName();
        return name.substring(0, 1).toLowerCase(Locale.ROOT) + name.substring(1);
    }

    // checks whether a value can be used as a part of a URL without having
    // to be escaped or changing what a route matches
    static boolean isValidUrlPart(String value) {
        return value != null && !value.isEmpty() && value.matches("[a-zA-Z0-9_-]+");
    }

    private static List<String> propertyNames(Class<?> beanClass) {
        try {
            return new ArrayList<>(BeanUtils.getPropertyNames(beanClass, null, null, null));
        } catch (BeanUtilsException e) {
            throw new BeanIntrospectionException(beanClass, e);
        }
    }

    private List<String> listedProperties(List<String> properties) {
        var any_listed = constraints_.values().stream().anyMatch(ConstrainedProperty::isListed);

        var result = new ArrayList<String>();
        for (var property : properties) {
            var constraint = constraints_.get(property);
            if (any_listed) {
                // a column that was asked for is shown whatever backs its
                // value, a computed one reads just as well as a stored one
                if (constraint != null && constraint.isListed()) {
                    result.add(property);
                }
            } else if (isPersisted(property) && !property.equals(ordinal_) && isDisplayable(property)) {
                // a column that was never asked for only shows up when it
                // has something to show, since the content of a file or the
                // instances of a relationship don't fit in a table cell
                result.add(property);
            }
        }

        // the properties with an explicit position come first, in that order
        result.sort((a, b) -> Integer.compare(position(a), position(b)));
        return result;
    }

    // checks whether a property can be shown in a table and entered through
    // a form: the content of a file, the instances that a relationship
    // points to and the values of a collection all need more than the text,
    // the checkbox and the select that are generated
    private boolean isDisplayable(String property) {
        return null == undisplayable(property);
    }

    // says why a property can't be shown or entered, or null when it can
    private String undisplayable(String property) {
        var constraint = constraints_.get(property);
        // a relationship that points at one other instance is stored as the
        // identifier of it, which shows and enters like the number that it
        // is, while the ones that point at several need a control that picks
        // them and would fill a table cell with all of them
        if (constraint != null) {
            if (constraint.isFile()) {
                return "the content of a file needs a field that uploads one";
            }
            if (constraint.hasManyToOneAssociation()) {
                return "a relationship to one instance needs a control that picks it";
            }
            if (constraint.hasManyToMany()) {
                return "a relationship to several instances needs a control that picks them";
            }
        }

        var type = propertyType(beanClass_, property);
        if (null == type) {
            return null;
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type) || type.isArray()) {
            return "a property that holds several values needs a control that enters them";
        }

        return null;
    }

    // refuses an entity that can't be added, which is one whose form leaves
    // out a property that has to be provided
    private void verifyAddable(Class<T> beanClass, List<String> properties, Validated pristine) {
        // an addition leaves out what is wrong with the identifier, which it
        // can only do by the name that those errors carry, so no other
        // property is allowed to report under that same name
        var identifier_subject = subjectOfProperty(identifier_);
        for (var property : properties) {
            if (!property.equals(identifier_) &&
                identifier_subject.equals(subjectOfProperty(property))) {
                throw new DuplicateSubjectException(beanClass, identifier_subject, identifier_, property);
            }
        }

        // the validation of an addition runs the callbacks of the bean
        // first, and one of those is allowed to fill in whatever a new
        // instance doesn't carry, so a bean that has them is left to them
        if (Callbacks.class.isAssignableFrom(beanClass) ||
            CallbacksProvider.class.isAssignableFrom(beanClass)) {
            return;
        }

        // a default of the constraints is only rendered into a field that is
        // shown, so a property that isn't shown only has what a new instance
        // was constructed with
        // only the constraints of the property itself are consulted, since
        // the rules of a bean are allowed to judge a property by what
        // another one holds, which a form fills in
        // an addition marks the instance as one that doesn't exist yet
        // before it validates it, so the identifier is held to its own
        // constraints while it carries that
        try {
            BeanUtils.setPropertyValue(pristine, identifier_, NEW_IDENTIFIER);
        } catch (Exception e) {
            // an identifier that can't be assigned is reported by the
            // addition that needs to
        }

        for (var property : properties) {
            // whatever the reason that a property isn't part of the form,
            // one that has to be provided makes every addition fail on a
            // field that isn't there to fill in
            // the ordinal is handed out before an addition is validated and
            // the identifier is the database's to assign, so what a new
            // instance carries for either of them says nothing
            if (property.equals(identifier_) ||
                property.equals(ordinal_) ||
                addable_.contains(property)) {
                continue;
            }
            var constraint = constraints_.get(property);
            if (null == constraint) {
                continue;
            }

            // an addition is held to the rules that a constraint generates,
            // so those rules decide whether it can hold what a new instance
            // carries, instead of this deciding that for itself
            for (var rule : pristine.generateConstrainedPropertyRules(constraint)) {
                // a rule that judges this property by what another one holds
                // says nothing yet when the form still fills that one in,
                // while a property that nothing fills in is stuck with what a
                // new instance gave it, the ordinal that is handed out
                // included
                if (rule instanceof ValidationRuleSameAs &&
                    addable_.contains(constraint.getSameAs())) {
                    continue;
                }
                if (!rule.setBean(pristine).validate()) {
                    throw new MandatoryPropertyWithoutFieldException(beanClass, property);
                }
            }
        }
    }

    /**
     * Retrieves the properties that this administration has nothing to show
     * or to enter them with, together with the reason of each.
     * <p>They're left out of the tables and the forms, so this is what says
     * that a property is missing on purpose rather than by accident.
     *
     * @return the properties that aren't shown, by the reason that they
     * aren't
     * @since 1.10
     */
    public Map<String, String> getUndisplayableProperties() {
        return undisplayable_;
    }

    private int position(String property) {
        var constraint = constraints_.get(property);
        if (constraint == null || !constraint.hasPosition()) {
            return Integer.MAX_VALUE;
        }
        return constraint.getPosition();
    }

    private List<String> formProperties(List<String> properties, boolean adding) {
        var result = new ArrayList<String>();
        for (var property : properties) {
            // the identifier comes from the URL and the ordinal is moved
            // through its own operation
            if (property.equals(identifier_) || property.equals(ordinal_)) {
                continue;
            }
            // an instance is placed in its list when it's created, while
            // changing that list afterwards would leave its ordinal behind
            // in the list that it came from
            if (!adding && property.equals(ordinalRestriction_)) {
                continue;
            }
            var constraint = constraints_.get(property);
            // a property that the database never sees can still be one that
            // the user types, like the confirmation of another one, so what
            // decides is whether it's kept out on purpose rather than
            // whether it ends up in a column
            if (!isSaved(property) && (constraint == null || !constraint.isSameAs())) {
                continue;
            }
            if (constraint != null && !constraint.isEditable()) {
                continue;
            }
            // an upload and a relationship are entered through fields that
            // aren't generated yet
            if (!isDisplayable(property)) {
                continue;
            }
            result.add(property);
        }
        result.sort((a, b) -> Integer.compare(position(a), position(b)));
        return result;
    }

    /**
     * Retrieves the bean class that is administered.
     *
     * @return the administered bean class
     * @since 1.10
     */
    public Class<T> getBeanClass() {
        return beanClass_;
    }

    /**
     * Retrieves the URL slug that the routes of this entity are mounted on.
     *
     * @return the URL slug of this entity
     * @since 1.10
     */
    public String getSlug() {
        return slug_;
    }

    /**
     * Retrieves the label that is displayed for this entity.
     *
     * @return the display label of this entity
     * @since 1.10
     */
    public String getLabel() {
        if (options_.label() != null) {
            return options_.label();
        }
        return StringUtils.capitalize(beanClass_.getSimpleName());
    }

    /**
     * Retrieves the label that is displayed for several instances of this
     * entity.
     * <p>This defaults to the singular label with an {@code s} appended to
     * it, which is what the pages that talk about more than one instance
     * use.
     *
     * @return the plural display label of this entity
     * @see #getLabel()
     * @since 1.10
     */
    public String getLabelPlural() {
        if (options_.labelPlural() != null) {
            return options_.labelPlural();
        }
        return getLabel() + "s";
    }

    /**
     * Retrieves the name of the property that identifies an instance.
     * <p>This defaults to {@code id} when no property has been constrained
     * as the identifier.
     *
     * @return the name of the identifier property
     * @since 1.10
     */
    public String getIdentifier() {
        return identifier_;
    }

    /**
     * Retrieves the properties that are shown as the columns of the browse
     * table, in the order in which they are displayed.
     * <p>When no property has been constrained as {@code listed}, every
     * persistent property is shown.
     *
     * @return the listed properties
     * @since 1.10
     */
    public List<String> getListedProperties() {
        return listed_;
    }

    /**
     * Retrieves the properties that a search of the browse page matches
     * against.
     * <p>These are the columns of the browse table that hold text the
     * database keeps, so what a search matches is always visible in the
     * table. When there are none, the browse page doesn't offer a search.
     *
     * @return the searchable properties
     * @since 1.10
     */
    public List<String> getSearchableProperties() {
        return searchable_;
    }

    /**
     * Indicates whether the browse table can be sorted by one of the
     * properties of this entity.
     * <p>That's the case for every column of the browse table whose value
     * the database keeps, since the database is what does the sorting.
     *
     * @param property the name of the property
     * @return {@code true} when the browse table can be sorted by it; or
     * <p>{@code false} when it can't
     * @since 1.10
     */
    public boolean isSortable(String property) {
        return listed_.contains(property) && isPersisted(property);
    }

    /**
     * Retrieves the properties that can be submitted when an instance of
     * this entity is edited, in the order in which they are displayed.
     * <p>The identifier and the ordinal are never part of a form, since the
     * first comes from the URL and the second is changed by moving an
     * instance, and neither is the property that the ordinal is restricted
     * to, since moving an instance to another list would leave its ordinal
     * behind in the list that it came from.
     * <p>A property with a {@code file} constraint isn't part of a form
     * either, as long as the forms of this administration don't generate the
     * file fields that such a property is entered through.
     *
     * @return the editable properties
     * @see #getAddableProperties()
     * @since 1.10
     */
    public List<String> getEditableProperties() {
        return editable_;
    }

    /**
     * Retrieves the properties that can be submitted when an instance of
     * this entity is added, in the order in which they are displayed.
     * <p>These are the editable properties, together with the property that
     * the ordinal is restricted to, since an instance is placed in its list
     * when it's created.
     *
     * @return the addable properties
     * @see #getEditableProperties()
     * @since 1.10
     */
    public List<String> getAddableProperties() {
        return addable_;
    }

    /**
     * Retrieves the name of the database column that one of the properties
     * of this entity is stored in.
     * <p>This is the name of the property itself, unless a
     * {@code columnName} constraint maps it to another one.
     *
     * @param property the name of the property
     * @return the name of the database column
     * @since 1.10
     */
    public String getColumnName(String property) {
        if (null == property) {
            return null;
        }
        var constraint = constraints_.get(property);
        if (constraint != null && constraint.hasColumnName()) {
            return constraint.getColumnName();
        }
        return property;
    }

    /**
     * Retrieves the name of the database column that the identifier of this
     * entity is stored in.
     *
     * @return the name of the identifier column
     * @see #getIdentifier()
     * @since 1.10
     */
    public String getIdentifierColumn() {
        return getColumnName(identifier_);
    }

    /**
     * Retrieves the name of the database column that the ordinal of this
     * entity is stored in.
     *
     * @return the name of the ordinal column; or
     * <p>{@code null} when this entity isn't ordered manually
     * @see #getOrdinal()
     * @since 1.10
     */
    public String getOrdinalColumn() {
        return getColumnName(ordinal_);
    }

    /**
     * Retrieves the name of the database column that the ordinal of this
     * entity is partitioned into lists by.
     *
     * @return the name of the ordinal restriction column; or
     * <p>{@code null} when the ordinal isn't restricted
     * @see #getOrdinalRestriction()
     * @since 1.10
     */
    public String getOrdinalRestrictionColumn() {
        return getColumnName(ordinalRestriction_);
    }

    /**
     * Creates an ordinal sequence for the instances of this entity.
     * <p>The sequence is set up with the database columns that the
     * identifier, the ordinal and its restriction are stored in, which is
     * what keeps a property that is mapped to another column name working.
     *
     * @param datasource the datasource that the instances are stored in
     * @return the ordinal sequence of this entity; or
     * <p>{@code null} when this entity isn't ordered manually
     * @see #isOrdered()
     * @since 1.10
     */
    public OrdinalSequence createOrdinalSequence(Datasource datasource) {
        if (null == ordinal_) {
            return null;
        }
        return new OrdinalSequence(datasource, createManager(datasource).getTable(),
            getIdentifierColumn(), getOrdinalColumn(), getOrdinalRestrictionColumn());
    }

    /**
     * Retrieves the name of the property that positions the instances
     * relative to each other.
     *
     * @return the name of the ordinal property; or
     * <p>{@code null} when this entity isn't ordered manually
     * @see #getOrdinalRestriction()
     * @since 1.10
     */
    public String getOrdinal() {
        return ordinal_;
    }

    /**
     * Retrieves the name of the property that the ordinal is restricted
     * to.
     * <p>When an entity is ordered within a parent, only the instances that
     * share the same value for this property are moved relative to each
     * other.
     *
     * @return the name of the ordinal restriction property; or
     * <p>{@code null} when the ordinal isn't restricted
     * @see #getOrdinal()
     * @since 1.10
     */
    public String getOrdinalRestriction() {
        return ordinalRestriction_;
    }

    /**
     * Indicates whether the instances of this entity can be moved relative
     * to each other.
     *
     * @return {@code true} when this entity has an ordinal property; or
     * <p>{@code false} when it hasn't
     * @see #getOrdinal()
     * @since 1.10
     */
    public boolean isOrdered() {
        return ordinal_ != null;
    }

    /**
     * Retrieves the label that is displayed for one of the properties of
     * this entity.
     * <p>The name of a constrained property is used when it has been
     * provided, otherwise the property name is split up into words.
     *
     * @param property the name of the property
     * @return the display label of the property
     * @since 1.10
     */
    public String getPropertyLabel(String property) {
        var constraint = constraints_.get(property);
        if (constraint != null && constraint.getName() != null) {
            return constraint.getName();
        }
        // an error can name a subject that no property answers to, which is
        // still what has to be reported about
        if (null == property || property.isEmpty()) {
            return "";
        }
        var spaced = property.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
        return spaced.substring(0, 1).toUpperCase(Locale.ROOT) + spaced.substring(1);
    }

    /**
     * Retrieves the name that something which is wrong with a property is
     * reported under.
     * <p>That's the name of the property unless its constraints were given a
     * subject of their own.
     *
     * @param property the name of the property
     * @return the subject that its errors carry
     * @since 1.10
     */
    public String subjectOfProperty(String property) {
        var constraint = constraints_.get(property);
        return constraint == null ? property : constraint.getSubjectName();
    }

    /**
     * Retrieves the property that something which is wrong with an instance
     * belongs to.
     * <p>What a validation error names is the subject of a constraint, which
     * is the name of the property unless that constraint was given a subject
     * of its own.
     *
     * @param subject the subject that an error names
     * @return the name of the property that it belongs to; or
     * <p>the subject itself when no property claims it
     * @since 1.10
     */
    public String propertyOfSubject(String subject) {
        if (null == subject) {
            return null;
        }
        var property = subjects_.get(subject);
        return property == null ? subject : property;
    }

    /**
     * Retrieves the constraint of one of the properties of this entity.
     *
     * <p>A copy is handed out, since changing a constraint afterwards would
     * describe an entity that differs from the one whose properties and
     * columns were derived when it was registered. The values of a constraint
     * are copied along with it as far as they can be, which is as far as a
     * value says how to copy itself: one that isn't cloneable is shared with
     * the constraint that this entity keeps, so treat what you get back as
     * something to read rather than something to change.
     *
     * @param property the name of the property
     * @return the constrained property; or
     * <p>{@code null} when the property isn't constrained
     * @since 1.10
     */
    public ConstrainedProperty getConstraint(String property) {
        var constraint = constraints_.get(property);
        return constraint == null ? null : constraint.clone();
    }

    /**
     * Retrieves the options that this entity was registered with.
     *
     * @return the options of this entity
     * @since 1.10
     */
    public CrudEntityOptions<T> getOptions() {
        return options_;
    }

    /**
     * Creates a query manager for the instances of this entity.
     *
     * @param datasource the datasource that the instances are stored in
     * @return the query manager of this entity
     * @since 1.10
     */
    public GenericQueryManager<T> createManager(Datasource datasource) {
        if (options_.table() != null) {
            return GenericQueryManagerFactory.instance(datasource, beanClass_, options_.table());
        }
        return GenericQueryManagerFactory.instance(datasource, beanClass_);
    }

    /**
     * Retrieves the identifier value of an instance of this entity.
     *
     * @param instance the instance to retrieve the identifier of
     * @return the identifier value
     * @since 1.10
     */
    public int identifierValue(T instance) {
        return numberValue(instance, identifier_).intValue();
    }

    /**
     * Retrieves the ordinal value of an instance of this entity.
     *
     * @param instance the instance to retrieve the ordinal of
     * @return the ordinal value; or
     * <p>{@code -1} when this entity isn't ordered manually
     * @see #getOrdinal()
     * @since 1.10
     */
    public int ordinalValue(T instance) {
        if (null == ordinal_) {
            return -1;
        }
        return numberValue(instance, ordinal_).intValue();
    }

    /**
     * Retrieves the value that names the list an instance belongs to.
     *
     * @param instance the instance to retrieve the restriction of
     * @return the ordinal restriction value; or
     * <p>{@code -1} when the ordinal isn't restricted
     * @see #getOrdinalRestriction()
     * @since 1.10
     */
    public long ordinalRestrictionValue(T instance) {
        if (null == ordinalRestriction_) {
            return -1;
        }
        return numberValue(instance, ordinalRestriction_).longValue();
    }

    // provides the constraint to format a value with, copied when it
    // carries a format, since a format keeps state while it works and
    // requests are handled at the same time
    private ConstrainedProperty formattingConstraint(String property) {
        var constraint = constraints_.get(property);
        if (constraint != null && constraint.getFormat() != null) {
            return constraint.clone();
        }
        return constraint;
    }

    private Number numberValue(T instance, String property) {
        try {
            var value = BeanUtils.getPropertyValue(instance, property);
            if (null == value) {
                return -1;
            }
            return (Number) value;
        } catch (BeanUtilsException e) {
            throw new PropertyAccessException(beanClass_, property, e);
        }
    }

    /**
     * Formats the value of one of the properties of an instance for display.
     *
     * @param instance the instance to format a property value of
     * @param property the name of the property
     * @return the formatted value
     * @since 1.10
     */
    public String formattedValue(T instance, String property) {
        try {
            var value = BeanUtils.getPropertyValue(instance, property);
            if (value == null) {
                return "";
            }
            return BeanUtils.formatPropertyValue(value, formattingConstraint(property));
        } catch (BeanUtilsException e) {
            // a value that can't be retrieved has to be reported instead of
            // being shown as empty, since an empty edit field would replace
            // what is actually stored
            throw new PropertyAccessException(beanClass_, property, e);
        }
    }

    // the saved properties that the edit form doesn't enter, which an
    // update still writes
    List<String> uneditedSavedProperties() {
        return uneditedSaved_;
    }

    // writes the value of a property the way its edit field submits it, or
    // null when the property holds nothing
    String inputValue(T instance, String property) {
        try {
            var value = BeanUtils.getPropertyValue(instance, property);
            if (null == value) {
                return null;
            }
            return BeanUtils.formatPropertyValueForInput(value, propertyType(beanClass_, property), getConstraint(property));
        } catch (BeanUtilsException e) {
            throw new PropertyAccessException(beanClass_, property, e);
        }
    }

    // condenses what the edit form of an instance enters into a single
    // digest, so that a submission can carry along what the form was
    // rendered from and an edit that would overwrite somebody else's can be
    // noticed; only the editable properties take part, since they are what
    // an edit writes
    String stateDigest(T instance) {
        java.security.MessageDigest digest;
        try {
            digest = java.security.MessageDigest.getInstance("SHA-256");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        for (var property : editable_) {
            digest.update(property.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            digest.update((byte) 0);
            var value = inputValue(instance, property);
            // a value that holds nothing is told apart from one that holds
            // empty text
            if (null == value) {
                digest.update((byte) 1);
            } else {
                digest.update(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            digest.update((byte) 0);
        }
        return StringUtils.encodeHexLower(digest.digest());
    }
}
