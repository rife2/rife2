/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.elements;

import rife.engine.Context;
import rife.engine.Element;

/**
 * Standard element that can be placed {@link rife.engine.Router#before before}
 * any other route to indicate to the clients that the content should not be cached.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Context#preventCaching()
 * @since 1.0
 */
public class Uncached implements Element {
    @Override
    public void process(Context c) {
        c.preventCaching();
    }
}