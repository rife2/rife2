/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallSimpleCallTarget1 extends AbstractContinuableObject {
    public void execute() {
        var answer = "during call target 1\n" + call(TestCallSimpleCallTarget2.class);
        answer(answer);
    }
}