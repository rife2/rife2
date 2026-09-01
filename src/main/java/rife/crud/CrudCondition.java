/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

/**
 * The method of a {@code CrudCondition} will be executed to decide whether
 * an operation applies to a single instance of an entity.
 * <p>A condition is evaluated when the browse page decides which buttons to
 * show for a row, and again when the corresponding operation is submitted,
 * so that an instance that was refused can't be operated on through a
 * forged request.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#deletable(CrudCondition)
 * @see CrudAction#visibleWhen(CrudCondition)
 * @since 1.10
 */
@FunctionalInterface
public interface CrudCondition<T> {
    /**
     * Indicates whether the operation applies to an instance.
     *
     * @param instance the instance to evaluate
     * @return {@code true} when the operation applies; or
     * <p>{@code false} when it doesn't
     * @since 1.10
     */
    boolean accepts(T instance);
}
