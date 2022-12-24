/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallAnswerInOtherThreadCallTarget extends AbstractContinuableObject {
    private boolean doAnswer_ = false;

    public void setDoAnswer(boolean doAnswer) {
        doAnswer_ = doAnswer;
    }

    public void execute() {
        if (doAnswer_) {
            answer(Boolean.TRUE);
        }
    }
}