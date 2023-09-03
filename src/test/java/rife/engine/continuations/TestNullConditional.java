/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestNullConditional implements Element {
    public void process(Context c) {
        String value = null;
        if (c.hasParameterValue("value")) {
            value = c.parameter("value");
        }

        c.print(value);
        c.print(c.continuationId());
        c.pause();

        c.print(value);
    }
}
