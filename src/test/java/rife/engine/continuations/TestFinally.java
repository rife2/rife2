/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestFinally implements Element {
    @Override
    public void process(Context c) {
        var test = "start";
        var nl = "\n";
        c.print(test + "\n");
        c.print(c.continuationId());
        c.pause();
        try {
            c.print("try" + nl);
            c.print(c.continuationId());
            c.pause();
            throw new RuntimeException();
        } catch (RuntimeException e) {
            c.print("catch" + nl);
            c.print(c.continuationId());
            c.pause();
        } finally {
            String empty = "";
            c.print("fi" + empty + "nal" + empty + "ly" + nl);
            c.print(c.continuationId());
            c.pause();
        }

        test = "after finally";
        c.print(test);
    }
}