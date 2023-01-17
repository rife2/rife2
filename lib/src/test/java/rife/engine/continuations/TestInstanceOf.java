/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestInstanceOf implements Element {
    public void process(Context c) {
        String before = "before instanceof pause";
        Object after = "after instanceof pause";

        c.print(before + "\n" + c.continuationId());
        c.pause();

        String after_string = null;
        if (after instanceof String) {
            after_string = (String) after;
        }

        c.print(after_string);
    }
}
