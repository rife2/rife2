/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestParameterFrameMerge extends AbstractContinuableObject {
    private String result_;

    public void execute(String value, int assignment) {
        if (assignment == 1) {
            value = "assigned";
        } else if (assignment == 2) {
            value = null;
        }
        pause();
        result_ = value;
    }

    public String getResult() {
        return result_;
    }
}
