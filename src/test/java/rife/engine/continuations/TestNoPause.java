/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestNoPause implements Element {
    @Override
    public void process(Context c) {
        var before = "before simple pause";
        assert before != null;
        var after = "after simple pause";
        assert after != null;

        c.print(c.continuationId());
    }
}
