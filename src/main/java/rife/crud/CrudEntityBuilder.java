/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

/**
 * Glue interface to help building the options of an administered entity.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudAdmin#entity(Class, CrudEntityBuilder)
 * @since 1.10
 */
@FunctionalInterface
public interface CrudEntityBuilder<T> {
    /**
     * Perform the options build of an administered entity.
     *
     * @param options an instance of the building DSL.
     * @since 1.10
     */
    void build(CrudEntityOptions<T> options);
}
