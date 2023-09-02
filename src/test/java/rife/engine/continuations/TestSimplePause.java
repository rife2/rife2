/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestSimplePause implements Element {
    @Override
    public void process(Context c) {
        var before = "before simple pause";
        var after = "after simple pause";

        c.print(before + "\n" + c.continuationId());
        c.pause();
        c.print(after);
    }
}
