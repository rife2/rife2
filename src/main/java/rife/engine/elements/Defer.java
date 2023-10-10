/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.elements;

import rife.engine.Context;
import rife.engine.Element;

/**
 * Standard element that allows specific routes to always defer
 * execution to the servlet container.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Context#defer()
 * @since 1.0
 */
public class Defer implements Element {
    public void process(Context c)
    throws Exception {
        c.defer();
    }
}