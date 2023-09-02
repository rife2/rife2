/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestNullReference implements Element {
    @Override
    public void process(Context c) {
        String string = null;
        c.print(c.continuationId());
        c.pause();
        string.length();
    }
}