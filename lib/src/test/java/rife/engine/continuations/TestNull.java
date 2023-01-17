/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestNull implements Element {
    public void process(Context c) {
        String response = null;

        c.print("before null pause\n" + c.continuationId());
        c.pause();

        response = c.parameter("response");

        c.print(response);
    }
}
