/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDatabaseContentDisposition {
    @Test
    void testInlineDisposition() {
        assertEquals("inline; filename=\"Field recording.wav\"", DatabaseContent.inlineDisposition("Field recording.wav"));
        assertEquals("inline; filename=\"a\\\"b\\\\c;d.txt\"", DatabaseContent.inlineDisposition("a\"b\\c;d.txt"));
        assertEquals("inline; filename=\"_t_.png\"; filename*=UTF-8''%C3%A9t%C3%A9.png",
            DatabaseContent.inlineDisposition("été.png"));
    }
}
