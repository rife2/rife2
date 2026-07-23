/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestPauseCurrentClassFrameMerge extends AbstractContinuableObject {
    private boolean useSelf_;
    private String result_;

    public void execute() {
        Object value;
        if (useSelf_) {
            value = this;
        } else {
            value = new Peer();
        }
        pause();
        result_ = value.getClass().getSimpleName();
    }

    public String getResult() {
        return result_;
    }

    public static class Peer implements Cloneable {
        public Peer clone()
        throws CloneNotSupportedException {
            return (Peer) super.clone();
        }
    }
}
