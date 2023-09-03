/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestPauseInWhileInterface implements CloneableContinuable, ContinuableSupportAware {
    private ContinuableSupport support_;

    public void setContinuableSupport(ContinuableSupport support) {
        support_ = support;
    }

    public Object clone()
    throws CloneNotSupportedException {
        return super.clone();
    }

    public void execute() {
        var count = 5;

        while (count > 0) {
            support_.pause();
            count--;
        }
        count--;
        support_.pause();
    }
}