/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallAnswerInOtherThreadCallInterfaceSource implements ContinuableObject, ContinuableSupportAware {
    private ContinuableSupport support_;

    public void setContinuableSupport(ContinuableSupport support) {
        support_ = support;
    }

    public Object clone()
    throws CloneNotSupportedException {
        return super.clone();
    }

    private StringBuffer result_;

    public void execute() {
        result_ = new StringBuffer("before call\n");
        Boolean answer = (Boolean) support_.call(TestCallAnswerInOtherThreadCallInterfaceTarget.class);
        result_.append(answer);
        result_.append("\nafter call");
    }

    public String getResult() {
        return result_.toString();
    }
}