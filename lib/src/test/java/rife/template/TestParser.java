/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import org.junit.jupiter.api.Test;

public class TestParser {
    @Test
    public void testInstantiation() {
        Parser p = new Parser();
        p.parse("<!--  V   identifier  /-->");
    }
}