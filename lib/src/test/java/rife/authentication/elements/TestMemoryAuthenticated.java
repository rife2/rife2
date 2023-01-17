/*
 * Copyright 2001-2023 Geert Bevin (jdevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import org.junit.jupiter.api.Test;
import rife.test.MockConversation;

import static org.junit.jupiter.api.Assertions.*;

public class TestMemoryAuthenticated {
    @Test
    void testMemoryAuthenticatedBasic() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        var response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");

        response = conversation.doRequest("http://localhost/login");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/landing");

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "guest");

        // other login

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");

        response = conversation.doRequest("http://localhost/login");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "jdevin");
        form.setParameter("password", "yeolpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/landing");

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "jdevin");

        // invalid login

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");

        response = conversation.doRequest("http://localhost/login");
        form = response.getParsedHtml().getFormWithName("credentials");
        assertEquals(response.getTemplate().getValue("errors:*"), "");
        form.setParameter("login", "invalid");
        form.setParameter("password", "invalid");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");
    }

    @Test
    void testMemoryAuthenticatedEncrypted() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        var response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");

        response = conversation.doRequest("http://localhost/login");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guestencrypted");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/landing");

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "guestencrypted");

        // other login

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");

        response = conversation.doRequest("http://localhost/login");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "jdevinencrypted");
        form.setParameter("password", "yeolpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/landing");

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "jdevinencrypted");

        // invalid login

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");

        response = conversation.doRequest("http://localhost/login");
        form = response.getParsedHtml().getFormWithName("credentials");
        assertEquals(response.getTemplate().getValue("errors:*"), "");
        form.setParameter("login", "invalid");
        form.setParameter("password", "invalid");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");
    }

    @Test
    void testMemoryAuthenticatedRole() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        // missing role

        var response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/loginAdmin");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");

        // login with role

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/loginAdmin");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "jdevin");
        form.setParameter("password", "yeolpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/admin/landing");

        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "jdevin");

        // login without role

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/loginAdmin");
        form = response.getParsedHtml().getFormWithName("credentials");
        assertEquals(response.getTemplate().getValue("errors:*"), "");
        form.setParameter("login", "johndoe");
        form.setParameter("password", "thepassofbass");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");

        // invalid login

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/loginAdmin");
        form = response.getParsedHtml().getFormWithName("credentials");
        assertEquals(response.getTemplate().getValue("errors:*"), "");
        form.setParameter("login", "invalid");
        form.setParameter("password", "invalid");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");
    }

    @Test
    void testMemoryAuthenticatedRoleEncrypted() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        // missing role

        var response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/loginAdmin");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guestencrypted");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");

        // login with role

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/loginAdmin");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "jdevinencrypted");
        form.setParameter("password", "yeolpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/admin/landing");

        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "jdevinencrypted");

        // login without role

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/loginAdmin");
        form = response.getParsedHtml().getFormWithName("credentials");
        assertEquals(response.getTemplate().getValue("errors:*"), "");
        form.setParameter("login", "johndoeencrypted");
        form.setParameter("password", "thepassofbass");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");

        // invalid login

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/loginAdmin");
        form = response.getParsedHtml().getFormWithName("credentials");
        assertEquals(response.getTemplate().getValue("errors:*"), "");
        form.setParameter("login", "invalid");
        form.setParameter("password", "invalid");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");
    }

    @Test
    void testMemoryAuthenticatedRole2() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        // missing role

        var response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");

        // login with role

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "jdevin");
        form.setParameter("password", "yeolpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/maint/landing");

        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "jdevin");

        // other login with role

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "johndoe");
        form.setParameter("password", "thepassofbass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/maint/landing");

        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "johndoe");

        // invalid login

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        form = response.getParsedHtml().getFormWithName("credentials");
        assertEquals(response.getTemplate().getValue("errors:*"), "");
        form.setParameter("login", "invalid");
        form.setParameter("password", "invalid");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");
    }

    @Test
    void testMemoryAuthenticatedRole2Encrypted() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        // missing role

        var response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guestencrypted");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");

        // login with role

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "jdevinencrypted");
        form.setParameter("password", "yeolpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/maint/landing");

        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "jdevinencrypted");

        // other login with role

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "johndoeencrypted");
        form.setParameter("password", "thepassofbass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/maint/landing");

        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "johndoeencrypted");

        // invalid login

        conversation = new MockConversation(site);
        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        form = response.getParsedHtml().getFormWithName("credentials");
        assertEquals(response.getTemplate().getValue("errors:*"), "");
        form.setParameter("login", "invalid");
        form.setParameter("password", "invalid");
        response = form.submit();

        assertEquals(0, response.getNewCookieNames().size());
        assertEquals(1, response.getParsedHtml().getForms().size());
        assertNotEquals(response.getTemplate().getValue("errors:*"), "");
    }

    @Test
    void testMemoryAuthenticatedRoleIsolation() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        // missing role

        var response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");

        response = conversation.doRequest("http://localhost/loginMaint");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "johndoe");
        form.setParameter("password", "thepassofbass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/maint/landing");

        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "johndoe");

        // visit a section that requires another role

        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");
    }

    @Test
    void testMemoryAuthenticatedRoleIsolation2() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        // missing role

        var response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");

        response = conversation.doRequest("http://localhost/login");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/landing");

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "guest");

        // visit a section that requires a role

        response = conversation.doRequest("http://localhost/admin/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginAdmin");

        response = conversation.doRequest("http://localhost/maint/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/loginMaint");
    }

    @Test
    void testMemoryAuthenticatedSessionDuration()
    throws Exception {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        site.validator.getSessionManager().setSessionDuration(1000);
        var conversation = new MockConversation(site);

        var response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");

        response = conversation.doRequest("http://localhost/login");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/landing");

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "guest");

        Thread.sleep(2000);

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");
    }

    @Test
    void testMemoryAuthenticatedNotEnforce() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        var response = conversation.doRequest("http://localhost/notEnforced/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/notEnforced/username");
        assertEquals(response.getText(), "not logged in");

        response = conversation.doRequest("http://localhost/loginNotEnforced");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/notEnforced/landing");

        response = conversation.doRequest("http://localhost/notEnforced/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/notEnforced/username");
        assertEquals(response.getText(), "guest");

        // change to user login

        response = conversation.doRequest("http://localhost/loginNotEnforced");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "jdevin");
        form.setParameter("password", "yeolpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/notEnforced/landing");

        response = conversation.doRequest("http://localhost/notEnforced/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/notEnforced/username");
        assertEquals(response.getText(), "jdevin");
    }

    @Test
    void testMemoryAuthenticatedLogoutTemplate() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        var response = conversation.doRequest("http://localhost/login");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/landing");

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "guest");

        // log out
        assertNotEquals(conversation.getCookieValue("authId"), "");
        response = conversation.doRequest("http://localhost/logout");
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getText().contains("Logged out"));
        assertEquals(conversation.getCookieValue("authId"), "");

        // verify user is logged out
        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");
    }

    @Test
    void testMemoryAuthenticatedLogoutBefore() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        var response = conversation.doRequest("http://localhost/login");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/landing");

        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getText(), "Landing");
        response = conversation.doRequest("http://localhost/username");
        assertEquals(response.getText(), "guest");

        // log out
        assertNotEquals(conversation.getCookieValue("authId"), "");
        response = conversation.doRequest("http://localhost/beforelogout");
        assertEquals(response.getStatus(), 200);
        assertEquals(response.getText(), "logged out");
        assertEquals(conversation.getCookieValue("authId"), "");

        // verify user is logged out
        response = conversation.doRequest("http://localhost/landing");
        assertEquals(response.getStatus(), 302);
        assertEquals(response.getHeader("Location"), "http://localhost/login");
    }

    @Test
    void testMemoryAuthenticatedFilteredTagsNotEnforced() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        var response = conversation.doRequest("http://localhost/notEnforced/template");
        assertEquals(response.getText().trim(), """
            not authenticated
            not matching login1
            not matching login3
            not matching role1
            not matching role2
            not matching role3""");
    }

    @Test
    void testMemoryAuthenticatedFilteredTags() {
        MemoryAuthenticatedSite site = new MemoryAuthenticatedSite();
        var conversation = new MockConversation(site);

        var response = conversation.doRequest("http://localhost/login");
        var form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "guest");
        form.setParameter("password", "guestpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);

        response = conversation.doRequest("http://localhost/template");
        assertEquals(response.getText().trim(), """
            authenticated
            matching login1
            not matching login3
            not matching role1
            not matching role2
            not matching role3
            """ + conversation.getCookieValue("authId"));

        response = conversation.doRequest("http://localhost/login");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "jdevin");
        form.setParameter("password", "yeolpass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);

        response = conversation.doRequest("http://localhost/template");
        assertEquals(response.getText().trim(), """
            authenticated
            matching login2
            not matching login3
            matching role1
            matching role2
            not matching role3
            """ + conversation.getCookieValue("authId"));

        response = conversation.doRequest("http://localhost/login");
        form = response.getParsedHtml().getFormWithName("credentials");
        form.setParameter("login", "johndoe");
        form.setParameter("password", "thepassofbass");
        response = form.submit();

        assertEquals("authId", response.getNewCookieNames().get(0));
        assertEquals(0, response.getParsedHtml().getForms().size());
        assertEquals(response.getStatus(), 302);

        response = conversation.doRequest("http://localhost/template");
        assertEquals(response.getText().trim(), """
            authenticated
            not matching login1
            not matching login3
            not matching role1
            matching role2
            not matching role3
            """ + conversation.getCookieValue("authId"));
    }
}
