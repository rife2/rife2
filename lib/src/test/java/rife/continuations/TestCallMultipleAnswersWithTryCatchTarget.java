/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallMultipleAnswersWithTryCatchTarget extends AbstractContinuableObject {
    private static int whichAnswer_ = 1;

    public void execute() {
        if (1 == whichAnswer_) {
            whichAnswer_ = 2;

            try {
                var answer = "during call target 1\n";
                answer(answer);
            } catch (Error e) {
                throw new RuntimeException(e);
            }
        } else if (2 == whichAnswer_) {
            try {
                var answer = "during call target 2\n";
                answer(answer);
            } catch (Error e) {
                throw new RuntimeException(e);
            }
        }
    }
}