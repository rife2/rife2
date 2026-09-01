/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.crud.exceptions.*;


/**
 * A custom operation that is performed on a single instance of an entity.
 * <p>Actions are what makes it possible to administer the domain verbs of
 * an entity, like activating a poll or publishing an article, next to the
 * operations that this administration provides itself.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#action(String, String, CrudActionHandler)
 * @since 1.10
 */
public class CrudAction<T> {
    private final String name_;
    private final String label_;
    private final CrudActionHandler<T> handler_;
    private CrudCondition<T> visibleWhen_;
    private boolean confirm_;
    private boolean registered_ = false;

    /**
     * Creates a new action.
     * <p>An action that is created this way is provided to
     * {@link CrudEntityOptions#action(CrudAction)}, which makes it possible
     * to keep it in its own class:
     * <pre>public class PublishAction extends CrudAction&lt;Article&gt; {
     *    public PublishAction() {
     *        super("publish", "Publish", new PublishArticle());
     *        visibleWhen(new Unpublished());
     *    }
     *}</pre>
     *
     * @param name    the name of the action, which is used in its URL
     * @param label   the label of the action's button
     * @param handler the handler that performs the action
     * @since 1.10
     */
    public CrudAction(String name, String label, CrudActionHandler<T> handler) {
        this(name, label, handler, null, false);
    }


    CrudAction(String name, String label, CrudActionHandler<T> handler, CrudCondition<T> visibleWhen, boolean confirm) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (null == handler) throw new IllegalArgumentException("handler can't be null.");
        // the name ends up in the URL of the action, where anything else
        // would either be escaped or change what the route matches
        if (!CrudEntity.isValidUrlPart(name)) {
            throw new InvalidActionNameException(name);
        }
        if (label != null && label.isBlank()) {
            throw new IllegalArgumentException("The label of the '" + name + "' action can't be blank.");
        }

        name_ = name;
        label_ = label != null ? label : name;
        handler_ = handler;
        visibleWhen_ = visibleWhen;
        confirm_ = confirm;
    }

    /**
     * Sets the condition that decides whether this action applies to an
     * instance.
     * <p>The button is only shown for the instances that the condition
     * accepts, and the condition is evaluated again when the action is
     * submitted, so that a refused instance can't be acted on through a
     * forged request.
     *
     * @param visibleWhen the condition that accepts the instances this
     *                    action applies to
     * @return this action instance
     * @see CrudCondition
     * @since 1.10
     */
    public CrudAction<T> visibleWhen(CrudCondition<T> visibleWhen) {
        verifyChangeable();

        visibleWhen_ = visibleWhen;
        return this;
    }

    /**
     * Makes this action ask for a confirmation before it's performed.
     *
     * @return this action instance
     * @since 1.10
     */
    public CrudAction<T> confirm() {
        verifyChangeable();

        confirm_ = true;
        return this;
    }

    // remembers that this action has been registered with an entity, after
    // which the browse page and the routes describe it as it is
    void register() {
        registered_ = true;
    }

    // refuses a change that comes too late to have any effect
    private void verifyChangeable() {
        if (registered_) {
            throw new ConfigurationRegisteredException("'" + name_ + "' action");
        }
    }

    /**
     * Retrieves the name of this action, which is used in its URL.
     *
     * @return the name of this action
     * @since 1.10
     */
    public String getName() {
        return name_;
    }

    /**
     * Retrieves the label of this action's button.
     *
     * @return the label of this action
     * @since 1.10
     */
    public String getLabel() {
        return label_;
    }

    /**
     * Retrieves the handler that performs this action.
     *
     * @return the handler of this action
     * @since 1.10
     */
    public CrudActionHandler<T> getHandler() {
        return handler_;
    }

    /**
     * Indicates whether this action asks for a confirmation.
     *
     * @return {@code true} when this action is confirmed; or
     * <p>{@code false} when it's performed right away
     * @see #confirm()
     * @since 1.10
     */
    public boolean isConfirmed() {
        return confirm_;
    }

    /**
     * Indicates whether this action applies to an instance.
     *
     * @param instance the instance to evaluate
     * @return {@code true} when this action applies; or
     * <p>{@code false} when it doesn't
     * @see #visibleWhen(CrudCondition)
     * @since 1.10
     */
    public boolean isVisible(T instance) {
        return visibleWhen_ == null || visibleWhen_.accepts(instance);
    }
}
