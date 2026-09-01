/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.crud.exceptions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configures how a single bean class is administered.
 * <p>An instance of this class is handed to the lambda of
 * {@link CrudAdmin#entity(Class, CrudEntityBuilder)}, so that you
 * can tailor the administration of that entity without having to subclass
 * anything.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudAdmin
 * @since 1.10
 */
public class CrudEntityOptions<T> {
    private String slug_ = null;
    private String label_ = null;
    private final Map<String, String> help_ = new LinkedHashMap<>();
    private final Map<String, List<String>> sections_ = new LinkedHashMap<>();
    private final Map<String, CrudColumnRenderer<T>> columns_ = new LinkedHashMap<>();
    private String labelPlural_ = null;
    private String table_ = null;
    private String role_ = null;
    private int pageSize_ = 20;
    private CrudCondition<T> deletable_ = null;
    private final List<CrudAction<T>> actions_ = new ArrayList<>();
    private final List<CrudBeforeHook<T>> beforeAdds_ = new ArrayList<>();
    private final List<CrudAfterHook<T>> afterAdds_ = new ArrayList<>();
    private final List<CrudBeforeHook<T>> beforeUpdates_ = new ArrayList<>();
    private final List<CrudAfterHook<T>> afterUpdates_ = new ArrayList<>();
    private final List<CrudBeforeHook<T>> beforeDeletes_ = new ArrayList<>();
    private final List<CrudAfterHook<T>> afterDeletes_ = new ArrayList<>();
    private final List<CrudBeforeMoveHook> beforeMoves_ = new ArrayList<>();
    private final List<CrudAfterMoveHook> afterMoves_ = new ArrayList<>();
    private CrudEntityOptions<T> source_ = null;
    private boolean registered_ = false;

    /**
     * Creates new options with the defaults of every setting.
     *
     * @since 1.10
     */
    public CrudEntityOptions() {
    }

    CrudEntityOptions(CrudEntityOptions<T> other) {
        slug_ = other.slug_;
        label_ = other.label_;
        help_.putAll(other.help_);
        other.sections_.forEach((section, properties) -> sections_.put(section, new ArrayList<>(properties)));
        columns_.putAll(other.columns_);
        labelPlural_ = other.labelPlural_;
        table_ = other.table_;
        role_ = other.role_;
        pageSize_ = other.pageSize_;
        deletable_ = other.deletable_;
        // the actions themselves are kept, since an action is allowed to be
        // a class of its own and copying one into a plain action would leave
        // whatever it does behind
        // changing one afterwards is refused by the registration instead
        actions_.addAll(other.actions_);
        beforeAdds_.addAll(other.beforeAdds_);
        afterAdds_.addAll(other.afterAdds_);
        beforeUpdates_.addAll(other.beforeUpdates_);
        afterUpdates_.addAll(other.afterUpdates_);
        beforeDeletes_.addAll(other.beforeDeletes_);
        afterDeletes_.addAll(other.afterDeletes_);
        beforeMoves_.addAll(other.beforeMoves_);
        afterMoves_.addAll(other.afterMoves_);
        source_ = other;
    }

    // refuses every further change to these options and to the ones that
    // they were copied from, which is done once a registration has actually
    // succeeded so that a failed one leaves its caller with a configuration
    // that can still be corrected
    void register() {
        registered_ = true;
        for (var action : actions_) {
            action.register();
        }
        if (source_ != null) {
            source_.registered_ = true;
            for (var action : source_.actions_) {
                action.register();
            }
        }
    }

    // refuses a change that comes too late to have any effect
    private void verifyChangeable() {
        if (registered_) {
            throw new ConfigurationRegisteredException("options of this entity");
        }
    }

    /**
     * Sets the URL slug that the routes of this entity are mounted on.
     * <p>This defaults to the uncapitalized simple name of the bean class.
     *
     * @param slug the URL slug of this entity
     * @return this options instance
     * @since 1.10
     */
    public CrudEntityOptions<T> slug(String slug) {
        verifyChangeable();
        if (null == slug) throw new IllegalArgumentException("slug can't be null.");
        // the slug ends up in the URL of every operation of this entity
        if (!CrudEntity.isValidUrlPart(slug)) {
            throw new InvalidSlugException(slug);
        }

        slug_ = slug;
        return this;
    }

    /**
     * Says what a property is for, underneath the field that enters it.
     * <p>The constraints of a property say what a value of it has to be,
     * while this says what it means, which is what somebody filling in a
     * form has to know:
     * <pre>.entity(Article.class, e -&gt; e
     *    .help("slug", "This is what the address of the article ends with."))</pre>
     *
     * @param property the property to say something about
     * @param text     what to say about it
     * @return this options instance
     * @since 1.10
     */
    public CrudEntityOptions<T> help(String property, String text) {
        verifyChangeable();
        if (null == property) throw new IllegalArgumentException("property can't be null.");
        if (null == text) throw new IllegalArgumentException("text can't be null.");

        help_.put(property, text);
        return this;
    }

    String help(String property) {
        return help_.get(property);
    }

    Map<String, String> getHelp() {
        return help_;
    }

    /**
     * Groups properties into a titled section of the form.
     * <p>The fields of a section sit together inside a box that carries the
     * label, in the order that they're listed here, and the sections follow
     * each other in the order that they were declared. The properties that
     * no section claims keep their place at the top of the form:
     * <pre>.entity(Article.class, e -&gt; e
     *    .section("Content", "title", "body")
     *    .section("Publication", "status", "featured"))</pre>
     * <p>A property belongs to a single section, and a section that lists
     * one that another section already claims is refused.
     *
     * @param label      the label that the section carries
     * @param properties the properties that make up the section
     * @return this options instance
     * @since 1.10
     */
    public CrudEntityOptions<T> section(String label, String... properties) {
        verifyChangeable();
        if (null == label) throw new IllegalArgumentException("label can't be null.");
        if (label.isBlank()) throw new IllegalArgumentException("label can't be blank.");
        if (null == properties || 0 == properties.length) throw new IllegalArgumentException("properties can't be empty.");
        // two sections with the same label read as one section, while their
        // fields would still be split between two boxes
        if (sections_.containsKey(label)) {
            throw new DuplicateSectionException(label);
        }

        var section = new ArrayList<String>();
        for (var property : properties) {
            if (null == property) throw new IllegalArgumentException("property can't be null.");
            // a property that two sections claim would show its field twice,
            // where a submission only fills in one of them
            for (var declared : sections_.entrySet()) {
                if (declared.getValue().contains(property)) {
                    throw new PropertyInMultipleSectionsException(property, declared.getKey());
                }
            }
            if (section.contains(property)) {
                throw new PropertyInMultipleSectionsException(property, label);
            }
            section.add(property);
        }

        sections_.put(label, section);
        return this;
    }

    Map<String, List<String>> getSections() {
        return sections_;
    }

    /**
     * Renders a column of the browse table differently from the formatted
     * value.
     * <p>The renderer is executed for every cell of its column and returns
     * what that cell shows, so a status can be a badge instead of text:
     * <pre>.entity(Article.class, e -&gt; e
     *    .column("status", (article, value) -&gt; CrudCell.badge(value, article.getStatus())))</pre>
     * <p>Only what the cell shows changes, the header and the place of the
     * column stay what the constraints made them.
     *
     * @param property the property whose column is rendered
     * @param renderer the renderer that decides what its cells show
     * @return this options instance
     * @see CrudColumnRenderer
     * @see CrudCell
     * @since 1.10
     */
    public CrudEntityOptions<T> column(String property, CrudColumnRenderer<T> renderer) {
        verifyChangeable();
        if (null == property) throw new IllegalArgumentException("property can't be null.");
        if (null == renderer) throw new IllegalArgumentException("renderer can't be null.");

        columns_.put(property, renderer);
        return this;
    }

    CrudColumnRenderer<T> column(String property) {
        return columns_.get(property);
    }

    Map<String, CrudColumnRenderer<T>> getColumns() {
        return columns_;
    }

    /**
     * Sets the label that is displayed for this entity.
     * <p>This defaults to the simple name of the bean class.
     *
     * @param label the display label of this entity
     * @return this options instance
     * @since 1.10
     */
    public CrudEntityOptions<T> label(String label) {
        verifyChangeable();
        if (null == label) throw new IllegalArgumentException("label can't be null.");
        if (label.isBlank()) throw new IllegalArgumentException("label can't be blank.");

        label_ = label;
        return this;
    }

    /**
     * Sets the label that is displayed for several instances of this entity.
     * <p>This defaults to the singular label with an {@code s} appended to
     * it, which is what the titles and the messages that talk about more
     * than one instance use.
     *
     * @param labelPlural the plural display label of this entity
     * @return this options instance
     * @see #label(String)
     * @since 1.10
     */
    public CrudEntityOptions<T> labelPlural(String labelPlural) {
        verifyChangeable();
        if (null == labelPlural) throw new IllegalArgumentException("labelPlural can't be null.");
        if (labelPlural.isBlank()) throw new IllegalArgumentException("labelPlural can't be blank.");

        labelPlural_ = labelPlural;
        return this;
    }

    /**
     * Sets the database table that the instances of this entity are stored
     * in.
     * <p>This defaults to the table that the query manager derives from the
     * bean class, and it makes it possible to administer the same bean class
     * in several tables.
     *
     * @param table the name of the database table
     * @return this options instance
     * @since 1.10
     */
    public CrudEntityOptions<T> table(String table) {
        verifyChangeable();
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        // the name ends up in the generated queries, where anything else
        // fails at the first request instead of at configuration
        if (!table.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("The table name '" + table + "' isn't usable, use letters, digits and underscores without a leading digit.");
        }

        table_ = table;
        return this;
    }

    /**
     * Sets the authentication role that this entity belongs to.
     * <p>The dashboard and the menu will only show this entity to the users
     * that have this role, and its operations refuse the requests of the
     * others, so that its routes can't be reached by typing their URL
     * either. This relies on an authentication element having established
     * the identity of the request beforehand, which you do by mounting the
     * administration inside a group that is preceded by one.
     *
     * @param role the name of the authentication role
     * @return this options instance
     * @since 1.10
     */
    public CrudEntityOptions<T> role(String role) {
        verifyChangeable();

        role_ = role;
        return this;
    }

    /**
     * Sets the number of instances that are shown on a single browse page.
     * <p>This defaults to {@code 20}.
     *
     * @param pageSize the number of instances per page
     * @return this options instance
     * @since 1.10
     */
    public CrudEntityOptions<T> pageSize(int pageSize) {
        verifyChangeable();
        if (pageSize < 1) throw new IllegalArgumentException("pageSize has to be positive.");
        pageSize_ = pageSize;
        return this;
    }

    /**
     * Sets the condition that decides whether an instance can be deleted.
     * <p>The delete button is only shown for the instances that the
     * condition accepts, and the condition is evaluated again when the
     * deletion is submitted, so that a refused instance can't be deleted
     * through a forged request.
     *
     * @param deletable the condition that accepts the deletable instances
     * @return this options instance
     * @see CrudCondition
     * @since 1.10
     */
    public CrudEntityOptions<T> deletable(CrudCondition<T> deletable) {
        verifyChangeable();

        deletable_ = deletable;
        return this;
    }

    /**
     * Adds a custom action that is performed on a single instance.
     * <p>The action shows up as a button in the actions column of the browse
     * table, and it's performed through a POST request, just like the
     * operations that this administration provides itself.
     *
     * @param name    the name of the action, which is used in its URL
     * @param label   the label of the action's button
     * @param handler the handler that performs the action
     * @return this options instance
     * @see CrudAction
     * @since 1.10
     */
    public CrudEntityOptions<T> action(String name, String label, CrudActionHandler<T> handler) {
        return action(new CrudAction<>(name, label, handler, null, false));
    }

    /**
     * Adds a custom action that is performed on a single instance, with
     * additional configuration.
     *
     * @param name    the name of the action, which is used in its URL
     * @param label   the label of the action's button
     * @param handler the handler that performs the action
     * @param options the builder that configures the action
     * @return this options instance
     * @see CrudAction
     * @since 1.10
     */
    public CrudEntityOptions<T> action(String name, String label, CrudActionHandler<T> handler, CrudActionBuilder<T> options) {
        verifyChangeable();
        if (null == options) throw new IllegalArgumentException("options can't be null.");

        var action = new CrudAction<>(name, label, handler, null, false);
        options.build(action);
        return action(action);
    }

    /**
     * Adds a custom action that is performed on a single instance.
     * <p>Providing the action as an instance makes it possible to keep it in
     * its own class, together with the rules that decide when it applies.
     *
     * @param action the action to add
     * @return this options instance
     * @see CrudAction
     * @see #action(String, String, CrudActionHandler)
     * @since 1.10
     */
    public CrudEntityOptions<T> action(CrudAction<T> action) {
        verifyChangeable();
        if (null == action) throw new IllegalArgumentException("action can't be null.");
        // two actions with the same name would be mounted on the same URL,
        // where the button of one of them performs the other one
        for (var registered : actions_) {
            if (registered.getName().equals(action.getName())) {
                throw new DuplicateActionNameException(action.getName());
            }
        }

        actions_.add(action);
        return this;
    }

    /**
     * Adds a hook that is executed right before an instance is added.
     * <p>The hook runs after the submission was validated, and it can refuse
     * the addition by returning the message that the add form redisplays.
     * Hooks can be added several times and run in the order they were added,
     * where the first refusal wins.
     *
     * @param hook the hook to add
     * @return this options instance
     * @see CrudBeforeHook
     * @since 1.10
     */
    public CrudEntityOptions<T> beforeAdd(CrudBeforeHook<T> hook) {
        verifyChangeable();
        if (null == hook) throw new IllegalArgumentException("hook can't be null.");

        beforeAdds_.add(hook);
        return this;
    }

    /**
     * Adds a hook that is executed right after an instance was added.
     * <p>The hook runs inside the transaction of the addition, after the
     * administration verified what actually reached the database, so
     * throwing from it takes the addition back. Hooks can be added several
     * times and run in the order they were added.
     *
     * @param hook the hook to add
     * @return this options instance
     * @see CrudAfterHook
     * @since 1.10
     */
    public CrudEntityOptions<T> afterAdd(CrudAfterHook<T> hook) {
        verifyChangeable();
        if (null == hook) throw new IllegalArgumentException("hook can't be null.");

        afterAdds_.add(hook);
        return this;
    }

    /**
     * Adds a hook that is executed right before an instance is updated.
     * <p>The hook runs after the submission was validated, and it can refuse
     * the update by returning the message that the edit form redisplays.
     * Hooks can be added several times and run in the order they were added,
     * where the first refusal wins.
     *
     * @param hook the hook to add
     * @return this options instance
     * @see CrudBeforeHook
     * @since 1.10
     */
    public CrudEntityOptions<T> beforeUpdate(CrudBeforeHook<T> hook) {
        verifyChangeable();
        if (null == hook) throw new IllegalArgumentException("hook can't be null.");

        beforeUpdates_.add(hook);
        return this;
    }

    /**
     * Adds a hook that is executed right after an instance was updated.
     * <p>The hook runs inside the transaction of the update, after the
     * administration verified what actually reached the database, so
     * throwing from it takes the update back. Hooks can be added several
     * times and run in the order they were added.
     *
     * @param hook the hook to add
     * @return this options instance
     * @see CrudAfterHook
     * @since 1.10
     */
    public CrudEntityOptions<T> afterUpdate(CrudAfterHook<T> hook) {
        verifyChangeable();
        if (null == hook) throw new IllegalArgumentException("hook can't be null.");

        afterUpdates_.add(hook);
        return this;
    }

    /**
     * Adds a hook that is executed right before an instance is deleted.
     * <p>The hook can refuse the deletion by returning the message that the
     * browse page displays instead of performing it. Hooks can be added
     * several times and run in the order they were added, where the first
     * refusal wins.
     *
     * @param hook the hook to add
     * @return this options instance
     * @see CrudBeforeHook
     * @since 1.10
     */
    public CrudEntityOptions<T> beforeDelete(CrudBeforeHook<T> hook) {
        verifyChangeable();
        if (null == hook) throw new IllegalArgumentException("hook can't be null.");

        beforeDeletes_.add(hook);
        return this;
    }

    /**
     * Adds a hook that is executed right after an instance was deleted.
     * <p>The hook runs inside the transaction of the deletion, so throwing
     * from it takes the deletion back. Hooks can be added several times and
     * run in the order they were added.
     *
     * @param hook the hook to add
     * @return this options instance
     * @see CrudAfterHook
     * @since 1.10
     */
    public CrudEntityOptions<T> afterDelete(CrudAfterHook<T> hook) {
        verifyChangeable();
        if (null == hook) throw new IllegalArgumentException("hook can't be null.");

        afterDeletes_.add(hook);
        return this;
    }

    /**
     * Adds a hook that is executed right before an instance is moved.
     * <p>The hook can refuse the move by returning the message that the
     * browse page displays instead of performing it. Hooks can be added
     * several times and run in the order they were added, where the first
     * refusal wins.
     *
     * @param hook the hook to add
     * @return this options instance
     * @see CrudBeforeMoveHook
     * @since 1.10
     */
    public CrudEntityOptions<T> beforeMove(CrudBeforeMoveHook hook) {
        verifyChangeable();
        if (null == hook) throw new IllegalArgumentException("hook can't be null.");

        beforeMoves_.add(hook);
        return this;
    }

    /**
     * Adds a hook that is executed right after an instance was moved.
     * <p>The hook runs while the list that the instance moved in is still
     * being held, so throwing from it takes the move back. Hooks can be
     * added several times and run in the order they were added.
     *
     * @param hook the hook to add
     * @return this options instance
     * @see CrudAfterMoveHook
     * @since 1.10
     */
    public CrudEntityOptions<T> afterMove(CrudAfterMoveHook hook) {
        verifyChangeable();
        if (null == hook) throw new IllegalArgumentException("hook can't be null.");

        afterMoves_.add(hook);
        return this;
    }

    String slug() {
        return slug_;
    }

    String label() {
        return label_;
    }

    String labelPlural() {
        return labelPlural_;
    }

    String table() {
        return table_;
    }

    /**
     * Retrieves the authentication role that this entity belongs to.
     *
     * @return the name of the authentication role; or
     * <p>{@code null} when this entity isn't restricted to a role
     * @since 1.10
     */
    public String getRole() {
        return role_;
    }

    /**
     * Retrieves the number of instances that are shown on a single browse
     * page.
     *
     * @return the number of instances per page
     * @since 1.10
     */
    public int getPageSize() {
        return pageSize_;
    }

    /**
     * Indicates whether an instance can be deleted.
     *
     * @param instance the instance to evaluate
     * @return {@code true} when the instance can be deleted; or
     * <p>{@code false} when it can't
     * @since 1.10
     */
    public boolean isDeletable(T instance) {
        return deletable_ == null || deletable_.accepts(instance);
    }

    /**
     * Retrieves the custom actions of this entity.
     *
     * @return the custom actions
     * @since 1.10
     */
    public List<CrudAction<T>> getActions() {
        return Collections.unmodifiableList(actions_);
    }

    /**
     * Retrieves the hooks that are executed right before an instance is
     * added.
     *
     * @return the hooks, in the order that they were added
     * @since 1.10
     */
    public List<CrudBeforeHook<T>> getBeforeAddHooks() {
        return Collections.unmodifiableList(beforeAdds_);
    }

    /**
     * Retrieves the hooks that are executed right after an instance was
     * added.
     *
     * @return the hooks, in the order that they were added
     * @since 1.10
     */
    public List<CrudAfterHook<T>> getAfterAddHooks() {
        return Collections.unmodifiableList(afterAdds_);
    }

    /**
     * Retrieves the hooks that are executed right before an instance is
     * updated.
     *
     * @return the hooks, in the order that they were added
     * @since 1.10
     */
    public List<CrudBeforeHook<T>> getBeforeUpdateHooks() {
        return Collections.unmodifiableList(beforeUpdates_);
    }

    /**
     * Retrieves the hooks that are executed right after an instance was
     * updated.
     *
     * @return the hooks, in the order that they were added
     * @since 1.10
     */
    public List<CrudAfterHook<T>> getAfterUpdateHooks() {
        return Collections.unmodifiableList(afterUpdates_);
    }

    /**
     * Retrieves the hooks that are executed right before an instance is
     * deleted.
     *
     * @return the hooks, in the order that they were added
     * @since 1.10
     */
    public List<CrudBeforeHook<T>> getBeforeDeleteHooks() {
        return Collections.unmodifiableList(beforeDeletes_);
    }

    /**
     * Retrieves the hooks that are executed right after an instance was
     * deleted.
     *
     * @return the hooks, in the order that they were added
     * @since 1.10
     */
    public List<CrudAfterHook<T>> getAfterDeleteHooks() {
        return Collections.unmodifiableList(afterDeletes_);
    }

    /**
     * Retrieves the hooks that are executed right before an instance is
     * moved.
     *
     * @return the hooks, in the order that they were added
     * @since 1.10
     */
    public List<CrudBeforeMoveHook> getBeforeMoveHooks() {
        return Collections.unmodifiableList(beforeMoves_);
    }

    /**
     * Retrieves the hooks that are executed right after an instance was
     * moved.
     *
     * @return the hooks, in the order that they were added
     * @since 1.10
     */
    public List<CrudAfterMoveHook> getAfterMoveHooks() {
        return Collections.unmodifiableList(afterMoves_);
    }
}
