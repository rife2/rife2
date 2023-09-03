/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestMemberMethod implements Element {
    public String createString(String first, String second, long number) {
        return first + " " + second + " " + number;
    }

    public void process(Context c) {
        var string_buffer1 = new StringBuilder(createString("some", "value", 6899L));

        c.print("before pause" + "\n" + c.continuationId());
        c.pause();

        c.print(string_buffer1.substring(2));
    }
}
