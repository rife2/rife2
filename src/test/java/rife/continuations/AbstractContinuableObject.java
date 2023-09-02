/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public abstract class AbstractContinuableObject extends ContinuableSupport implements CloneableContinuable {
    @Override
    public Object clone()
    throws CloneNotSupportedException {
        return super.clone();
    }
}