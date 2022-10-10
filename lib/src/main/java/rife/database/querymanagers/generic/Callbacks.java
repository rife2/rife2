/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

/**
 * Callbacks are hooks that are being called when beans are manipulated
 * through the {@link
 * rife.database.querymanagers.generic.GenericQueryManager} or other
 * query managers that are based on it ({@link
 * rife.cmf.dam.ContentQueryManager}, for instance). They can either
 * be implemented directly by implementing this interface, or they can be
 * provided by a bean by implementing the {@link
 * rife.database.querymanagers.generic.CallbacksProvider} interface.
 * <p>This can for example be used to delete associated and dependent objects
 * when delete is called (by implementing {@link #beforeDelete(int)}), or to
 * clear a cache when an object has been modified (by implementing {@link
 * #afterSave(Object, boolean)} and {@link #afterDelete(int, boolean)}).
 * <p>The return value of callbacks can be used to cancel actions. When the
 * <code>before*</code> callbacks return <code>false</code>, the associated
 * actions are cancelled. When the <code>after*</code> callbacks return
 * <code>false</code>, the execution of the action is interrupted at that step
 * and no further callbacks will be called.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @see rife.database.querymanagers.generic.GenericQueryManager
 * @since 1.0
 */
public interface Callbacks<BeanType> {
    /**
     * Is called before {@link
     * rife.database.querymanagers.generic.GenericQueryManager#validate(Validated)}.
     *
     * @param object the bean instance that will be validated
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.3
     */
    boolean beforeValidate(BeanType object);

    /**
     * Is called before {@link
     * rife.database.querymanagers.generic.GenericQueryManager#insert(Object)},
     * or in the beginning of {@link
     * rife.database.querymanagers.generic.GenericQueryManager#save(Object)}
     * if a new bean is being saved.
     *
     * @param object the bean instance that will be inserted
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean beforeInsert(BeanType object);

    /**
     * Is called before {@link
     * rife.database.querymanagers.generic.GenericQueryManager#delete(int)}.
     *
     * @param objectId the id of the bean that will be deleted
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean beforeDelete(int objectId);

    /**
     * Is called before {@link
     * rife.database.querymanagers.generic.GenericQueryManager#save(Object)}.
     *
     * @param object the bean instance that will be saved
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean beforeSave(BeanType object);

    /**
     * Is called before {@link
     * rife.database.querymanagers.generic.GenericQueryManager#update(Object)},
     * or in the beginning of {@link
     * rife.database.querymanagers.generic.GenericQueryManager#save(Object)}
     * if an existing bean is being saved.
     *
     * @param object the bean instance that will be updated
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean beforeUpdate(BeanType object);

    /**
     * Is called after {@link
     * rife.database.querymanagers.generic.GenericQueryManager#validate(Validated)}.
     *
     * @param object the bean instance that was validated
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.3
     */
    boolean afterValidate(BeanType object);

    /**
     * Is called after {@link
     * rife.database.querymanagers.generic.GenericQueryManager#insert(Object)},
     * or at the end of {@link
     * rife.database.querymanagers.generic.GenericQueryManager#save(Object)}
     * if a new bean was saved.
     *
     * @param object  the bean instance that was inserted
     * @param success <code>true</code> if the insert was successful; or
     *                <p><code>false</code> otherwise
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean afterInsert(BeanType object, boolean success);

    /**
     * Is called after {@link
     * rife.database.querymanagers.generic.GenericQueryManager#delete(int)}.
     *
     * @param objectId the id of the bean instance that was deleted
     * @param success  <code>true</code> if the delete was successful; or
     *                 <p><code>false</code> otherwise
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean afterDelete(int objectId, boolean success);

    /**
     * Is called after {@link
     * rife.database.querymanagers.generic.GenericQueryManager#save(Object)}.
     *
     * @param object  the bean instance that was saved
     * @param success <code>true</code> if the save was successful; or
     *                <p><code>false</code> otherwise
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean afterSave(BeanType object, boolean success);

    /**
     * Is called after {@link
     * rife.database.querymanagers.generic.GenericQueryManager#update(Object)},
     * or at the end of {@link
     * rife.database.querymanagers.generic.GenericQueryManager#save(Object)}
     * if an existing bean was saved.
     *
     * @param object  the bean instance that was updated
     * @param success <code>true</code> if the update was successful; or
     *                <p><code>false</code> otherwise
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean afterUpdate(BeanType object, boolean success);

    /**
     * Is called after {@link
     * rife.database.querymanagers.generic.GenericQueryManager#restore(int)}
     * and {@link
     * rife.database.querymanagers.generic.GenericQueryManager#restoreFirst(RestoreQuery)},
     * and for every instance restored during {@link
     * rife.database.querymanagers.generic.GenericQueryManager#restore()}
     * and {@link
     * rife.database.querymanagers.generic.GenericQueryManager#restore(RestoreQuery)}.
     *
     * @param object the bean instance that was restored
     * @return <code>true</code> if the execution should continue as normal;
     * or
     * <p><code>false</code> if the execution should be interrupted
     * @since 1.0
     */
    boolean afterRestore(BeanType object);
}
