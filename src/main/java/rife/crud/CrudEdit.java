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
import rife.tools.Convert;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.Validated;

import java.util.Locale;
import java.util.Map;
import rife.validation.ValidationError;

/**
 * Edits an existing instance of an entity.
 * <p>The instance is restored first and then filled with the values that
 * were submitted. Only the editable properties are filled in, and the
 * identifier is always taken from the URL, which means that a submitted
 * identifier can't retarget the update and that a submitted ordinal can't
 * reorder anything. The saved properties that aren't part of the form are
 * written as the row holds them at the moment of the update, read inside
 * its own transaction, so an edit never stores a stale value for
 * something it didn't ask about.
 * <p>Since only the properties of the form are filled in, an unchecked
 * checkbox is stored as {@code false} instead of keeping the value that it
 * had, which browsers don't send at all.
 * <p>The identifier, the ordinal and the list are read from the database
 * and put back on the instance before it's stored, and what was stored is
 * read back afterwards, so that a callback which changes one of them is
 * reported instead of quietly editing another row.
 * <p>The form carries a digest of the values it was rendered from, so that
 * a submission which was written on top of something that isn't stored
 * anymore is refused instead of overwriting what somebody else saved in the
 * meantime. The form comes back with what was typed kept in its fields,
 * each field that differs from what is stored now saying so underneath, and
 * a fresh digest, so that submitting again overwrites knowingly. The digest
 * is checked once more inside the transaction of the update. For an entity
 * that is ordered manually this closes the window between the check and the
 * write, since the update already holds the whole list while it runs; for
 * one that isn't, the read that this recheck makes takes no lock that
 * survives to the write, so it only narrows the window rather than closing
 * it, and two submissions that both read before either commits can still
 * let the second overwrite the first. A stored version column is what would
 * close that last gap.
 *
 * @see CrudAdmin
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CrudEdit<T> extends CrudElement<T> {
    CrudEdit(CrudAdmin admin, CrudEntity<T> entity) {
        super(admin, entity);
    }

    protected String pageKind() {
        return "edit";
    }

    protected void perform(Context c) {
        var instance = restoreInstance(c);
        if (null == instance) {
            missingInstance(c);
            return;
        }

        var manager = entity_.createManager(admin_.datasource());
        var routes = admin_.routes(entity_);
        var id = requestedIdentifier(c);

        // the digest of what is stored right now rides along in the form,
        // so that a submission says what it was written on top of
        var baseline = entity_.stateDigest(instance);
        Map<String, String> conflicts = null;

        if (c.method() == RequestMethod.POST) {
            var submitted_baseline = c.parameter("crudBaseline");
            // the validation is reset before the values arrive, since
            // filling in a value that can't be converted is what reports it
            resetValidation(instance);
            // only the properties that the form actually shows are filled in,
            // so that a forged request can't reach the ones that it hides
            c.parametersBean(instance, null, entity_.getEditableProperties());
            forceIdentifier(instance, id);

            // a submission that was written on top of something that isn't
            // stored anymore would overwrite what somebody else saved in the
            // meantime, without either of them being told
            if (submitted_baseline != null && !submitted_baseline.equals(baseline)) {
                var stored = manager.restore(id);
                if (null == stored) {
                    missingInstance(c);
                    return;
                }
                conflicts = conflictHints(stored, instance);
                refused(c, conflictMessage());
            } else {
                // the list and the ordinal that this edit is held to are read
                // straight from the table, since a callback of the restore can
                // have given the instance different ones on the way out of the
                // database, and they're put back on the instance so that the
                // update stores what was actually there
                var sequence = entity_.isOrdered() ? entity_.createOrdinalSequence(admin_.datasource()) : null;
                var restriction = -1L;
                var ordinal = -1;
                if (sequence != null) {
                    var placement = sequence.storedPlacement(id);
                    if (null == placement) {
                        missingInstance(c);
                        return;
                    }
                    ordinal = placement.ordinal();
                    restriction = placement.restriction();
                    forceProperty(instance, entity_.getOrdinal(), ordinal);
                    if (entity_.getOrdinalRestriction() != null) {
                        forceProperty(instance, entity_.getOrdinalRestriction(), restriction);
                    }
                }

                if (validate(instance, manager)) {
                    var refusal = checkHooks(c, "beforeUpdate", entity_.getOptions().getBeforeUpdateHooks(), instance);
                    if (refusal != null) {
                        refused(c, refusal);
                    } else {
                        var outcome = update(c, manager, instance, id, restriction, ordinal, submitted_baseline);
                        if (outcome == Outcome.CONFLICT) {
                            // somebody saved between the check above and the
                            // transaction of the update, so what is stored
                            // now is read again for the form to show
                            var stored = manager.restore(id);
                            if (null == stored) {
                                missingInstance(c);
                                return;
                            }
                            baseline = entity_.stateDigest(stored);
                            conflicts = conflictHints(stored, instance);
                            refused(c, conflictMessage());
                        } else if (outcome != Outcome.INVALID) {
                            switch (outcome) {
                                case MISSING -> missingInstance(c);
                                case UPDATED -> {
                                    flash(c, entity_.getLabel() + " has been updated.");
                                    redirectToBrowse(c);
                                }
                                case REFUSED -> {
                                    flash(c, "This " + entity_.getLabel() + " hasn't been updated.");
                                    redirectToBrowse(c);
                                }
                                case MOVED_AWAY -> {
                                    flash(c, "This " + entity_.getLabel() + " was moved at the same time and hasn't been updated, try again.");
                                    redirectToBrowse(c);
                                }
                                default -> {
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }

        var t = page(c);
        t.setValueEncoded(ENTITY_LABEL_VALUE, entity_.getLabel());
        t.setValue(FORM_TITLE_VALUE, "Edit " + CrudAdd.escape(entity_.getLabel()));
        // the form posts to a URL that keeps the browse state, so that a
        // successful submission still lands back on the page it came from
        var state = browseState(c);
        t.setValueEncoded(FORM_URL_VALUE, c.urlFor(routes.edit).pathInfo(String.valueOf(id)).params(state));
        t.setValueEncoded(CANCEL_URL_VALUE, c.urlFor(routes.browse).params(state));
        t.setValue(SUBMIT_LABEL_VALUE, "Save");
        t.setValueEncoded("crud:baselineDigest", baseline);
        t.setBlock("crud:baseline", "crud:baseline:present");
        var form = new CrudFormBuilder(entity_, c.template("crud.fields"), false, c.method() == RequestMethod.POST ? c : null);
        if (conflicts != null) {
            form.conflicts(conflicts);
        }
        t.setValue("errors", form.generateErrors(instance, refusalMessage(c)));
        t.setValue("crud:fields", form.generateFields(instance));
        t.setBlock(CONTENT_VALUE, "crud:form");
        c.print(t);
    }

    // updates an instance, holding the whole list that it sits in when the
    // entity is ordered; an update doesn't offer the ordinal or the list,
    // and holding the list makes it possible to notice a callback that
    // changes them anyway, which would move the instance somewhere that
    // nothing was keeping the ordinals apart
    private Outcome update(Context c, GenericQueryManager<T> manager, T instance, int id, long restriction, int ordinal, String baseline) {
        try {
            return updateWithin(c, manager, instance, id, restriction, ordinal, baseline);
        } catch (DatabaseException e) {
            if (!isConstraintViolation(e)) {
                throw e;
            }
            // the uniqueness was still free when it was validated, so the
            // form comes back with what has become wrong in the meantime
            refused(c, "The database didn't accept this " + entity_.getLabel().toLowerCase(Locale.ROOT) +
                       ", a value of it is already used or isn't allowed.");
            return Outcome.INVALID;
        }
    }

    private Outcome updateWithin(Context c, GenericQueryManager<T> manager, T instance, int id, long restriction, int ordinal, String baseline) {
        if (!entity_.isOrdered()) {
            // the identifier is checked after the update, so the update has
            // to be something that can still be taken back
            var outcome = new Outcome[]{Outcome.INVALID};
            inTransaction(() -> {
                outcome[0] = updateOnce(c, manager, instance, id, baseline);
                if (outcome[0] == Outcome.UPDATED) {
                    // a callback of the update can have put another identifier
                    // on the instance, which would hand the hooks an instance
                    // that names a different row than the one that was written
                    forceIdentifier(instance, id);
                    performHooks(c, "afterUpdate", entity_.getOptions().getAfterUpdateHooks(), instance);
                }
            });
            return outcome[0];
        }

        var sequence = entity_.createOrdinalSequence(admin_.datasource());
        var outcome = new Outcome[]{Outcome.INVALID};
        sequence.inList(restriction, () -> {
            // the row can have been moved or removed between reading where
            // it sat and locking that list, which would update the row
            // while something else is ordering the list that it went to
            var current = sequence.storedPlacement(id);
            if (null == current) {
                outcome[0] = Outcome.MISSING;
                return;
            }
            if (current.restriction() != restriction || current.ordinal() != ordinal) {
                outcome[0] = Outcome.MOVED_AWAY;
                return;
            }
            outcome[0] = updateOnce(c, manager, instance, id, baseline);
            if (outcome[0] != Outcome.UPDATED) {
                return;
            }

            // the columns are read straight from the table inside the
            // transaction that wrote them, so that a callback which changed
            // the list or the ordinal and put them back on the instance
            // afterwards is still noticed
            verifyPlacement(sequence, id, restriction, ordinal);

            var hooks = entity_.getOptions().getAfterUpdateHooks();
            if (!hooks.isEmpty()) {
                forceStoredPlacement(instance, id, restriction, ordinal);
                performHooks(c, "afterUpdate", hooks, instance);
                // a hook that stored the instance again can have moved it
                // out of the list that is being held, which the ordinals of
                // the list it went to aren't protected against
                verifyPlacement(sequence, id, restriction, ordinal);
            }
        });
        return outcome[0];
    }

    private void verifyPlacement(OrdinalSequence sequence, int id, long restriction, int ordinal) {
        if (!sequence.isPlacedAt(id, restriction, ordinal)) {
            throw new PlacementChangedException(entity_.getBeanClass(), id, restriction, ordinal);
        }
    }

    private Outcome updateOnce(Context c, GenericQueryManager<T> manager, T instance, int id, String baseline) {
        // the row is read again inside this transaction: the digest is
        // judged against it, and the columns that the form doesn't enter
        // are carried over from it
        var current = manager.restore(id);
        if (null == current) {
            return Outcome.MISSING;
        }
        // the form's own check runs before anything else, so this one only
        // catches a save that landed between that check and this transaction
        if (baseline != null && !entity_.stateDigest(current).equals(baseline)) {
            return Outcome.CONFLICT;
        }

        // an update writes every saved column while the form only entered
        // the editable ones, so what it never asked about is written as the
        // row holds it right now, the way the identifier and the ordinal
        // already are, instead of as this request found it
        for (var property : entity_.uneditedSavedProperties()) {
            try {
                BeanUtils.setPropertyValue(instance, property, BeanUtils.getPropertyValue(current, property));
            } catch (BeanUtilsException e) {
                throw new PropertyAccessException(entity_.getBeanClass(), property, e);
            }
        }

        // the identifier is put back right before the update, since a
        // callback of the validation can have pointed the instance at
        // another row in the meantime
        forceIdentifier(instance, id);

        // an update instead of a save, so that an instance that was deleted
        // in the meantime is reported as gone instead of being stored again
        // as a new one, and a constraint that the database refuses isn't
        // caught here so that the transaction around this takes back what
        // the callbacks did instead of committing it with nothing stored
        var updated = manager.update(instance);
        if (updated != -1) {
            // the identifier that the manager worked with says which row was
            // written, while the one on the instance is whatever a callback
            // left behind afterwards
            if (updated != id) {
                throw new CallbackChangedIdentifierException(entity_.getBeanClass(), id, updated);
            }
            return Outcome.UPDATED;
        }
        // nothing was updated because the instance is gone, or because a
        // callback of the bean refused the update
        return manager.restore(id) == null ? Outcome.MISSING : Outcome.REFUSED;
    }

    private enum Outcome {
        UPDATED,
        MOVED_AWAY,
        REFUSED,
        MISSING,
        INVALID,
        CONFLICT
    }

    // collects, for every field whose stored value differs from what saving
    // would write, what is stored right now, so that the editor can
    // reconcile knowingly: every hint says exactly what saving again would
    // overwrite, their own edits included
    private Map<String, String> conflictHints(T stored, T filled) {
        var hints = new java.util.LinkedHashMap<String, String>();
        for (var property : entity_.getEditableProperties()) {
            var now = entity_.inputValue(stored, property);
            var submitted = entity_.inputValue(filled, property);
            if (!java.util.Objects.equals(now, submitted)) {
                hints.put(property, now == null ? "" : now);
            }
        }
        return hints;
    }

    private String conflictMessage() {
        return "This " + entity_.getLabel() + " was changed while you were editing it. " +
               "Each field that differs shows what is stored now, and saving again overwrites it.";
    }

    private boolean validate(T instance, GenericQueryManager<T> manager) {
        if (!(instance instanceof Validated validated)) {
            return true;
        }
        return validated.validate(manager);
    }

    private void forceIdentifier(T instance, int id) {
        forceProperty(instance, entity_.getIdentifier(), id);
    }

}
