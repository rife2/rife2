/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * Glue interface to help building the pathinfo mapping
 * structure
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
@FunctionalInterface
public interface PathInfoBuilder {
    /**
     * Perform the pathinfo mapping structure build.
     *
     * @param m an instance of the building DSL.
     * @since 1.0
     */
    void build(PathInfoMapping m);
}