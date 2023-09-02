/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallAnswerInOtherThreadCallInterfaceTarget implements CloneableContinuable, ContinuableSupportAware {
    private ContinuableSupport support_;

    @Override
    public void setContinuableSupport(ContinuableSupport support) {
        support_ = support;
    }

    @Override
    public Object clone()
    throws CloneNotSupportedException {
        return super.clone();
    }

    private boolean doAnswer_ = false;

    public void setDoAnswer(boolean doAnswer) {
        doAnswer_ = doAnswer;
    }

    public void execute() {
        if (doAnswer_) {
            support_.answer(Boolean.TRUE);
        }
    }
}