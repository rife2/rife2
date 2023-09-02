/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallSimpleCallInterfaceSource implements CloneableContinuable, ContinuableSupportAware {
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

    private StringBuffer result_;

    public void execute() {
        result_ = new StringBuffer("before call\n");
        var answer = (String) support_.call(TestCallSimpleCallInterfaceTarget1.class);
        result_.append(answer);
        result_.append("after call");
    }

    public String getResult() {
        return result_.toString();
    }
}