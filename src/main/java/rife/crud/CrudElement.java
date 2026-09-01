/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.authentication.credentialsmanagers.RoleUserIdentity;
import rife.crud.exceptions.*;
import rife.database.DbQueryManager;
import rife.database.TransactionUserWithoutResult;
import rife.engine.Context;
import rife.engine.Element;
import rife.template.Template;
import rife.tools.BeanUtils;
import rife.tools.Convert;
import rife.tools.exceptions.ControlFlowRuntimeException;
import rife.validation.Validated;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides what the operations of an administration have in common.
 * <p>Every operation renders its content into the same page shell, so that
 * the navigation, the flash messages and the styling are identical
 * throughout the administration.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public abstract class CrudElement<T> implements Element {
    static final String FLASH_ATTRIBUTE = "rife.crud.flash";
    static final String REFUSAL_ATTRIBUTE = "rife.crud.refusal";
    static final String IDENTITY_ATTRIBUTE = "rife.crud.identity";
    static final String FLASH_SHOWN_ATTRIBUTE = "rife.crud.flash.shown";

    // the values of the page that every operation fills in, which are the
    // only ones that more than one of them has to agree about
    static final String CONTENT_VALUE = "crud:content";
    static final String ENTITY_LABEL_VALUE = "crud:entityLabel";
    static final String FORM_TITLE_VALUE = "crud:formTitle";
    static final String FORM_URL_VALUE = "crud:formUrl";
    static final String CANCEL_URL_VALUE = "crud:cancelUrl";
    static final String SUBMIT_LABEL_VALUE = "crud:submitLabel";

    protected final CrudAdmin admin_;
    protected final CrudEntity<T> entity_;

    protected CrudElement(CrudAdmin admin, CrudEntity<T> entity) {
        admin_ = admin;
        entity_ = entity;
    }

    /**
     * Refuses the request when the entity is restricted to a role that the
     * user doesn't have, and performs the operation otherwise.
     *
     * @param c the context of the current request
     * @see #perform(Context)
     * @since 1.10
     */
    public final void process(Context c)
    throws Exception {
        if (entity_ != null && !isVisible(c, entity_)) {
            forbidden(c);
            return;
        }
        perform(c);
    }

    /**
     * Performs the operation that this element provides.
     *
     * @param c the context of the current request
     * @throws Exception when the operation couldn't be performed
     * @since 1.10
     */
    protected abstract void perform(Context c)
    throws Exception;

    private void forbidden(Context c) {
        c.setStatus(403);
        var t = page(c);
        t.setValueEncoded(ENTITY_LABEL_VALUE, entity_.getLabel());
        t.setBlock(CONTENT_VALUE, "crud:forbidden");
        c.print(t);
    }

    /**
     * Creates the page template of this administration and fills in
     * everything that every page shows.
     *
     * @param c the context of the current request
     * @return the page template
     * @since 1.10
     */
    protected Template page(Context c) {
        var t = c.template("crud.page");
        t.setValueEncoded("crud:title", admin_.title());

        if (admin_.environment() != null) {
            t.setValueEncoded("crud:environmentName", admin_.environment());
            t.setBlock("crud:environment", "crud:environment:present");
        }

        // the url of it holds the host that the request carried, which is
        // nothing that this page decided
        t.setValueEncoded("crud:styleUrl", c.urlFor(admin_.styleRoute()));
        // what an application adds is written as it stands, since it's the
        // application's own and not anything that arrived with a request
        t.setValue("crud:head", admin_.head());
        // the slug lets the styles of an application reach one entity without
        // reaching the others, while the kind of page says which of them is
        // being looked at, since an addition and a change are the same form
        var slug = entitySlug();
        t.setValueEncoded("crud:entityClass", slug == null ? "" : " crud-entity-" + slug);
        t.setValueEncoded("crud:pageClass", " crud-page-" + pageKind());

        t.setValueEncoded("crud:dashboardUrl", c.urlFor(admin_.getDashboardRoute()));
        for (var registered : admin_.getEntities()) {
            if (!isVisible(c, registered)) {
                continue;
            }
            t.setValueEncoded("crud:menuLabel", registered.getLabelPlural());
            t.setValueEncoded("crud:menuUrl", c.urlFor(admin_.routes(registered).browse));
            t.setValue("crud:menuClass", registered.getSlug().equals(entitySlug()) ? "crud-current" : "");
            t.appendBlock("crud:menu", "crud:menuItem");
        }
        for (var link : admin_.links().entrySet()) {
            t.setValueEncoded("crud:menuLabel", link.getKey());
            t.setValueEncoded("crud:menuUrl", link.getValue());
            t.setValue("crud:menuClass", "");
            t.appendBlock("crud:menu", "crud:menuItem");
        }

        var flash = c.attribute(FLASH_SHOWN_ATTRIBUTE);
        if (null == flash) {
            var session = c.session(false);
            flash = session == null ? null : session.attribute(FLASH_ATTRIBUTE);
            if (flash != null) {
                // taking it out of the session makes it show only once, so
                // it's kept with the request in case this page is rendered
                // more than once while handling it
                session.removeAttribute(FLASH_ATTRIBUTE);
                c.setAttribute(FLASH_SHOWN_ATTRIBUTE, flash);
            }
        }
        if (flash != null) {
            t.setValueEncoded("crud:flashMessage", String.valueOf(flash));
            t.setBlock("crud:flash", "crud:flash:present");
        }

        return t;
    }

    /**
     * Stores a message that is shown on the next page that is rendered.
     *
     * @param c       the context of the current request
     * @param message the message to show
     * @since 1.10
     */
    protected void flash(Context c, String message) {
        c.session().setAttribute(FLASH_ATTRIBUTE, message);
    }

    /**
     * Sends the browser back to the browse page of this entity, on the page
     * that the operation came from.
     *
     * @param c the context of the current request
     * @since 1.10
     */
    protected void redirectToBrowse(Context c) {
        c.redirect(c.urlFor(admin_.routes(entity_).browse).params(browseState(c)));
    }

    /**
     * Collects the state of the browse page that this operation came from,
     * as its row links carried it along.
     * <p>This lets finishing or cancelling an operation land back on the
     * page it started from, searched, sorted and paged the way it was.
     * The state is validated the way the browse page itself validates it, so
     * a form whose fields happen to carry these names doesn't put what was
     * typed into them in a URL.
     *
     * @param c the context of the current request
     * @return the browse state that came along with the request
     * @since 1.10
     */
    protected java.util.Map<String, String[]> browseState(Context c) {
        var state = new java.util.LinkedHashMap<String, String[]>();
        var search = c.parameter("search");
        if (search != null && !search.isBlank() && !entity_.getSearchableProperties().isEmpty()) {
            state.put("search", new String[]{search});
        }
        var sort = c.parameter("sort");
        if (sort != null && entity_.isSortable(sort)) {
            state.put("sort", new String[]{sort});
            if ("desc".equals(c.parameter("dir"))) {
                state.put("dir", new String[]{"desc"});
            }
        }
        var offset = c.parameterInt("offset", 0);
        if (offset > 0) {
            state.put("offset", new String[]{String.valueOf(offset)});
        }
        return state;
    }

    /**
     * Restores the instance that the identifier in the path info of the
     * request corresponds to.
     *
     * @param c the context of the current request
     * @return the restored instance; or
     * <p>{@code null} when no instance corresponds to the identifier
     * @since 1.10
     */
    protected T restoreInstance(Context c) {
        var id = requestedIdentifier(c);
        if (id < 0) {
            return null;
        }
        return entity_.createManager(admin_.datasource()).restore(id);
    }

    /**
     * Puts the identifier that the request asked for back onto an instance.
     * <p>A callback of the bean can give an instance another identifier on
     * the way out of the database, which would make whatever is done with it
     * afterwards apply to a different row than the one that was asked for.
     *
     * @param instance the instance to correct
     * @param id       the identifier that the request asked for
     * @since 1.10
     */
    protected void forceRequestedIdentifier(T instance, int id) {
        try {
            BeanUtils.setPropertyValue(instance, entity_.getIdentifier(), id);
        } catch (Exception e) {
            throw new PropertyAccessException(entity_.getBeanClass(), entity_.getIdentifier(), e);
        }
    }

    /**
     * Puts the values that the database holds for the identifier, the ordinal
     * and the list back onto an instance.
     * <p>A callback of the store can have changed any of them afterwards,
     * which would give a hook an instance that doesn't describe what was
     * written.
     *
     * @since 1.10
     */
    protected void forceStoredPlacement(T instance, int id, long restriction, int ordinal) {
        forceRequestedIdentifier(instance, id);
        if (entity_.isOrdered()) {
            forceProperty(instance, entity_.getOrdinal(), ordinal);
            if (entity_.getOrdinalRestriction() != null) {
                forceProperty(instance, entity_.getOrdinalRestriction(), restriction);
            }
        }
    }

    /**
     * Puts a value that the database holds back onto an instance, so that a
     * callback which changed it doesn't get to pass it on.
     *
     * @since 1.10
     */
    protected void forceProperty(T instance, String property, Object value) {
        try {
            // the lists are named by longs while the property that holds one
            // can be any whole number, so the value is given the width that
            // the bean declares for it
            var type = BeanUtils.getPropertyType(entity_.getBeanClass(), property);
            BeanUtils.setPropertyValue(instance, property, Convert.toType(value, type));
        } catch (Exception e) {
            throw new PropertyAccessException(entity_.getBeanClass(), property, e);
        }
    }

    /**
     * Retrieves the identifier that the request asked for.
     * <p>This says which instance an operation applies to, rather than the
     * identifier that a restored instance carries, since a callback of the
     * bean can have given it another one on the way out of the database.
     *
     * @param c the context of the current request
     * @return the identifier in the path info of the request; or
     * <p>{@code -1} when the request didn't ask for one
     * @since 1.10
     */
    protected int requestedIdentifier(Context c) {
        return c.parameterInt("crudId", -1);
    }

    /**
     * Sends the browser back to the browse page with a message that says
     * that the instance is gone.
     *
     * @param c the context of the current request
     * @since 1.10
     */
    protected void missingInstance(Context c) {
        flash(c, entity_.getLabel() + " " + c.parameter("crudId") + " no longer exists.");
        redirectToBrowse(c);
    }

    /**
     * Indicates whether an entity is shown to the user of the current
     * request.
     * <p>An entity that is restricted to a role is only shown to the users
     * that have it, which relies on an authentication element having
     * established the identity of the request beforehand. The operations of
     * a hidden entity refuse the request too, so that its routes can't be
     * reached by typing their URL either.
     *
     * @param c      the context of the current request
     * @param entity the entity to evaluate
     * @return {@code true} when the entity is shown; or
     * <p>{@code false} when it's hidden
     * @since 1.10
     */
    protected boolean isVisible(Context c, CrudEntity<?> entity) {
        var role = entity.getOptions().getRole();
        if (null == role) {
            return true;
        }
        for (var identity : identities(c)) {
            if (identity.getAttributes().isInRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the identities that an authentication element established
     * for this request.
     * <p>Every one of them is considered, since the order in which the
     * attributes are visited isn't the order in which the authentication
     * elements were mounted. They're looked up once and kept with the
     * request, since a page that shows a menu asks this for every entity
     * that is registered.
     *
     * @param c the context of the current request
     * @return the identities of this request
     * @since 1.10
     */
    @SuppressWarnings("unchecked")
    protected List<RoleUserIdentity> identities(Context c) {
        if (c.attribute(IDENTITY_ATTRIBUTE) instanceof List<?> cached) {
            return (List<RoleUserIdentity>) cached;
        }

        var result = new ArrayList<RoleUserIdentity>();
        for (var name : c.attributeNames()) {
            if (c.attribute(name) instanceof RoleUserIdentity identity) {
                result.add(identity);
            }
        }
        c.setAttribute(IDENTITY_ATTRIBUTE, result);
        return result;
    }

    /**
     * Names the kind of page that this operation renders, which its class on
     * the page is made of.
     *
     * @since 1.10
     */
    protected abstract String pageKind();

    String entitySlug() {
        return entity_ == null ? null : entity_.getSlug();
    }

    /**
     * Remembers that the operation was refused, by the database or by a
     * hook, so that the form that is rendered afterwards can say so.
     * <p>The message is kept with the request, since an element is shared by
     * every request that reaches it and what one of them ran into is nothing
     * that the others should be told about.
     *
     * @param c       the context of the current request
     * @param message the message to show above the form
     * @since 1.10
     */
    protected void refused(Context c, String message) {
        // an operation that runs outside of a request has nowhere to show
        // this and nobody to show it to
        if (c != null) {
            c.setAttribute(REFUSAL_ATTRIBUTE, message);
        }
    }

    /**
     * Retrieves what was refused during this request.
     *
     * @param c the context of the current request
     * @return the message to show above the form; or
     * <p>{@code null} when nothing was refused
     * @see #refused(Context, String)
     * @since 1.10
     */
    protected String refusalMessage(Context c) {
        var message = c.attribute(REFUSAL_ATTRIBUTE);
        return message == null ? null : String.valueOf(message);
    }

    /**
     * Runs the hooks that can refuse an operation, in the order that they
     * were registered.
     * <p>A hook that fails is reported as that hook having failed, so that
     * the database refusing what a hook did isn't mistaken for the database
     * refusing what was submitted.
     *
     * @param c        the context of the current request
     * @param name     the name of the hook, for when one fails
     * @param hooks    the hooks to run
     * @param instance the instance that the operation is about
     * @return {@code null} when every hook lets the operation proceed; or
     * <p>the message of the first one that refuses it
     * @since 1.10
     */
    protected String checkHooks(Context c, String name, List<CrudBeforeHook<T>> hooks, T instance) {
        for (var hook : hooks) {
            String refusal;
            try {
                refusal = hook.check(c, instance);
            } catch (Exception e) {
                // a hook that sends the request somewhere else isn't a
                // hook that failed
                if (e instanceof ControlFlowRuntimeException) {
                    throw e;
                }
                throw new HookFailedException(entity_.getBeanClass(), name, e);
            }
            if (refusal != null) {
                return refusal;
            }
        }
        return null;
    }

    /**
     * Runs the hooks of an operation that was performed, in the order that
     * they were registered.
     * <p>A hook that fails is reported as that hook having failed, so that
     * the database refusing what a hook did isn't mistaken for the database
     * refusing what was submitted, and the transaction of the operation
     * takes the operation back.
     *
     * @param c        the context of the current request
     * @param name     the name of the hook, for when one fails
     * @param hooks    the hooks to run
     * @param instance the instance that the operation was about
     * @since 1.10
     */
    protected void performHooks(Context c, String name, List<CrudAfterHook<T>> hooks, T instance) {
        for (var hook : hooks) {
            try {
                hook.perform(c, instance);
            } catch (Throwable e) {
                throw afterHookFailure(name, e);
            }
        }
    }

    /**
     * Runs the hooks that can refuse a move, in the order that they were
     * registered.
     *
     * @param c       the context of the current request
     * @param hooks   the hooks to run
     * @param id      the identifier of the instance that is being moved
     * @param upwards whether the instance moves towards the start of its
     *                list
     * @return {@code null} when every hook lets the move proceed; or
     * <p>the message of the first one that refuses it
     * @since 1.10
     */
    protected String checkMoveHooks(Context c, List<CrudBeforeMoveHook> hooks, int id, boolean upwards) {
        for (var hook : hooks) {
            String refusal;
            try {
                refusal = hook.check(c, id, upwards);
            } catch (Exception e) {
                // a hook that sends the request somewhere else isn't a
                // hook that failed
                if (e instanceof ControlFlowRuntimeException) {
                    throw e;
                }
                throw new HookFailedException(entity_.getBeanClass(), "beforeMove", e);
            }
            if (refusal != null) {
                return refusal;
            }
        }
        return null;
    }

    /**
     * Runs the hooks of a move that was performed, in the order that they
     * were registered.
     *
     * @param c       the context of the current request
     * @param hooks   the hooks to run
     * @param id      the identifier of the instance that was moved
     * @param upwards whether the instance moved towards the start of its
     *                list
     * @since 1.10
     */
    protected void performMoveHooks(Context c, List<CrudAfterMoveHook> hooks, int id, boolean upwards) {
        for (var hook : hooks) {
            try {
                hook.perform(c, id, upwards);
            } catch (Throwable e) {
                throw afterHookFailure("afterMove", e);
            }
        }
    }

    // an after hook runs inside the transaction of the operation, and
    // sending the request somewhere else would commit that transaction and
    // send the browser on without the operation reporting its own outcome,
    // so the signal is replaced with a failure that takes the operation back
    private RuntimeException afterHookFailure(String name, Throwable error) {
        if (error instanceof ControlFlowRuntimeException) {
            return new HookDivertedRequestException(entity_.getBeanClass(), name);
        }
        // the failures that nothing can be done about stay what they are
        if (error instanceof Error fatal) {
            throw fatal;
        }
        return new HookFailedException(entity_.getBeanClass(), name, error);
    }

    /**
     * Clears the validation of an instance before the submitted values are
     * filled into it.
     * <p>Filling in a value that can't be converted is what reports that it
     * can't be, so clearing the validation afterwards would throw away
     * exactly what has to be shown.
     *
     * @param instance the instance whose validation is cleared
     * @since 1.10
     */
    protected void resetValidation(T instance) {
        if (instance instanceof Validated validated) {
            validated.resetValidation();
        }
    }


    // performs an operation and the hooks that follow it inside one
    // transaction, so that a hook that throws takes the operation back; a
    // datasource without transactions is refused here rather than letting
    // the operation promise something it can't provide
    void inTransaction(TransactionUserWithoutResult user) {
        admin_.verifyTransactions();
        new DbQueryManager(admin_.datasource()).inTransaction(user);
    }

    // class 23 of the SQL states is the integrity constraint violation
    // class, which the drivers agree on, so an operation can handle a
    // violated constraint instead of reporting it as an error
    static boolean isConstraintViolation(Throwable error) {
        return hasSqlStateClass(error, "23");
    }

    // simultaneous transactions that conflicted can be reported as
    // something to try again instead of as an error
    static boolean isTransactionConflict(Throwable error) {
        // class 40 of the SQL states is the transaction rollback class,
        // covering deadlocks and serialization failures
        return hasSqlStateClass(error, "40");
    }

    private static boolean hasSqlStateClass(Throwable error, String stateClass) {
        while (error != null) {
            if (error instanceof SQLException sql &&
                sql.getSQLState() != null &&
                sql.getSQLState().startsWith(stateClass)) {
                return true;
            }
            error = error.getCause();
        }
        return false;
    }
}
