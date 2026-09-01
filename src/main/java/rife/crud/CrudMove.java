/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.crud.exceptions.PlacementChangedException;
import rife.database.exceptions.DatabaseException;
import rife.engine.Context;

/**
 * Moves an instance of an entity up or down relative to the others.
 * <p>The instances are moved through the ordinal property that they have
 * been constrained with, and when that constraint restricts the ordinal to
 * another property, only the instances that share the same value for it are
 * moved relative to each other.
 * <p>The instance is named by its identifier throughout, so that a move
 * that was asked for while another one was still going still moves the
 * instance that the button belonged to instead of whatever took its place.
 * <p>The ordinals don't have to follow each other, since an instance is
 * exchanged with the one next to it in the order that the ordinals put them
 * in, whatever the distance between them is.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CrudMove<T> extends CrudElement<T> {
    CrudMove(CrudAdmin admin, CrudEntity<T> entity) {
        super(admin, entity);
    }

    protected String pageKind() {
        return "move";
    }

    protected void perform(Context c) {
        // the instance isn't restored here, since a move only works with
        // where the row sits, and that read also says whether it's there
        var upwards = "up".equals(c.parameter("crudDirection"));
        var id = requestedIdentifier(c);

        Outcome outcome;
        try {
            outcome = moveCurrent(c, id, upwards);
        } catch (DatabaseException e) {
            if (!isTransactionConflict(e)) {
                throw e;
            }
            // the database broke a deadlock between simultaneous moves by
            // refusing this one, which isn't an error to report but a move
            // to try again
            outcome = Outcome.CONFLICT;
        }

        switch (outcome) {
            case MISSING -> {
                missingInstance(c);
                return;
            }
            case CONFLICT -> flash(c, "This " + entity_.getLabel() + " was changed at the same time and hasn't been moved, try again.");
            case REFUSED -> flash(c, refusalMessage(c));
            case MOVED, UNCHANGED -> {
            }
        }

        redirectToBrowse(c);
    }

    // the move and the hooks that follow a successful one are performed
    // while the same list is held, so that a hook which throws takes the
    // move back
    Outcome moveCurrent(Context c, int id, boolean upwards) {
        // the list comes from the stored row, and that read also says
        // whether the row is there at all, so nothing has to be restored to
        // find out
        var sequence = entity_.createOrdinalSequence(admin_.datasource());
        var placement = sequence.storedPlacement(id);
        if (null == placement) {
            return Outcome.MISSING;
        }
        var restriction = placement.restriction();
        var outcome = new Outcome[]{Outcome.UNCHANGED};
        sequence.inList(restriction, () -> {
            // the row can have been moved to another list between reading
            // where it sat and holding that list, which leaves this move
            // holding a list that the row isn't in anymore
            var current = sequence.storedPlacement(id);
            if (null == current) {
                outcome[0] = Outcome.MISSING;
                return;
            }
            if (current.restriction() != restriction) {
                outcome[0] = Outcome.CONFLICT;
                return;
            }

            // the hooks only see a row that is still there and still in the
            // list that is being held, so a stale request doesn't run them
            // and can't be refused by one either
            var refusal = checkMoveHooks(c, entity_.getOptions().getBeforeMoveHooks(), id, upwards);
            if (refusal != null) {
                refused(c, refusal);
                outcome[0] = Outcome.REFUSED;
                return;
            }

            var moved = upwards ? sequence.moveUp(id, restriction) : sequence.moveDown(id, restriction);
            if (!moved) {
                // it sits at the end of its list
                return;
            }
            outcome[0] = Outcome.MOVED;

            var hooks = entity_.getOptions().getAfterMoveHooks();
            if (hooks.isEmpty()) {
                return;
            }

            var placed = sequence.storedPlacement(id);
            performMoveHooks(c, hooks, id, upwards);
            // a hook that stored the instance again can have moved it
            // somewhere that this move never put it, and the ordinals of
            // the list it went to were never locked for it
            var after = sequence.storedPlacement(id);
            if (null == after || after.restriction() != placed.restriction() || after.ordinal() != placed.ordinal()) {
                throw new PlacementChangedException(entity_.getBeanClass(), id, placed.restriction(), placed.ordinal());
            }
        });
        return outcome[0];
    }

    enum Outcome {
        MOVED,
        UNCHANGED,
        MISSING,
        REFUSED,
        CONFLICT
    }
}
