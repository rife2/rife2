/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestTryCatch implements Element {
    public void process(Context c) {
        var template = c.template("engine_continuation_trycatch");
        try {
            template.setValue("title", "start");
            c.print(template);
            c.pause();

            boolean do_throw = c.parameterBoolean("throw", false);
            if (do_throw) {
                throw new RuntimeException(" : throw done");
            }

            template.appendValue("title", " : throw not done");
            c.print(template);
            c.pause();
        } catch (RuntimeException e) {
            template.appendValue("title", e.getMessage());
            template.appendValue("title", " catch");
            c.print(template);
            c.pause();
        } finally {
            template.appendValue("title", " : finally done");
            c.print(template);
            c.pause();
        }

        template.appendValue("title", " : all done");
        c.print(template);
    }
}