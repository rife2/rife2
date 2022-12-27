/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

/**
 * This interface can be implemented to use with the
 * {@link GenericQueryManager#restore(BeanFetcher)} method.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see GenericQueryManager
 * @since 1.0
 */
@FunctionalInterface
public interface BeanFetcher<BeanType> {
    /**
     * This method will be called with each bean instance that was
     * retrieved with the restore query.
     *
     * @param instance the bean instance that was retrieved
     * @since 1.0
     */
    void gotBeanInstance(BeanType instance);
}