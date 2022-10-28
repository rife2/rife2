/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIntegerUtils {
    @Test
    public void testIntToBytes() {
        assertEquals(265325803, IntegerUtils.bytesToInt(IntegerUtils.intToBytes(265325803)));
    }

}
