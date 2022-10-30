/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import org.junit.jupiter.api.Test;
import rife.engine.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMocksGroups {
    @Test
    public void testGroup() {
        var conversation = new MockConversation(new GroupSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());
        assertEquals("/three", conversation.doRequest("/three").getText());
        assertEquals("/four", conversation.doRequest("/four").getText());
    }

    @Test
    public void testGroupPrefix() {
        var conversation = new MockConversation(new GroupPrefixSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());
        assertEquals("/group/three", conversation.doRequest("/group/three").getText());
        assertEquals("/group/four", conversation.doRequest("/group/four").getText());

    }

    @Test
    public void testGroupsMultiLevel() {
        var conversation = new MockConversation(new GroupsMultiLevelSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());
        assertEquals("/prefix1/three", conversation.doRequest("/prefix1/three").getText());
        assertEquals("/prefix1/four", conversation.doRequest("/prefix1/four").getText());
        assertEquals("/prefix1/prefix2/five", conversation.doRequest("/prefix1/prefix2/five").getText());
        assertEquals("/prefix1/prefix2/six", conversation.doRequest("/prefix1/prefix2/six").getText());
        assertEquals("/seven", conversation.doRequest("/seven").getText());
        assertEquals("/eight", conversation.doRequest("/eight").getText());
        assertEquals("/prefix1/prefix2/nine", conversation.doRequest("/prefix1/prefix2/nine").getText());
        assertEquals("/prefix1/prefix2/ten", conversation.doRequest("/prefix1/prefix2/ten").getText());
        assertEquals("/prefix1/prefix2/prefix3/eleven", conversation.doRequest("/prefix1/prefix2/prefix3/eleven").getText());
        assertEquals("/prefix1/prefix2/prefix3/twelve", conversation.doRequest("/prefix1/prefix2/prefix3/twelve").getText());

    }

    // TODO : test path info routes in groups

    @Test
    public void testBefore() {
        var conversation = new MockConversation(new BeforeSite());
        assertEquals("before1before2/one", conversation.doRequest("/one").getText());
        assertEquals("before1before2/two", conversation.doRequest("/two").getText());
    }

    @Test
    public void testAfter() {
        var conversation = new MockConversation(new AfterSite());
        assertEquals("/oneafter1after2", conversation.doRequest("/one").getText());
        assertEquals("/twoafter1after2", conversation.doRequest("/two").getText());
    }

    @Test
    public void testBeforeAfter() {
        var conversation = new MockConversation(new BeforeAfterSite());
        assertEquals("before1before2/oneafter1after2", conversation.doRequest("/one").getText());
        assertEquals("before1before2/twoafter1after2", conversation.doRequest("/two").getText());
    }

    @Test
    public void testBeforeGroup() {
        var conversation = new MockConversation(new BeforeGroupSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());
        assertEquals("before1before2/three", conversation.doRequest("/three").getText());
        assertEquals("before1before2/four", conversation.doRequest("/four").getText());
    }

    @Test
    public void testAfterGroup() {
        var conversation = new MockConversation(new AfterGroupSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());
        assertEquals("/threeafter1after2", conversation.doRequest("/three").getText());
        assertEquals("/fourafter1after2", conversation.doRequest("/four").getText());
    }

    @Test
    public void testBeforeAfterGroup() {
        var conversation = new MockConversation(new BeforeAfterGroupSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());
        assertEquals("before1before2/threeafter1after2", conversation.doRequest("/three").getText());
        assertEquals("before1before2/fourafter1after2", conversation.doRequest("/four").getText());
    }

    @Test
    public void testBeforeAfterGroupsMultiLevel() {
        var conversation = new MockConversation(new BeforeAfterGroupsMultiLevelSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());

        assertEquals("before1before2/prefix1/three", conversation.doRequest("/prefix1/three").getText());
        assertEquals("before1before2/prefix1/four", conversation.doRequest("/prefix1/four").getText());
        assertEquals("before1before2/prefix1/prefix2/fiveafter1after2", conversation.doRequest("/prefix1/prefix2/five").getText());
        assertEquals("before1before2/prefix1/prefix2/sixafter1after2", conversation.doRequest("/prefix1/prefix2/six").getText());

        assertEquals("before5/seven", conversation.doRequest("/seven").getText());
        assertEquals("before5/eight", conversation.doRequest("/eight").getText());

        assertEquals("before1before2before3before4/prefix1/prefix2/nineafter1after2", conversation.doRequest("/prefix1/prefix2/nine").getText());
        assertEquals("before1before2before3before4/prefix1/prefix2/tenafter1after2", conversation.doRequest("/prefix1/prefix2/ten").getText());
        assertEquals("before1before2before3before4/prefix1/prefix2/prefix3/elevenafter3after4after1after2", conversation.doRequest("/prefix1/prefix2/prefix3/eleven").getText());
        assertEquals("before1before2before3before4/prefix1/prefix2/prefix3/twelveafter3after4after1after2", conversation.doRequest("/prefix1/prefix2/prefix3/twelve").getText());
    }

    @Test
    public void testBeforeAfterGroupRespond() {
        var conversation = new MockConversation(new BeforeAfterGroupRespondSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());

        assertEquals("before1before2/threeafter1after2", conversation.doRequest("/three").getText());
        assertEquals("before1", conversation.doRequest("/three?respond1=true").getText());
        assertEquals("before1before2", conversation.doRequest("/three?respond2=true").getText());
        assertEquals("before1before2/three", conversation.doRequest("/three?respond3=true").getText());
        assertEquals("before1before2/threeafter1after2", conversation.doRequest("/three?respond4=true").getText());
        assertEquals("before1before2/threeafter1", conversation.doRequest("/three?respond5=true").getText());
        assertEquals("before1before2/threeafter1after2", conversation.doRequest("/three?respond6=true").getText());

        assertEquals("before1before2/fourafter1after2", conversation.doRequest("/four").getText());
        assertEquals("before1", conversation.doRequest("/four?respond1=true").getText());
        assertEquals("before1before2", conversation.doRequest("/four?respond2=true").getText());
        assertEquals("before1before2/fourafter1after2", conversation.doRequest("/four??respond3=true").getText());
        assertEquals("before1before2/four", conversation.doRequest("/four?respond4=true").getText());
        assertEquals("before1before2/fourafter1", conversation.doRequest("/four?respond5=true").getText());
        assertEquals("before1before2/fourafter1after2", conversation.doRequest("/four??respond6=true").getText());
    }

    @Test
    public void testBeforeAfterGroupNext() {
        var conversation = new MockConversation(new BeforeAfterGroupNextSite());
        assertEquals("/one", conversation.doRequest("/one").getText());
        assertEquals("/two", conversation.doRequest("/two").getText());

        assertEquals("before1before2/threeafter1after2", conversation.doRequest("/three").getText());
        assertEquals("before2/threeafter1after2", conversation.doRequest("/three?next1=true").getText());
        assertEquals("before1/threeafter1after2", conversation.doRequest("/three?next2=true").getText());
        assertEquals("before1before2after1after2", conversation.doRequest("/three?next3=true").getText());
        assertEquals("before1before2/threeafter1after2", conversation.doRequest("/three?next4=true").getText());
        assertEquals("before1before2/threeafter2", conversation.doRequest("/three?next5=true").getText());
        assertEquals("before1before2/threeafter1", conversation.doRequest("/three?next6=true").getText());

        assertEquals("before1before2/fourafter1after2", conversation.doRequest("/four").getText());
        assertEquals("before2/fourafter1after2", conversation.doRequest("/four?next1=true").getText());
        assertEquals("before1/fourafter1after2", conversation.doRequest("/four?next2=true").getText());
        assertEquals("before1before2/fourafter1after2", conversation.doRequest("/four??next3=true").getText());
        assertEquals("before1before2after1after2", conversation.doRequest("/four?next4=true").getText());
        assertEquals("before1before2/fourafter2", conversation.doRequest("/four?next5=true").getText());
        assertEquals("before1before2/fourafter1", conversation.doRequest("/four?next6=true").getText());
    }
}
