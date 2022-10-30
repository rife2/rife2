/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestGroups {
    @Test
    public void testGroup()
    throws Exception {
        try (final var server = new TestServerRunner(new GroupSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("/three", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("/four", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGroupPrefix()
    throws Exception {
        try (final var server = new TestServerRunner(new GroupPrefixSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("/group/three", webClient.getPage("http://localhost:8181/group/three").getWebResponse().getContentAsString());
                assertEquals("/group/four", webClient.getPage("http://localhost:8181/group/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testGroupsMultiLevel()
    throws Exception {
        try (final var server = new TestServerRunner(new GroupsMultiLevelSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("/prefix1/three", webClient.getPage("http://localhost:8181/prefix1/three").getWebResponse().getContentAsString());
                assertEquals("/prefix1/four", webClient.getPage("http://localhost:8181/prefix1/four").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/five", webClient.getPage("http://localhost:8181/prefix1/prefix2/five").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/six", webClient.getPage("http://localhost:8181/prefix1/prefix2/six").getWebResponse().getContentAsString());
                assertEquals("/seven", webClient.getPage("http://localhost:8181/seven").getWebResponse().getContentAsString());
                assertEquals("/eight", webClient.getPage("http://localhost:8181/eight").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/nine", webClient.getPage("http://localhost:8181/prefix1/prefix2/nine").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/ten", webClient.getPage("http://localhost:8181/prefix1/prefix2/ten").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/prefix3/eleven", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/eleven").getWebResponse().getContentAsString());
                assertEquals("/prefix1/prefix2/prefix3/twelve", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/twelve").getWebResponse().getContentAsString());
            }
        }
    }

    // TODO : test path info routes in groups

    @Test
    public void testBefore()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("before1before2/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("before1before2/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testAfter()
    throws Exception {
        try (final var server = new TestServerRunner(new AfterSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/oneafter1after2", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/twoafter1after2", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfter()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("before1before2/oneafter1after2", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("before1before2/twoafter1after2", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeGroup()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeGroupSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("before1before2/three", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("before1before2/four", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testAfterGroup()
    throws Exception {
        try (final var server = new TestServerRunner(new AfterGroupSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("/threeafter1after2", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("/fourafter1after2", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfterGroup()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterGroupSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfterGroupsMultiLevel()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterGroupsMultiLevelSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());

                assertEquals("before1before2/prefix1/three", webClient.getPage("http://localhost:8181/prefix1/three").getWebResponse().getContentAsString());
                assertEquals("before1before2/prefix1/four", webClient.getPage("http://localhost:8181/prefix1/four").getWebResponse().getContentAsString());
                assertEquals("before1before2/prefix1/prefix2/fiveafter1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/five").getWebResponse().getContentAsString());
                assertEquals("before1before2/prefix1/prefix2/sixafter1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/six").getWebResponse().getContentAsString());

                assertEquals("before5/seven", webClient.getPage("http://localhost:8181/seven").getWebResponse().getContentAsString());
                assertEquals("before5/eight", webClient.getPage("http://localhost:8181/eight").getWebResponse().getContentAsString());

                assertEquals("before1before2before3before4/prefix1/prefix2/nineafter1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/nine").getWebResponse().getContentAsString());
                assertEquals("before1before2before3before4/prefix1/prefix2/tenafter1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/ten").getWebResponse().getContentAsString());
                assertEquals("before1before2before3before4/prefix1/prefix2/prefix3/elevenafter3after4after1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/eleven").getWebResponse().getContentAsString());
                assertEquals("before1before2before3before4/prefix1/prefix2/prefix3/twelveafter3after4after1after2", webClient.getPage("http://localhost:8181/prefix1/prefix2/prefix3/twelve").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfterGroupRespond()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterGroupRespondSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());

                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("before1", webClient.getPage("http://localhost:8181/three?respond1=true").getWebResponse().getContentAsString());
                assertEquals("before1before2", webClient.getPage("http://localhost:8181/three?respond2=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/three", webClient.getPage("http://localhost:8181/three?respond3=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three?respond4=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1", webClient.getPage("http://localhost:8181/three?respond5=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three?respond6=true").getWebResponse().getContentAsString());

                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
                assertEquals("before1", webClient.getPage("http://localhost:8181/four?respond1=true").getWebResponse().getContentAsString());
                assertEquals("before1before2", webClient.getPage("http://localhost:8181/four?respond2=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four??respond3=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/four", webClient.getPage("http://localhost:8181/four?respond4=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1", webClient.getPage("http://localhost:8181/four?respond5=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four??respond6=true").getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testBeforeAfterGroupNext()
    throws Exception {
        try (final var server = new TestServerRunner(new BeforeAfterGroupNextSite())) {
            try (final WebClient webClient = new WebClient()) {
                assertEquals("/one", webClient.getPage("http://localhost:8181/one").getWebResponse().getContentAsString());
                assertEquals("/two", webClient.getPage("http://localhost:8181/two").getWebResponse().getContentAsString());

                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three").getWebResponse().getContentAsString());
                assertEquals("before2/threeafter1after2", webClient.getPage("http://localhost:8181/three?next1=true").getWebResponse().getContentAsString());
                assertEquals("before1/threeafter1after2", webClient.getPage("http://localhost:8181/three?next2=true").getWebResponse().getContentAsString());
                assertEquals("before1before2after1after2", webClient.getPage("http://localhost:8181/three?next3=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1after2", webClient.getPage("http://localhost:8181/three?next4=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter2", webClient.getPage("http://localhost:8181/three?next5=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/threeafter1", webClient.getPage("http://localhost:8181/three?next6=true").getWebResponse().getContentAsString());

                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four").getWebResponse().getContentAsString());
                assertEquals("before2/fourafter1after2", webClient.getPage("http://localhost:8181/four?next1=true").getWebResponse().getContentAsString());
                assertEquals("before1/fourafter1after2", webClient.getPage("http://localhost:8181/four?next2=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1after2", webClient.getPage("http://localhost:8181/four??next3=true").getWebResponse().getContentAsString());
                assertEquals("before1before2after1after2", webClient.getPage("http://localhost:8181/four?next4=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter2", webClient.getPage("http://localhost:8181/four?next5=true").getWebResponse().getContentAsString());
                assertEquals("before1before2/fourafter1", webClient.getPage("http://localhost:8181/four?next6=true").getWebResponse().getContentAsString());
            }
        }
    }
}
