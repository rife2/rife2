/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestContentRepository {
    @Test
    void testInstantiation() {
        var repository = new ContentRepository();
        assertNotNull(repository);

        assertNull(repository.getName());
    }

    @Test
    void testName() {
        var repository = new ContentRepository();
        repository.setName("anotherone");
        assertEquals("anotherone", repository.getName());
        repository.name("stillonemore");
        assertEquals("stillonemore", repository.getName());
        repository.setName(null);
        assertNull(repository.getName());
    }

    @Test
    void testValidation() {
        var repository = new ContentRepository();

        repository.resetValidation();
        assertFalse(repository.validate());
        assertFalse(repository.isSubjectValid("name"));

        repository.resetValidation();
        repository.setName("anotherone");
        assertTrue(repository.validate());
        assertTrue(repository.isSubjectValid("name"));

        repository.resetValidation();
        assertTrue(repository.validate());
        assertTrue(repository.isSubjectValid("name"));
    }
}
