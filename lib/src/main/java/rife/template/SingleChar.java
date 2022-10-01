/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

class SingleChar implements ParseCondition {
    private final int expected_;

    SingleChar(int expected) {
        expected_ = expected;
    }

    @Override
    public boolean isValid(int codePoint) {
        return expected_ == codePoint;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }
}
