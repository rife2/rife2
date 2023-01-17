/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallSimpleCallTarget2 extends AbstractContinuableObject {
    public void execute() {
        var answer = "during call target 2\n";
        answer(answer);
    }
}