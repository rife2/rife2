/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestPrivateMethod implements Element {
    public int getInt() {
        return 1234;
    }

    @Override
    public void process(Context c) {
        int result = getInt();

        c.print(c.continuationId());
        c.pause();
        c.print(result);
    }
}
