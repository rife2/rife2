/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestPauseInWhile extends AbstractContinuableObject {
    public void execute() {
        var count = 5;

        while (count > 0) {
            pause();
            count--;
        }
        count--;
        pause();
    }
}