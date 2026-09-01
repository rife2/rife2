/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.crud.exceptions.*;
import rife.cmf.dam.OrdinalSequence;
import rife.database.exceptions.DatabaseException;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.engine.Context;
import rife.engine.RequestMethod;
import rife.tools.BeanUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.Validated;

import java.util.Locale;
import rife.validation.ValidationError;

import java.util.LinkedHashSet;

/**
 * Adds a new instance of an entity.
 * <p>The form is generated from the constraints of the bean, and a
 * submission that doesn't validate is shown again with the errors marked,
 * so that nothing is lost.
 * <p>An instance of an entity that is ordered manually is placed at the end
 * of its list, which relies on the ordinal sequence of that entity to hand
 * out the ordinal and to store it as one thing, so that no two additions
 * that happen at the same time are given the same one.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CrudAdd<T> extends CrudElement<T> {
    CrudAdd(CrudAdmin admin, CrudEntity<T> entity) {
        super(admin, entity);
    }

    protected String pageKind() {
        return "add";
    }

    protected void perform(Context c) {
        var manager = entity_.createManager(admin_.datasource());
        var routes = admin_.routes(entity_);

        T instance;
        if (c.method() == RequestMethod.POST) {
            instance = newInstance();
            // the validation is reset before the values arrive, since
            // filling in a value that can't be converted is what reports it
            resetValidation(instance);
            // only the properties that the form actually shows are filled in,
            // so that a forged request can't reach the ones that it hides
            c.parametersBean(instance, null, entity_.getAddableProperties());
            forceNewIdentifier(instance);

            // an ordered instance is validated with the ordinal that it's
            // actually going to be stored with, since a constraint on that
            // property would otherwise refuse a value that this assigns
            var outcome = entity_.isOrdered() ? saveOrdered(c, instance) : saveDirectly(c, manager, instance);
            if (outcome != Outcome.INVALID) {
                // a callback of the bean can refuse the addition, which
                // isn't the same as it having been stored
                flash(c, outcome == Outcome.ADDED
                    ? entity_.getLabel() + " has been added."
                    : "This " + entity_.getLabel() + " hasn't been added.");
                redirectToBrowse(c);
                return;
            }
        } else {
            instance = newInstance();
        }

        var t = page(c);
        t.setValueEncoded(ENTITY_LABEL_VALUE, entity_.getLabel());
        t.setValue(FORM_TITLE_VALUE, "Add " + escape(entity_.getLabel()));
        t.setValueEncoded(FORM_URL_VALUE, c.urlFor(routes.add));
        t.setValueEncoded(CANCEL_URL_VALUE, c.urlFor(routes.browse));
        t.setValue(SUBMIT_LABEL_VALUE, "Add");
        var form = new CrudFormBuilder(entity_, c.template("crud.fields"), true, c.method() == RequestMethod.POST ? c : null);
        t.setValue("errors", form.generateErrors(instance, refusalMessage(c)));
        t.setValue("crud:fields", form.generateFields(instance));
        t.setBlock(CONTENT_VALUE, "crud:form");
        c.print(t);
    }

    // validates an instance the way it's going to be stored; the identifier
    // that it carries says that it isn't stored yet, since the database
    // assigns the real one, so errors about the identifier can't be
    // corrected in the form and don't refuse the addition
    private boolean validate(T instance) {
        if (!(instance instanceof Validated validated)) {
            return true;
        }
        var manager = entity_.createManager(admin_.datasource());
        validated.validate(manager);

        var remaining = new LinkedHashSet<ValidationError>();
        for (var error : validated.getValidationErrors()) {
            if (!entity_.subjectOfProperty(entity_.getIdentifier()).equals(error.getSubject())) {
                remaining.add(error);
            }
        }
        validated.replaceValidationErrors(remaining);
        return remaining.isEmpty();
    }

    private T newInstance() {
        try {
            return entity_.getBeanClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new InstanceCreationException(entity_.getBeanClass(), e);
        }
    }

    // marks the instance as one that doesn't exist yet; an identifier that
    // can't be assigned is reported, since an addition that keeps whatever
    // the bean happens to hold would be stored as that instance instead of
    // as a new one
    private void forceNewIdentifier(T instance) {
        try {
            BeanUtils.setPropertyValue(instance, entity_.getIdentifier(), CrudEntity.NEW_IDENTIFIER);
        } catch (BeanUtilsException e) {
            throw new PropertyAccessException(entity_.getBeanClass(), entity_.getIdentifier(), e);
        }
    }

    Outcome saveOrdered(T instance) {
        return saveOrdered(null, instance);
    }

    // saves an instance together with the ordinal that places it at the end
    // of its list; the ordinal is obtained and stored inside a single
    // transaction that holds the whole list, so the instance is only ever
    // inserted once and no two additions can get the same ordinal
    // a list that holds nothing yet has no rows to lock, which the sequence
    // covers itself within this application; an addition made at the same
    // moment from another application sharing the database can still obtain
    // the same ordinal, and the one that the database refuses is reported as
    // something to submit again rather than being retried here, since a
    // callback of the bean is allowed to do work elsewhere and would do it
    // twice
    Outcome saveOrdered(Context c, T instance) {
        var sequence = entity_.createOrdinalSequence(admin_.datasource());
        var restriction = entity_.ordinalRestrictionValue(instance);
        var outcome = new Outcome[]{Outcome.INVALID};

        try {
            sequence.inList(restriction, () -> {
                var ordinal = sequence.nextOrdinal(restriction);
                assignOrdinal(instance, ordinal);
                if (!validate(instance)) {
                    return;
                }
                var refusal = checkHooks(c, "beforeAdd", entity_.getOptions().getBeforeAddHooks(), instance);
                if (refusal != null) {
                    refused(c, refusal);
                    return;
                }

                var stored_id = entity_.createManager(admin_.datasource()).insert(instance);
                outcome[0] = stored_id != -1 ? Outcome.ADDED : Outcome.REFUSED;
                if (outcome[0] != Outcome.ADDED) {
                    return;
                }

                // the columns are read straight from the table inside the
                // transaction that wrote them, so that a callback which
                // moved the row out of the locked list and put the values
                // back on the instance afterwards is still noticed
                verifyPlacement(sequence, stored_id, restriction, ordinal);

                // a callback of the insert can have changed the identifier,
                // the ordinal or the list on the instance, which would hand
                // the hooks an instance that doesn't describe what was
                // stored
                forceStoredPlacement(instance, stored_id, restriction, ordinal);

                var hooks = entity_.getOptions().getAfterAddHooks();
                if (!hooks.isEmpty()) {
                    performHooks(c, "afterAdd", hooks, instance);
                    // a hook that stored the instance again can have moved it
                    // out of the list that is being held, and the ordinals
                    // of the list it went to were never locked for it
                    verifyPlacement(sequence, stored_id, restriction, ordinal);
                }
            });
        } catch (DatabaseException e) {
            if (!isConstraintViolation(e)) {
                throw e;
            }
            refused(c, "The database didn't accept this " + entity_.getLabel().toLowerCase(Locale.ROOT) +
                       ", a value of it is already used or isn't allowed, submit it again.");
            return Outcome.INVALID;
        }
        return outcome[0];
    }

    private void verifyPlacement(OrdinalSequence sequence, int id, long restriction, int ordinal) {
        if (!sequence.isPlacedAt(id, restriction, ordinal)) {
            throw new PlacementChangedException(entity_.getBeanClass(), id, restriction, ordinal);
        }
    }

    private Outcome saveDirectly(Context c, GenericQueryManager<T> manager, T instance) {
        if (!validate(instance)) {
            return Outcome.INVALID;
        }
        var refusal = checkHooks(c, "beforeAdd", entity_.getOptions().getBeforeAddHooks(), instance);
        if (refusal != null) {
            refused(c, refusal);
            return Outcome.INVALID;
        }
        return insert(c, manager, instance);
    }

    // stores an instance, reporting a value that another request stored
    // first as something to correct rather than as a failure; the insert and
    // the hooks that follow a successful one share a single transaction, so
    // that a hook which throws takes the addition back
    private Outcome insert(Context c, GenericQueryManager<T> manager, T instance) {
        var outcome = new Outcome[]{Outcome.INVALID};
        try {
            inTransaction(() -> {
                var stored_id = manager.insert(instance);
                outcome[0] = stored_id != -1 ? Outcome.ADDED : Outcome.REFUSED;
                if (outcome[0] == Outcome.ADDED) {
                    forceStoredPlacement(instance, stored_id, -1, -1);
                    performHooks(c, "afterAdd", entity_.getOptions().getAfterAddHooks(), instance);
                }
            });
            return outcome[0];
        } catch (DatabaseException e) {
            if (!isConstraintViolation(e)) {
                throw e;
            }
            // which constraint the database refused isn't something that it
            // says in a way that points at a property, so the form is told
            // as a whole rather than a property being blamed for it
            refused(c, "The database didn't accept this " + entity_.getLabel().toLowerCase(Locale.ROOT) +
                       ", a value of it is already used or isn't allowed.");
            return Outcome.INVALID;
        }
    }

    enum Outcome {
        ADDED,
        REFUSED,
        INVALID
    }

    private void assignOrdinal(T instance, int ordinal) {
        try {
            BeanUtils.setPropertyValue(instance, entity_.getOrdinal(), ordinal);
        } catch (BeanUtilsException e) {
            throw new PropertyAccessException(entity_.getBeanClass(), entity_.getOrdinal(), e);
        }
    }

    static String escape(String value) {
        return StringUtils.encodeHtml(value);
    }
}
