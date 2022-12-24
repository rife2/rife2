/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestStepBackCounterInterface implements ContinuableObject, ContinuableSupportAware {
    private ContinuableSupport support_;

    public void setContinuableSupport(ContinuableSupport support) {
        support_ = support;
    }

    public Object clone()
    throws CloneNotSupportedException {
        return super.clone();
    }

    private int total_ = -5;

    public int getTotal() {
        return total_;
    }

    private boolean start_ = false;

    public void setStart(boolean start) {
        start_ = start;
    }

    private int answer_;

    public void setAnswer(int answer) {
        answer_ = answer;
    }

    public void execute() {
        var number_of_resumes = 0;

        if (total_ < 0) {
            total_++;
            support_.stepBack();
        }

        support_.pause();
        number_of_resumes++;

        if (start_) {
            support_.pause();
            number_of_resumes++;

            total_ += answer_;

            if (total_ < 50) {
                support_.stepBack();
            }
        }
    }
}