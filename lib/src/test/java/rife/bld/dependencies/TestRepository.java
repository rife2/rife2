/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestRepository {
    @Test
    void testInstantiation() {
        var repository1 = new Repository("http://my.repo");
        assertNotNull(repository1);
        assertEquals("http://my.repo", repository1.location());
        assertNull(repository1.username());
        assertNull(repository1.password());

        var repository2 = new Repository("http://your.repo", "user1", "pass1");
        assertNotNull(repository2);
        assertEquals("http://your.repo", repository2.location());
        assertEquals("user1", repository2.username());
        assertEquals("pass1", repository2.password());
    }

    @Test
    void testWithCredentials() {
        var repository1 = new Repository("http://my.repo");
        assertNotNull(repository1);
        assertEquals("http://my.repo", repository1.location());
        assertNull(repository1.username());
        assertNull(repository1.password());

        var repository2 = repository1.withCredentials("user1", "pass1");
        assertNotNull(repository2);
        assertEquals("http://my.repo", repository2.location());
        assertEquals("user1", repository2.username());
        assertEquals("pass1", repository2.password());

        var repository3 = repository1.withCredentials("user2", "pass2");
        assertNotNull(repository3);
        assertEquals("http://my.repo", repository3.location());
        assertEquals("user2", repository3.username());
        assertEquals("pass2", repository3.password());
    }

    @Test
    void testArtifactLocation() {
        var repository1 = new Repository("http://my.repo");
        assertNotNull(repository1);
        assertEquals("http://my.repo/groupId1/artifactId1/", repository1.getArtifactLocation("groupId1", "artifactId1"));

        var repository2 = new Repository("file:///local/repo");
        assertNotNull(repository2);
        assertEquals("/local/repo/groupId2/artifactId2/", repository2.getArtifactLocation("groupId2", "artifactId2"));

        var repository3 = new Repository("/local/repo");
        assertNotNull(repository3);
        assertEquals("/local/repo/groupId3/artifactId3/", repository3.getArtifactLocation("groupId3", "artifactId3"));
    }

    @Test
    void testIsLocal() {
        assertFalse(new Repository("http://my.repo").isLocal());
        assertTrue(new Repository("file:///local/repo").isLocal());
        assertTrue(new Repository("//local/repo").isLocal());
    }
}
