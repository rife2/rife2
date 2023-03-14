package rife.bld.operations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
public class TestCleanOperation {
    @Test
    void testInstantiation() {
        var operation = new CleanOperation();
        assertTrue(operation.directories().isEmpty());
    }
}
