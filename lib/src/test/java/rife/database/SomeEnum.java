/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

public enum SomeEnum {
    VALUE_ONE(1), VALUE_TWO(2), VALUE_THREE(3);

    private int mCount;

    SomeEnum(int count) {
        mCount = count;
    }

    public int getCount() {
        return mCount;
    }
}
