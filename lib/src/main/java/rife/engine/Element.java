/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * Interface describing the functionality of a RIFE2 element of execution.
 * <p>
 * Elements can be created as lambda expression instances for concise definition,
 * or as child classes which provides much advanced functionalities and reuse.
 * <p>
 * Lambda expression instances will use {@link RouteInstance} routes and
 * element classes will use {@link RouteClass} routes.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 2.0
 */
@FunctionalInterface
public interface Element {
    /**
     * Process the provided <code>Context</code> with this element.
     *
     * @param c the provided request/response context
     * @throws Exception when an error occurs
     */
    void process(Context c) throws Exception;
}
