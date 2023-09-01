/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestInnerClass implements Element {
    public void process(Context c) {
        Inner inner = new Inner();
        String before = "before pause";

        c.print(before + "\n" + c.continuationId());
        c.pause();
        c.print(inner.getOutput());
    }

    static class Inner {
        public String getOutput() {
            return "InnerClass's output";
        }
    }
}
