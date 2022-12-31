/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestUniqueIDGenerator {
    @Test
    void testGeneration() {
        var uid = UniqueIDGenerator.generate();
        assertNotNull(uid);
        assertNotNull(uid.toString());
        assertTrue(uid.toString().length() > 0);
    }

    @Test
    void testUniqueness() {
        var uid1 = UniqueIDGenerator.generate();
        var uid2 = UniqueIDGenerator.generate();
        var uid3 = UniqueIDGenerator.generate();
        var uid4 = UniqueIDGenerator.generate();
        assertNotNull(uid1);
        assertNotNull(uid2);
        assertNotNull(uid3);
        assertNotNull(uid4);
        assertArrayEquals(uid1.getID(), uid1.getID());
        assertFalse(Arrays.equals(uid1.getID(), uid2.getID()));
        assertFalse(Arrays.equals(uid1.getID(), uid3.getID()));
        assertFalse(Arrays.equals(uid1.getID(), uid4.getID()));
        assertArrayEquals(uid2.getID(), uid2.getID());
        assertFalse(Arrays.equals(uid2.getID(), uid3.getID()));
        assertFalse(Arrays.equals(uid2.getID(), uid4.getID()));
        assertArrayEquals(uid3.getID(), uid3.getID());
        assertFalse(Arrays.equals(uid3.getID(), uid4.getID()));
        assertArrayEquals(uid4.getID(), uid4.getID());
        var uid1_string = uid1.toString();
        var uid2_string = uid2.toString();
        var uid3_string = uid3.toString();
        var uid4_string = uid4.toString();
        assertEquals(0, uid1_string.compareTo(uid1_string));
        assertTrue(0 != uid1_string.compareTo(uid2_string));
        assertTrue(0 != uid1_string.compareTo(uid3_string));
        assertTrue(0 != uid1_string.compareTo(uid4_string));
        assertEquals(0, uid2_string.compareTo(uid2_string));
        assertTrue(0 != uid2_string.compareTo(uid3_string));
        assertTrue(0 != uid2_string.compareTo(uid4_string));
        assertEquals(0, uid3_string.compareTo(uid3_string));
        assertTrue(0 != uid3_string.compareTo(uid4_string));
        assertEquals(0, uid4_string.compareTo(uid4_string));
    }
}
