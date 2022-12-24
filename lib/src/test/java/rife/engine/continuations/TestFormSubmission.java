/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestFormSubmission implements Element {
    public void process(Context c) {
        var template = c.template("engine_continuation_form_submission");

        int total = 0;
        while (total < 50) {
            template.setValue("subtotal", total);
            c.print(template);
            c.pause();
            total += c.parameterInt("answer", 0);
        }

        c.print("got a total of " + total);
    }
}
