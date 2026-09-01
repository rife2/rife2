/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

/**
 * Glue interface to help building a custom action of an administered
 * entity.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#action(String, String, CrudActionHandler, CrudActionBuilder)
 * @since 1.10
 */
@FunctionalInterface
public interface CrudActionBuilder<T> {
    /**
     * Perform the build of a custom action.
     *
     * @param action an instance of the building DSL.
     * @since 1.10
     */
    void build(CrudAction<T> action);
}
