/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.engine.Context;

import java.util.Locale;
import rife.engine.RequestMethod;

/**
 * Performs a custom action on an instance of an entity.
 * <p>An action is only ever performed by a POST, so that looking at its URL
 * can't change anything. The button of an action that doesn't ask for a
 * confirmation submits it directly, while the others first show what they
 * are about to act on.
 * <p>The handler receives the full context, so that it can produce its own
 * response instead of returning to the browse page, which it does by
 * printing that response and interrupting the request with
 * {@link Context#respond()}.
 * <p>On an entity that is ordered manually, the handler runs while the list
 * the instance sat in is held, so that a handler which stores or deletes the
 * instance can't interleave with a move that is renumbering that list. That
 * list is read before it's held, so a move which takes the instance to
 * another list in between isn't kept apart from the handler, unlike the edit
 * and the move, which re-read the placement after they lock. Holding the
 * list also runs the handler inside a transaction: an exception takes back
 * what it did, while producing its own response commits it.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudAction
 * @since 1.10
 */
public class CrudPerform<T> extends CrudElement<T> {
    private final CrudAction<T> action_;

    CrudPerform(CrudAdmin admin, CrudEntity<T> entity, CrudAction<T> action) {
        super(admin, entity);
        action_ = action;
    }

    protected String pageKind() {
        return "action";
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
        // a forged request can't act on what the browse page refused
        if (!action_.isVisible(instance)) {
            flash(c, "This " + entity_.getLabel() + " can't be " + action_.getLabel().toLowerCase(Locale.ROOT) + ".");
            redirectToBrowse(c);
            return;
        }

        var routes = admin_.routes(entity_);
        var id = requestedIdentifier(c);

        // only a POST changes anything, so that a crawler or a preview that
        // merely looks at an action URL can't perform it
        if (c.method() == RequestMethod.POST) {
            String message;
            if (!entity_.isOrdered()) {
                message = action_.getHandler().perform(c, instance);
            } else {
                // the list comes from the stored row, so that the one that
                // is held is the one that the row actually sits in
                var placement = entity_.createOrdinalSequence(admin_.datasource()).storedPlacement(id);
                if (null == placement) {
                    missingInstance(c);
                    return;
                }
                message = performHeld(c, instance, placement.restriction());
            }
            // a handler that produced its own response interrupts the flow
            // itself, so reaching this point means going back to the list
            if (message != null) {
                flash(c, message);
            }
            redirectToBrowse(c);
            return;
        }

        var t = page(c);
        t.setValueEncoded(ENTITY_LABEL_VALUE, entity_.getLabel());
        t.setValueEncoded("crud:actionLabel", action_.getLabel());
        var state = browseState(c);
        t.setValueEncoded(FORM_URL_VALUE, c.urlFor(routes.actions.get(action_.getName())).pathInfo(String.valueOf(id)).params(state));
        t.setValueEncoded(CANCEL_URL_VALUE, c.urlFor(routes.browse).params(state));

        for (var property : entity_.getListedProperties()) {
            t.setValueEncoded("crud:detailLabel", entity_.getPropertyLabel(property));
            t.setValueEncoded("crud:detailValue", entity_.formattedValue(instance, property));
            t.appendBlock("crud:details", "crud:detail");
        }

        t.setBlock(CONTENT_VALUE, "crud:confirm");
        c.print(t);
    }

    // performs the action while the list that the instance sits in is
    // held, so that a handler which stores or deletes the instance can't
    // interleave with a move that is renumbering the same list
    String performHeld(Context c, T instance, long restriction) {
        var held = new String[1];
        entity_.createOrdinalSequence(admin_.datasource()).inList(restriction, () -> held[0] = action_.getHandler().perform(c, instance));
        return held[0];
    }
}
