/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com> and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.database.DbRowProcessor;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
//import rife.site.ValidationContext;
import java.util.List;

/**
 * A <code>GenericQueryManager</code> provides features that make it easy to
 * persist and retrieve beans in a database with single method calls. An
 * instance of the manager can be obtained by using the {@link
 * GenericQueryManagerFactory} class.
 * <p>Callbacks are also supported to make it possible to interact with the
 * persistence actions in a bean-centric way. More information can be found in
 * the {@link Callbacks} interface.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @see rife.database.querymanagers.generic.GenericQueryManagerFactory
 * @see rife.database.querymanagers.generic.Callbacks
 * @since 1.0
 */
public interface GenericQueryManager<BeanType> // extends ValidationContext
{
    /**
     * Get the handled class
     *
     * @return the handled class
     * @since 1.0
     */
    Class getBaseClass();

    /**
     * Get the managed database table name
     *
     * @return the table name
     * @since 1.0
     */
    String getTable();

    /**
     * Get the name of the property defined as the identifier.
     * <p>Defaults to "id".
     *
     * @return the name of the property defined as the identifier
     * @since 1.0
     */
    String getIdentifierName();

    /**
     * Get the value of the property defined as the identifier.
     * <p>The property defaults to "id".
     *
     * @param bean the bean to retrieve the identifier value for
     * @return the value of the property defined as the identifier
     * @since 1.0
     */
    int getIdentifierValue(BeanType bean)
    throws DatabaseException;

    /**
     * Install the database structure into the database.
     * <p>This method will cause the structure needed to persist the bean to
     * be installed into the database. This includes any validatity checks
     * that the database supports and that have already been defined.
     * Including (but not limited to): length, notNull, notEmpty, etc. etc.
     * This method will fail semi-gracefully if the installation fails.
     * Generally it's best to catch the {@link DatabaseException} and assume
     * that the database is already installed.
     *
     * @see #remove()
     * @since 1.0
     */
    void install()
    throws DatabaseException;

    /**
     * Install the database structure into the database using a custom query.
     * <p>This method will cause the structure needed to persist the bean to
     * be installed into the database using the provided custom query. The
     * custom query is usually obtained by using {@link
     * #getInstallTableQuery()}. This includes any validatity checks that the
     * database supports and that have already been defined. Including (but
     * not limited to): length, notNull, notEmpty, etc. etc. This method will
     * fail semi-gracefully if the installation fails. Generally it's best to
     * catch the {@link DatabaseException} and assume that the database is
     * already installed.
     *
     * @param query the {@link CreateTable} query to use to create the table
     * @see #install()
     * @see #remove()
     * @since 1.0
     */
    void install(CreateTable query)
    throws DatabaseException;

    /**
     * Remove the database structure
     * <p>This method will cause the database structure to be removed from the
     * database deleting all saved beans in the process. No new beans of this
     * type can be persisted until the database structure is reinstalled.
     *
     * @see #install()
     * @since 1.0
     */
    void remove()
    throws DatabaseException;

    /**
     * Persist a bean.
     * <p>This method allows a person to persist a bean to a DB to be later
     * restored. A bean to be persisted must have at least one integer
     * identifier and one bean property both with accessors. If the identifier
     * value is greater than or equal to 0, the bean is attempted to be
     * updated first, if this fails (or the identifier is -1) the bean is
     * assumed to be a new bean and a new sequential identifier is generated
     * by the database.
     *
     * @param bean the bean to be saved
     * @return the identifier assigned to the new/updated bean
     * @since 1.0
     */
    int save(BeanType bean)
    throws DatabaseException;

    /**
     * Insert a bean in the database.
     * <p>This method specifically forces insertion of the bean into the
     * database. This method is only recommended for use when you know what
     * you are doing. The {@link #save(Object bean)} method is safer because
     * it can detect whether to insert or update the bean in that database,
     * leading to safer, simpler code. Bean restrictions mirror {@link
     * #save(Object bean)}.
     *
     * @param bean the bean to be inserted
     * @return the indentier assigned to the new bean
     * @see #save(Object bean)
     * @since 1.0
     */
    int insert(BeanType bean)
    throws DatabaseException;

    /**
     * Update an existing bean in the database.
     * <p>This method specifically forces the updating of the bean into the
     * database. This method is only recommended for use when you know what
     * you are doing. The {@link #save(Object bean)} method is safer because
     * it can detect whether to insert or update the bean in that database,
     * leading to safer, simpler code. Bean restrictions mirror {@link
     * #save(Object bean)}.
     *
     * @param bean the bean to be updated
     * @return the indentier assigned to the new bean
     * @see #save(Object bean)
     * @since 1.0
     */
    int update(BeanType bean)
    throws DatabaseException;

    /**
     * Restore all the beans persisted under this manager.
     * <p>This method will return a {@link List} of all the beans persisted
     * under this manager.
     *
     * @return a {@link List} of all the persisted beans
     * @since 1.0
     */
    List<BeanType> restore()
    throws DatabaseException;

    /**
     * Restore a single bean using the identifier.
     * <p>This method will return a single bean having the provided
     * identifier. Since the identifier is unique, you can be assured of a
     * single bean with a persistent id. This id is never changed under normal
     * circumstances.
     *
     * @param objectId the identifier to identify the bean to restore
     * @return the bean that matches the identifier provided
     * @since 1.0
     */
    BeanType restore(int objectId)
    throws DatabaseException;

    /**
     * Restore all beans using the row processor provided.
     * <p>This method will return all beans using a {@link DbRowProcessor} for
     * reduced memory requirements as opposed to the full {@link List} version
     * of {@link #restore()}.
     *
     * @param rowProcessor the DbRowProcessor each row should be passed to
     * @return true if beans were restored, false if not
     * @see #restore()
     * @since 1.0
     */
    boolean restore(DbRowProcessor rowProcessor)
    throws DatabaseException;

    /**
     * Restore the first bean that matches the {@link RestoreQuery}.
     * <p>This method will return the first bean that matches the {@link
     * RestoreQuery}. Especially useful for selecting the first returned bean
     * from a complex query.
     *
     * @param query the query the bean should be restored from
     * @return the first bean that matches the {@link RestoreQuery}
     * @see #restore(RestoreQuery)
     * @since 1.0
     */
    BeanType restoreFirst(RestoreQuery query)
    throws DatabaseException;

    /**
     * Restore a list of beans that match the provided {@link RestoreQuery}.
     * <p>This method will return a list of beans that match the provided
     * {@link RestoreQuery}. This can be used for more complex queries, or for
     * exclusion of certain beans from the results.
     *
     * @param query the query the beans should be restored from
     * @return a list containing all the restored beans
     * @see #restore()
     * @since 1.0
     */
    List<BeanType> restore(RestoreQuery query)
    throws DatabaseException;

    /**
     * Restore a list of beans that match the provided {@link RestoreQuery}
     * and process with the {@link DbRowProcessor}.
     * <p>This method will return a list of beans that match the provided
     * RestoreQuery and process these matches with the provided {@link
     * DbRowProcessor}. This can be used for more memory-intensive (or larger
     * result sets) complex queries or for the exclusion of certain beans from
     * the results.
     *
     * @param query        the query the beans should be restored from
     * @param rowProcessor the row processor that should be used to process
     *                     each matched bean row
     * @return true if beans were processed, false if not
     * @since 1.0
     */
    boolean restore(RestoreQuery query, DbRowProcessor rowProcessor)
    throws DatabaseException;

    /**
     * Get the query that would be used to install the table.
     * <p>This method will return the query that would be used to install the
     * database structure. Can be used to modify the structure if i custom
     * structure is needed. Mostly likely to be passed into {@link
     * #install(CreateTable)}
     *
     * @return the query that would be used to install the database structure
     * @since 1.0
     */
    CreateTable getInstallTableQuery()
    throws DatabaseException;

    /**
     * Get the base query used to restore beans.
     * <p>This method will return the base query that would be used to restore
     * beans with {@link #restore()}. This can be used to restrict the query
     * so that less beans are returned or certain beans are returned.
     *
     * @return the query that would be used to restore a number of beans
     * @since 1.0
     */
    RestoreQuery getRestoreQuery();

    /**
     * Get the base query used to restore a single identifed bean.
     * <p>This method will return the base query that would be used to restore
     * a single bean with {@link #restore(int)}. This can be used to restrict
     * the query so that a bean not matching the query will not be returned.
     *
     * @return the query that would be used to restore a single identified
     * bean
     * @since 1.0
     */
    RestoreQuery getRestoreQuery(int objectId);

    /**
     * Count the number of beans persisted.
     * <p>This method will count the total number of beans persisted under
     * this manager.
     *
     * @return the number of beans persisted under this manager
     * @since 1.0
     * @since 1.0
     */
    int count()
    throws DatabaseException;

    /**
     * Count the number of beans persisted with a custom {@link CountQuery}.
     * <p>This method will count the total number of beans persisted under
     * this manager that match the provided {@link CountQuery}.
     *
     * @param query the query that will be used to determine which beans to
     *              count
     * @return the number of beans persisted under this manager that match the
     * provided query
     * @since 1.0
     */
    int count(CountQuery query)
    throws DatabaseException;

    /**
     * Get the base {@link CountQuery} used to count the number of beans
     * persisted under this manager
     *
     * @return the query that would be used to count the total number of beans
     * persisted under this managerù
     * @since 1.0
     */
    CountQuery getCountQuery();

    /**
     * Delete a single identified bean
     * <p>This method will delete the bean identifed by the passed in
     * identifier.
     *
     * @param objectId the identifier of the bean
     * @return true if the deletion suceeded, false if it did not
     * @since 1.0
     */
    boolean delete(int objectId)
    throws DatabaseException;

    /**
     * Delete beans selected by the passed in {@link DeleteQuery}
     * <p>This method will delete all beans identified by the passed in {@link
     * DeleteQuery}.
     *
     * @param query the query to select the beans
     * @return true if the deletion suceeded, false if it did not
     * @since 1.0
     */
    boolean delete(DeleteQuery query)
    throws DatabaseException;

    /**
     * Return the base {@link DeleteQuery} that would be used to delete beans
     *
     * @return the base {@link DeleteQuery}
     * @since 1.0
     */
    DeleteQuery getDeleteQuery();

    /**
     * Return the base {@link DeleteQuery} that would be used to delete a
     * single bean
     *
     * @param objectId the identifier to fill into the base {@link DeleteQuery}
     * @return the base {@link DeleteQuery}
     * @since 1.0
     */
    DeleteQuery getDeleteQuery(int objectId);

    /**
     * Add the listener to the manager to get notifications when actions
     * were successful.
     *
     * @param listener the listener that has to be added
     * @since 1.5
     */
    void addListener(GenericQueryManagerListener<BeanType> listener);

    /**
     * Remove all the listeners that are registered to the manager.
     *
     * @since 1.5
     */
    void removeListeners();

    /**
     * Create a new generic query manager of the same kind but for another
     * bean class.
     *
     * @param beanClass the class of the bean for which the new generic query
     *                  manager has to be created
     * @return a new generic query manager instance
     * @since 1.6
     */
    <OtherBeanType> GenericQueryManager<OtherBeanType> createNewManager(Class<OtherBeanType> beanClass);
}
