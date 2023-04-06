/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

/**
 * The methods of a {@code GenericQueryManagerListener} will be executed
 * as the corresponding actions are successfully executed through the
 * {@code GenericQueryManager} that this listener is registered with.
 * <p>The difference with {@code Callbacks} is that listeners are
 * associated with a {@code GenericQueryManager} and
 * {@code Callbacks} are associated with your domain model. Listeners
 * are also only called as a notification mechanisms, they don't allow you to
 * intervene in the execution flow. Listeners are called before 'after'
 * callbacks.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.database.querymanagers.generic.GenericQueryManager
 * @see rife.database.querymanagers.generic.Callbacks
 * @since 1.0
 */
public interface GenericQueryManagerListener<BeanType> {
    /**
     * Executed when the database structure has been successfully installed.
     *
     * @since 1.0
     */
    void installed();

    /**
     * Executed when the database structure has been successfully removed.
     *
     * @since 1.0
     */
    void removed();

    /**
     * Executed when a bean was successfully inserted.
     *
     * @param bean the bean that was inserted
     * @since 1.0
     */
    void inserted(BeanType bean);

    /**
     * Executed when a bean was successfully updated.
     *
     * @param bean the bean that was updated
     * @since 1.0
     */
    void updated(BeanType bean);

    /**
     * Executed when a bean was successfully restored.
     *
     * @param bean the bean that was restored
     * @since 1.0
     */
    void restored(BeanType bean);

    /**
     * Executed when a bean was successfully deleted.
     *
     * @param objectId the identifier of the bean that was deleted
     * @since 1.0
     */
    void deleted(int objectId);
}
