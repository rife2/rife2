/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestMockCookie {
    @Test
    void testConstructor() {
        var mock = new MockCookie("name", "value");
        assertEquals(mock.getName(), "name");
        assertEquals(mock.getValue(), "value");
    }

    @Test
    void testConstructorCookie() {
        var cookie1 = new Cookie("name1", "value1");

        var mock1 = new MockCookie(cookie1);
        assertEquals(mock1.getName(), cookie1.getName());
        assertEquals(mock1.getValue(), cookie1.getValue());
        assertEquals(mock1.getComment(), cookie1.getComment());
        assertEquals(mock1.getDomain(), cookie1.getDomain());
        assertEquals(mock1.getMaxAge(), cookie1.getMaxAge());
        assertEquals(mock1.getPath(), cookie1.getPath());
        assertEquals(mock1.getSecure(), cookie1.getSecure());
        assertEquals(mock1.getVersion(), cookie1.getVersion());
        assertEquals(mock1.isHttpOnly(), cookie1.isHttpOnly());

        var cookie2 = new Cookie("name2", "value2");
        cookie2.setComment("comment2");
        cookie2.setDomain("domain2");
        cookie2.setMaxAge(1234);
        cookie2.setPath("path2");
        cookie2.setSecure(true);
        cookie2.setVersion(2);
        cookie2.setHttpOnly(true);

        var mock2 = new MockCookie(cookie2);
        assertEquals(mock2.getName(), cookie2.getName());
        assertEquals(mock2.getValue(), cookie2.getValue());
        assertEquals(mock2.getComment(), cookie2.getComment());
        assertEquals(mock2.getDomain(), cookie2.getDomain());
        assertEquals(mock2.getMaxAge(), cookie2.getMaxAge());
        assertEquals(mock2.getPath(), cookie2.getPath());
        assertEquals(mock2.getSecure(), cookie2.getSecure());
        assertEquals(mock2.getVersion(), cookie2.getVersion());
        assertEquals(mock2.isHttpOnly(), cookie2.isHttpOnly());
    }

    @Test
    void testExpiration()
    throws InterruptedException {
        var cookie1 = new Cookie("name1", "value1");
        assertFalse(new MockCookie(cookie1).isExpired());

        var cookie2 = new Cookie("name2", "value2");
        cookie2.setMaxAge(0);
        assertTrue(new MockCookie(cookie2).isExpired());

        var cookie3 = new Cookie("name3", "value3");
        cookie3.setMaxAge(1);
        var mock3 = new MockCookie(cookie3);
        assertFalse(mock3.isExpired());
        Thread.sleep(1000);
        assertTrue(mock3.isExpired());

        var cookie4 = new Cookie("name4", "value4");
        cookie4.setMaxAge(4);
        var mock4 = new MockCookie(cookie4);
        assertFalse(mock4.isExpired());
        Thread.sleep(1000);
        assertFalse(mock4.isExpired());
        Thread.sleep(3000);
        assertTrue(mock4.isExpired());
    }
}
