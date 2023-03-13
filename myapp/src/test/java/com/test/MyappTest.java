package com.test;

import org.junit.jupiter.api.Test;
import rife.test.MockConversation;

import static org.junit.jupiter.api.Assertions.*;

public class MyappTest {
    @Test
    void verifyRoot() {
        var m = new MockConversation(new MyappSite());
        assertEquals(m.doRequest("/").getStatus(), 302);
    }

    @Test
    void verifyHello() {
        var m = new MockConversation(new MyappSite());
        assertEquals("Hello Myapp", m.doRequest("/hello")
            .getTemplate().getValue("title"));
    }
}
