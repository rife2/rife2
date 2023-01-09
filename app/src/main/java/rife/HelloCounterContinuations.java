/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;

public class HelloCounterContinuations extends Site {
    public static class Counter implements Element {
        public void process(Context c) {
            var count = 0;
            while (count++ < 10) {
                c.print(count);
                c.print(" <a href='" + c.urlFor(c.route()) + "'>add</a>");
                c.pause();
            }
            c.print("done");
        }
    }

    Route count = getPost("/count", Counter.class);

    public static void main(String[] args) {
        new Server().start(new HelloCounterContinuations());
    }
}
