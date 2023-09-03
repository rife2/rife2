/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestThrow implements Element {
    public void process(Context c) {
        var template = c.template("engine_continuation_throw");

        c.print(template);
        c.pause();

        var do_throw = c.parameterBoolean("throw", false);

        template.setValue("title", "do throw = " + do_throw);

        try {
            if (do_throw) {
                throw new Exception(" : throw message");
            }
        } catch (Exception e) {
            template.appendValue("title", e.getMessage());
        } finally {
            template.appendValue("title", " : finally message");
        }

        c.print(template);
        c.pause();

        template.appendValue("title", " : all done");
        c.print(template);
    }
}