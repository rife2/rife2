/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestConditional implements Element {
    public void process(Context c) {
        var stop = c.parameterBoolean("stop", false);
        var answer = c.parameterBoolean("answer", false);

        var template = c.template("engine_continuation_conditional");
        if (stop) {
            template.setValue("title", "stopping");
            c.print(template);
            return;
        }

        if (answer) {
            template.setValue("title", "pausing");
            c.print(template);
            c.pause();
        }

        template.appendValue("title", "printing");
        c.print(template);
    }
}