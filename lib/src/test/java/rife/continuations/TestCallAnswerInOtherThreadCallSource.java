/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallAnswerInOtherThreadCallSource extends AbstractContinuableObject {
    private StringBuffer result_;

    public void execute() {
        result_ = new StringBuffer("before call\n");
        var answer = (Boolean) call(TestCallAnswerInOtherThreadCallTarget.class);
        result_.append(answer);
        result_.append("\nafter call");
    }

    public String getResult() {
        return result_.toString();
    }
}