/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import org.junit.jupiter.api.Test;
import rife.test.MockConversation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloTest {
    @Test void verifyHello() {
        var conversation = new MockConversation(new HelloWorld());
        assertEquals("Hello World", conversation.doRequest("/hello").getText());
    }
}