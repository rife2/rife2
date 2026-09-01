/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.database.exceptions.DatabaseException;
import rife.engine.Context;
import rife.engine.RequestMethod;

/**
 * Deletes an existing instance of an entity, after a confirmation.
 * <p>The instance is shown before it's deleted, so that you can see what
 * you're about to lose. A deletion that the database refuses because it
 * would break a constraint, like a record that still refers to this one,
 * is reported with a message instead of an exception page, while the
 * failures that aren't about that are reported like any other.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CrudDelete<T> extends CrudElement<T> {
    CrudDelete(CrudAdmin admin, CrudEntity<T> entity) {
        super(admin, entity);
    }

    protected String pageKind() {
        return "delete";
    }

    protected void perform(Context c) {
        var instance = restoreInstance(c);
        if (null == instance) {
            missingInstance(c);
            return;
        }

        // the instance is made the one that the URL asked for before
        // anything decides about it, since a callback of the restore can
        // have pointed it at another row
        forceRequestedIdentifier(instance, requestedIdentifier(c));

        // the same check that decides whether the button is shown, so that
        // a forged request can't delete what the browse page refused
        if (!entity_.getOptions().isDeletable(instance)) {
            flash(c, "This " + entity_.getLabel() + " can't be deleted.");
            redirectToBrowse(c);
            return;
        }

        var routes = admin_.routes(entity_);
        var id = requestedIdentifier(c);

        if (c.method() == RequestMethod.POST) {
            var refusal = checkHooks(c, "beforeDelete", entity_.getOptions().getBeforeDeleteHooks(), instance);
            if (refusal != null) {
                flash(c, refusal);
                redirectToBrowse(c);
                return;
            }
            try {
                if (delete(c, instance, id)) {
                    flash(c, entity_.getLabel() + " has been deleted.");
                } else if (entity_.createManager(admin_.datasource()).restore(id) == null) {
                    // it was already gone, which isn't the same as a
                    // callback of the bean having refused the deletion
                    missingInstance(c);
                    return;
                } else {
                    flash(c, "This " + entity_.getLabel() + " hasn't been deleted.");
                }
            } catch (DatabaseException e) {
                if (!isConstraintViolation(e)) {
                    throw e;
                }
                // the database refused it, which the drivers report the same
                // way for a reference that is still there, a uniqueness that
                // would be broken and a check that doesn't hold
                flash(c, "This " + entity_.getLabel() + " can't be deleted, since a constraint in the database doesn't allow it.");
            }
            redirectToBrowse(c);
            return;
        }

        var t = page(c);
        t.setValueEncoded(ENTITY_LABEL_VALUE, entity_.getLabel());
        var state = browseState(c);
        t.setValueEncoded(FORM_URL_VALUE, c.urlFor(routes.delete).pathInfo(String.valueOf(id)).params(state));
        t.setValueEncoded(CANCEL_URL_VALUE, c.urlFor(routes.browse).params(state));

        for (var property : entity_.getListedProperties()) {
            t.setValueEncoded("crud:detailLabel", entity_.getPropertyLabel(property));
            t.setValueEncoded("crud:detailValue", entity_.formattedValue(instance, property));
            t.appendBlock("crud:details", "crud:detail");
        }

        t.setBlock(CONTENT_VALUE, "crud:delete");
        c.print(t);
    }

    // deletes an instance, leaving the ones that it was ordered between in
    // the order that they were already in; the deletion and the hooks that
    // follow a successful one share a single transaction, so that a hook
    // which throws takes the deletion back
    boolean delete(Context c, T instance, int id) {
        var manager = entity_.createManager(admin_.datasource());
        var deleted = new boolean[]{false};
        if (!entity_.isOrdered()) {
            inTransaction(() -> {
                deleted[0] = manager.delete(id);
                if (deleted[0]) {
                    performHooks(c, "afterDelete", entity_.getOptions().getAfterDeleteHooks(), instance);
                }
            });
            return deleted[0];
        }

        // the gap that this leaves behind doesn't have to be closed, since
        // the instances are moved through the order that their ordinals put
        // them in rather than through the values themselves
        // the list comes from the stored row, so that the one that is
        // locked is the one that the row is actually being taken out of
        var sequence = entity_.createOrdinalSequence(admin_.datasource());
        var placement = sequence.storedPlacement(id);
        if (null == placement) {
            return false;
        }
        sequence.inList(placement.restriction(), () -> {
            deleted[0] = manager.delete(id);
            if (deleted[0]) {
                performHooks(c, "afterDelete", entity_.getOptions().getAfterDeleteHooks(), instance);
            }
        });
        return deleted[0];
    }

}
