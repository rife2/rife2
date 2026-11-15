/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestFinishedPause implements Element {
    private int runs_ = 0;

    public void process(Context c) {
        runs_++;
        c.print("before " + runs_ + "\n" + c.continuationId());
        c.pause();
        c.print("after " + runs_ + "\n" + c.continuationId());
    }
}
