/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallMultipleAnswersWithTryCatchSource extends AbstractContinuableObject {
    private StringBuffer result_;

    public void execute() {
        result_ = new StringBuffer("before call\n");
        var answer = (String) call(TestCallMultipleAnswersWithTryCatchTarget.class);
        result_.append(answer);
        answer = (String) call(TestCallMultipleAnswersWithTryCatchTarget.class);
        result_.append(answer);
        result_.append("after call");
    }

    public String getResult() {
        return result_.toString();
    }
}