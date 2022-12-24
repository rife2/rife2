/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestSimplePause implements Element {
    public void process(Context c)
    throws Exception {
        var before = "before simple pause";
        var after = "after simple pause";

        c.print(before + "\n" + c.continuationId());
        c.pause();
        c.print(after);
    }
}
