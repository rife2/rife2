/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.crud.exceptions.*;
import rife.database.Datasource;
import rife.database.exceptions.DatabaseException;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.engine.PathInfoHandling;
import rife.engine.Route;
import rife.engine.Router;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a complete administration for the bean classes that are
 * registered with it.
 * <p>The constraints of a bean are all that's needed to browse, add, edit,
 * delete and reorder its instances:
 * <pre>public void setup() {
 *    group("/admin", new CrudAdmin(datasource)
 *        .entity(Person.class)
 *        .entity(Article.class, e -&gt; e.pageSize(10)));
 *}</pre>
 * <h3>What a bean has to be</h3>
 * <p>A bean that is administered has to validate, which it does by
 * extending {@link rife.validation.Validation} or
 * {@link rife.validation.MetaData}, by carrying a metadata class of its own
 * that is merged into it, or by implementing {@link
 * rife.validation.Validated} itself. What an administration refuses is
 * reported through the validation of the instance that it was given, so a
 * bean that validates nothing has nowhere to say that a submitted value
 * couldn't be read at all, and every operation on it would report that it
 * succeeded. One that doesn't is refused with an {@link
 * rife.crud.exceptions.UnvalidatedBeanException} where it's registered.
 * <p>Every operation also constructs an instance, so the bean needs an
 * accessible constructor without arguments, which is verified when it's
 * registered as well.
 * <p>Every bean also needs an identifier: an {@code int} or {@code Integer}
 * property that can be read and written and that the database keeps, which
 * the administration assigns instead of asking for. Which property that is
 * comes from the {@code identifier} constraint, and a bean that doesn't
 * declare one has to call it {@code id}.
 * <p>Beyond the identifier, constraints aren't required, but without them an
 * administration only has the properties of the bean to go by and falls back
 * to its defaults for everything else.
 * <p>Authentication and CSRF protection aren't provided by this router
 * itself, you compose them from the outside by preceding the group that
 * this administration is mounted in with the corresponding elements, just
 * like you do for your own routes.
 * <p>The database structure of the registered entities can be created with
 * {@link #install()}, which is mostly convenient during development since
 * an application typically evolves its schema with
 * {@code rife.database.migrations} instead.
 * <h3>Doing your own work during the operations</h3>
 * <p>When something of your own has to happen while an operation is
 * performed, give it to the hooks of {@link CrudEntityOptions}:
 * {@code beforeAdd}, {@code afterAdd}, {@code beforeUpdate},
 * {@code afterUpdate}, {@code beforeDelete}, {@code afterDelete},
 * {@code beforeMove} and {@code afterMove}. They receive the context of the
 * request that performs the operation, a before hook can refuse it with a
 * message that the user gets to read, and an after hook runs inside the
 * transaction of the operation after what reached the database was
 * verified, so throwing from one takes the whole operation back:
 * <pre>.entity(Article.class, e -&gt; e
 *    .beforeAdd((c, article) -&gt; article.isSpam() ? "This looks like spam." : null)
 *    .afterDelete((c, article) -&gt; audit.deleted(c, article)))</pre>
 * <p>The hooks only run for operations that come through this
 * administration, code that stores the same beans directly doesn't trigger
 * them. A hook that fails is reported as that hook having failed, rather
 * than as the database having refused what was submitted.
 * <p>The before hooks decide, the after hooks do. An instance isn't
 * validated again after the before hooks have seen it, so a value that one
 * of them puts on it is stored unchecked, and whether the transaction of
 * the operation is already open when a before hook runs depends on the
 * entity, so what a before hook writes isn't taken back with the operation.
 * The after hooks are the ones that run inside it.
 * <p>The three values below aren't a hook's to change either. An instance
 * that an after hook stores again into another list than the one that is
 * being held reports a
 * {@link rife.crud.exceptions.PlacementChangedException}, since the
 * ordinals of the list it went to were never locked for it.
 * <p>Reordering is the one operation that doesn't hand instances around.
 * {@code beforeMove} and {@code afterMove} are given the identifier of the
 * instance and the direction instead, since a move works with where a row
 * sits and never restores it.
 * <h3>What the callbacks of a bean may do</h3>
 * <p>The {@link rife.database.querymanagers.generic.Callbacks callbacks} of
 * an administered bean keep running for every operation, whether this
 * administration performs it or your own code does, so keep them for what
 * belongs to the bean and give what belongs to the administration to the
 * hooks above. Under an administration they are yours to use, for
 * everything except the three values that it identifies and places an
 * instance by:
 * <ul>
 * <li>the identifier, which says which row an operation is about</li>
 * <li>the ordinal, which says where an instance sits</li>
 * <li>the property that the ordinal is restricted to, which says which
 * instances it sits between</li>
 * </ul>
 * <p>A callback that changes one of those makes an instance describe a
 * different row than the one that a request asked for, or moves it out of
 * the list whose ordinals were locked for it, which is something that this
 * administration relies on not happening rather than something it can undo.
 * <p>Where noticing it costs nothing, it's noticed: an operation that ends
 * up having stored other values than it was asked to store reports a
 * {@link rife.crud.exceptions.CallbackChangedIdentifierException} or a
 * {@link rife.crud.exceptions.PlacementChangedException} and takes
 * the change back. The pages that list instances trust what they were
 * handed, since checking every row of every page would cost a query for
 * each of them.
 * <p>{@code afterRestore} has to return {@code true}. Returning
 * {@code false} from it doesn't leave out the instance that it was given,
 * it keeps that one and stops the ones behind it from being read at all,
 * which is a page that shows a row it refused while hiding the rows that
 * come after it.
 * <p>There is no way yet to leave individual instances out of an
 * administration. A {@link CrudEntityOptions#deletable(CrudCondition)
 * condition} decides whether an instance can be deleted and a
 * {@link CrudEntityOptions#role(String) role} decides who reaches an entity
 * at all, so neither of them hides a row that somebody shouldn't see. Keep
 * the instances that not everybody may reach in an entity of their own.
 * <p>Everything else is fair game. Refusing an operation by returning
 * {@code false} from {@code beforeInsert}, {@code beforeUpdate} or
 * {@code beforeDelete}, filling in values that no form shows, and doing
 * work elsewhere are all things that the operations of this administration
 * account for, and a refusal is reported as the instance not having been
 * stored rather than as a failure. A callback can't say why it refused,
 * which the before hooks above do better.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntity
 * @see CrudEntityOptions
 * @since 1.10
 */
public class CrudAdmin extends Router {
    private final Datasource datasource_;
    private final List<CrudEntity<?>> entities_ = new ArrayList<>();
    private final Map<String, String> links_ = new LinkedHashMap<>();
    private final Map<String, CrudRoutes> routes_ = new LinkedHashMap<>();
    private String title_ = "Administration";
    private String environment_ = null;
    private volatile Boolean transactional_ = null;
    private Route dashboardRoute_ = null;
    private Route styleRoute_ = null;
    private final List<String> head_ = new ArrayList<>();
    private boolean setup_ = false;

    /**
     * Creates a new administration for a datasource.
     *
     * @param datasource the datasource that the instances are stored in
     * @since 1.10
     */
    public CrudAdmin(Datasource datasource) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        datasource_ = datasource;
    }

    /**
     * Registers a bean class to be administered.
     *
     * @param beanClass the bean class to administer, which has to validate
     *                  and to be constructible without arguments
     * @return this administration instance
     * @throws rife.crud.exceptions.UnvalidatedBeanException when the bean
     *                                                       doesn't validate
     * @throws rife.crud.exceptions.InstanceCreationException when an instance
     *                                                        of the bean
     *                                                        can't be created
     * @see #entity(Class, CrudEntityBuilder)
     * @since 1.10
     */
    public <T> CrudAdmin entity(Class<T> beanClass) {
        return entity(beanClass, new CrudEntityOptions<T>());
    }

    /**
     * Registers a bean class to be administered, with options.
     * <p>The options make it possible to tailor the administration of this
     * entity, for instance:
     * <pre>.entity(Article.class, e -&gt; e
     *    .pageSize(10)
     *    .action("publish", "Publish", (c, article) -&gt; {
     *        manager.publish(article);
     *        return "The article has been published.";
     *    }))</pre>
     *
     * @param beanClass the bean class to administer, which has to validate
     *                  and to be constructible without arguments
     * @param options   the builder that configures the administration of
     *                  this entity
     * @return this administration instance
     * @throws rife.crud.exceptions.UnvalidatedBeanException when the bean
     *                                                       doesn't validate
     * @throws rife.crud.exceptions.InstanceCreationException when an instance
     *                                                        of the bean
     *                                                        can't be created
     * @see CrudEntityOptions
     * @see #entity(Class, CrudEntityOptions)
     * @since 1.10
     */
    public <T> CrudAdmin entity(Class<T> beanClass, CrudEntityBuilder<T> options) {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");
        if (null == options) throw new IllegalArgumentException("options can't be null.");

        // checked before the builder runs, so that a registration that is
        // refused anyway doesn't execute its effects first
        verifyChangeable();

        var entity_options = new CrudEntityOptions<T>();
        options.build(entity_options);
        return entity(beanClass, entity_options);
    }

    /**
     * Registers a bean class to be administered, with the options that
     * tailor its administration.
     * <p>Providing the options as an instance keeps a site declaration a
     * declaration, since everything that configures an entity can live in
     * its own class:
     * <pre>group("/admin", new CrudAdmin(datasource)
     *    .entity(Article.class, new ArticleOptions()));</pre>
     *
     * @param beanClass the bean class to administer, which has to validate
     *                  and to be constructible without arguments
     * @param options   the options that this entity is administered with
     * @return this administration instance
     * @throws rife.crud.exceptions.UnvalidatedBeanException when the bean
     *                                                       doesn't validate
     * @throws rife.crud.exceptions.InstanceCreationException when an instance
     *                                                        of the bean
     *                                                        can't be created
     * @see CrudEntityOptions
     * @see #entity(Class, CrudEntityBuilder)
     * @since 1.10
     */
    public <T> CrudAdmin entity(Class<T> beanClass, CrudEntityOptions<T> options) {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");
        if (null == options) throw new IllegalArgumentException("options can't be null.");

        verifyChangeable();

        var entity = new CrudEntity<>(beanClass, options);

        for (var registered : entities_) {
            if (registered.getSlug().equals(entity.getSlug())) {
                throw new DuplicateSlugException(entity.getSlug(), beanClass, registered.getBeanClass());
            }
        }

        // the configuration is only frozen once it has actually been
        // accepted, so that a registration that was refused leaves what it
        // was given in a state that can still be corrected
        entity.getOptions().register();
        entities_.add(entity);
        return this;
    }

    // refuses a change that comes after the routes of this administration
    // have been set up
    private void verifyChangeable() {
        if (setup_) {
            throw new ConfigurationRegisteredException("entities and links of this administration");
        }
    }

    /**
     * Adds a link to the navigation of this administration.
     * <p>This makes it possible for the pages that you write yourself to sit
     * next to the ones that are generated, so that the whole administration
     * shares a single navigation.
     *
     * @param label the label of the link
     * @param url   the URL that the link points to
     * @return this administration instance
     * @since 1.10
     */
    public CrudAdmin link(String label, String url) {
        verifyChangeable();
        if (null == label) throw new IllegalArgumentException("label can't be null.");
        if (label.isBlank()) throw new IllegalArgumentException("label can't be blank.");
        if (null == url) throw new IllegalArgumentException("url can't be null.");
        if (url.isBlank()) throw new IllegalArgumentException("url can't be blank.");

        links_.put(label, url);
        return this;
    }

    /**
     * Sets the title of this administration.
     *
     * @param title the title that is displayed on every page
     * @return this administration instance
     * @since 1.10
     */
    public CrudAdmin title(String title) {
        verifyChangeable();
        if (null == title) throw new IllegalArgumentException("title can't be null.");
        if (title.isBlank()) throw new IllegalArgumentException("title can't be blank.");

        title_ = title;
        return this;
    }

    /**
     * Sets the name of the environment that this administration runs in.
     * <p>When it's provided, the name shows up in the header of every page,
     * so that a development or staging deployment can't be mistaken for the
     * production one.
     *
     * @param environment the name of the environment
     * @return this administration instance
     * @since 1.10
     */
    public CrudAdmin environment(String environment) {
        verifyChangeable();
        if (null == environment) throw new IllegalArgumentException("environment can't be null.");
        if (environment.isBlank()) throw new IllegalArgumentException("environment can't be blank.");

        environment_ = environment;
        return this;
    }

    /**
     * Creates the database structure of every registered entity.
     * <p>This is a convenience for development, where the structure follows
     * the beans that are being written. An application evolves its schema
     * with {@code rife.database.migrations} instead, which is also what
     * belongs in the startup of one that runs more than once at a time.
     * <p>The table of an entity that is ordered manually gets a unique
     * constraint on its ordinal, combined with the restriction when there
     * is one, since a list that holds nothing yet has no rows to lock and the
     * first additions to it would otherwise be handed the same ordinal. A
     * schema that evolves with {@code rife.database.migrations} declares
     * that constraint itself.
     * <p>An entity whose table is already there is skipped, so that this can
     * be called on every startup. Only that table is looked at, so a
     * structure whose creation stopped after it was made is skipped as well
     * and has to be removed by hand. A failure is reported and nothing is
     * recovered from, since guessing what went wrong could take away a
     * structure that something else is already using.
     *
     * @return this administration instance
     * @throws rife.database.exceptions.DatabaseException when the structure
     *                                                    couldn't be created
     * @since 1.10
     */
    public CrudAdmin install() {
        for (var entity : entities_) {
            var manager = entity.createManager(datasource_);
            if (hasStructure(manager)) {
                continue;
            }
            if (!entity.isOrdered()) {
                manager.install();
                continue;
            }

            // a list that holds nothing yet has no rows to lock, so the
            // very first additions to it can obtain the same ordinal, and
            // this uniqueness is what refuses that
            var query = manager.getInstallTableQuery();
            if (entity.getOrdinalRestriction() != null) {
                query.unique(new String[]{entity.getOrdinalRestrictionColumn(), entity.getOrdinalColumn()});
            } else {
                query.unique(entity.getOrdinalColumn());
            }
            manager.install(query);
        }
        return this;
    }

    /**
     * Removes the database structure of every registered entity.
     * <p>An entity whose structure isn't there is skipped, just like
     * {@link #install()} skips the ones that are.
     *
     * @return this administration instance
     * @throws rife.database.exceptions.DatabaseException when the structure
     *                                                    couldn't be removed
     * @see #install()
     * @since 1.10
     */
    public CrudAdmin remove() {
        // the tables that refer to others are removed before the ones that
        // they refer to, which is the reverse of how they're created
        for (var i = entities_.size() - 1; i >= 0; i--) {
            var manager = entities_.get(i).createManager(datasource_);
            if (hasStructure(manager)) {
                manager.remove();
            }
        }
        return this;
    }

    // checks whether the table of an entity is present, to tell an already
    // installed structure apart from a failure that has to be reported; the
    // database is asked for its tables instead of probing the structure with
    // a query, so that a connection that can't be established or a table
    // that can't be read isn't mistaken for a table that isn't there
    private boolean hasStructure(GenericQueryManager<?> manager) {
        var table = manager.getTable();
        var connection = datasource_.getConnection();
        try {
            var metadata = connection.getMetaData();
            var jdbc = metadata.getConnection();

            // the tables of the schema are listed without a name pattern,
            // since a pattern is both case-sensitive and treats underscores
            // as wildcards, while the drivers don't agree on the case that
            // they store an unquoted table name in
            try (var tables = metadata.getTables(catalog(jdbc), schema(jdbc), null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    if (table.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Unable to determine whether the table '" + table + "' is present.", e);
        } finally {
            connection.close();
        }
    }

    // the catalog that the tables are looked up in, unrestricted when the
    // driver doesn't provide one
    private static String catalog(java.sql.Connection connection) {
        try {
            return connection.getCatalog();
        } catch (Exception e) {
            return null;
        }
    }

    // the schema that the tables are looked up in, unrestricted when the
    // driver doesn't provide one
    private static String schema(java.sql.Connection connection) {
        try {
            return connection.getSchema();
        } catch (Exception e) {
            return null;
        }
    }

    public void setup() {
        setup_ = true;
        var styles = styles();
        // the styles aren't restricted to the role that the operations are,
        // so that a page which says something isn't yours to see still looks
        // like every other page
        styleRoute_ = get("/style-" + fingerprint(styles) + ".css", c -> {
            c.setContentType("text/css; charset=utf-8");
            // the fingerprint in the name changes with the content, so the
            // browser asks for a new URL when the styles change and never has
            // to ask for this one again
            c.setHeader("Cache-Control", "public, max-age=31536000, immutable");
            c.print(styles);
        });

        dashboardRoute_ = get("/", new CrudDashboard(this));

        for (var entity : entities_) {
            routes_.put(entity.getSlug(), setupEntity(entity));
        }
    }

    private <T> CrudRoutes setupEntity(CrudEntity<T> entity) {
        var slug = entity.getSlug();
        var identifier = PathInfoHandling.MAP(m -> m.p("crudId", "\\d+"));

        var routes = new CrudRoutes();
        routes.browse = get("/" + slug, new CrudBrowse<>(this, entity));
        routes.add = route("/" + slug + "/add", new CrudAdd<>(this, entity));
        routes.edit = route("/" + slug + "/edit", identifier, new CrudEdit<>(this, entity));
        routes.delete = route("/" + slug + "/delete", identifier, new CrudDelete<>(this, entity));
        if (entity.isOrdered()) {
            routes.move = post("/" + slug + "/move",
                PathInfoHandling.MAP(m -> m.p("crudId", "\\d+").s().p("crudDirection", "up|down")),
                new CrudMove<>(this, entity));
        }
        for (var action : entity.getOptions().getActions()) {
            routes.actions.put(action.getName(),
                route("/" + slug + "/action/" + action.getName(), identifier, new CrudPerform<>(this, entity, action)));
        }
        return routes;
    }

    /**
     * Adds content to the head of every page of this administration.
     * <p>This is where a stylesheet of your own goes, which is all that
     * changing the way the administration looks takes, since the styles that
     * it ships with are written against the custom properties that it
     * declares:
     * <pre>.entity(Article.class)
     *    .head("&lt;link rel=\"stylesheet\" href=\"/css/admin.css\"&gt;")</pre>
     * <p>The content is written into the page as it stands, so it's yours to
     * make sure that it holds what you mean it to hold.
     *
     * @param content the content to add to the head of every page
     * @return this administration instance
     * @since 1.10
     */
    public CrudAdmin head(String content) {
        if (null == content) throw new IllegalArgumentException("content can't be null.");

        verifyChangeable();

        head_.add(content);
        return this;
    }

    String head() {
        return String.join("\n", head_);
    }

    Route styleRoute() {
        return styleRoute_;
    }

    // reads the styles that this administration ships with, which travel in
    // the jar of the framework the way its templates do
    private String styles() {
        // they're read through the class that they sit beside, since a
        // module provides its resources to itself, while a package that is
        // exported without being opened doesn't provide them to anybody else
        try (var stream = CrudAdmin.class.getResourceAsStream("style.css")) {
            if (null == stream) {
                throw new MissingStylesException();
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MissingStylesException(e);
        }
    }

    private static String fingerprint(String content) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256").digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var result = new StringBuilder();
            for (var i = 0; i < 6; i++) {
                result.append(String.format("%02x", digest[i]));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    Datasource datasource() {
        return datasource_;
    }

    // refuses a datasource whose driver has no transactions, asked once and
    // remembered; the driver is asked through a connection of its own, so
    // this isn't done while the administration is being set up, where a
    // database that isn't up yet would keep a site from starting at all
    void verifyTransactions() {
        var transactional = transactional_;
        if (null == transactional) {
            var connection = datasource_.getConnection();
            try {
                transactional = connection.supportsTransactions();
            } finally {
                // a pooled connection is returned to the pool by the level
                // that takes it out next, the way the query manager does it
                if (!datasource_.isPooled()) {
                    connection.close();
                }
            }
            transactional_ = transactional;
        }

        if (!transactional) {
            throw new TransactionsRequiredException(datasource_.getDriver());
        }
    }

    String title() {
        return title_;
    }

    String environment() {
        return environment_;
    }

    Map<String, String> links() {
        return links_;
    }

    /**
     * Retrieves the entities that are registered with this administration.
     *
     * @return the registered entities
     * @since 1.10
     */
    public List<CrudEntity<?>> getEntities() {
        return Collections.unmodifiableList(entities_);
    }

    /**
     * Retrieves the route of the dashboard of this administration.
     *
     * @return the dashboard route
     * @since 1.10
     */
    public Route getDashboardRoute() {
        return dashboardRoute_;
    }

    CrudRoutes routes(CrudEntity<?> entity) {
        return routes_.get(entity.getSlug());
    }

    static class CrudRoutes {
        Route browse;
        Route add;
        Route edit;
        Route delete;
        Route move;
        final Map<String, Route> actions = new LinkedHashMap<>();
    }
}
