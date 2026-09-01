/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

/**
 * The method of a {@code CrudColumnRenderer} will be executed for every cell
 * of its column, to decide what the browse table shows there.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#column(String, CrudColumnRenderer)
 * @see CrudCell
 * @since 1.10
 */
@FunctionalInterface
public interface CrudColumnRenderer<T> {
    /**
     * Renders the cell of one instance.
     *
     * @param instance the instance whose row the cell belongs to
     * @param value    the formatted value that the cell would have shown
     * @return the cell to show; or
     * <p>{@code null} to show the formatted value after all, which lets a
     * renderer only handle the rows that it cares about
     * @since 1.10
     */
    CrudCell render(T instance, String value);
}
