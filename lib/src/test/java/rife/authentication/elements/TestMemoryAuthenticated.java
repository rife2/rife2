/*
 * Copyright 2001-2022 Geert Bevin (jdevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.test.MockConversation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestMemoryAuthenticated {
    @Test
    public void testMemoryAuthenticatedBasic() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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
    public void testMemoryAuthenticatedEncrypted() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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
    public void testMemoryAuthenticatedRole() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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
    public void testMemoryAuthenticatedRoleEncrypted() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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
    public void testMemoryAuthenticatedRole2() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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
    public void testMemoryAuthenticatedRole2Encrypted() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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

        conversation = new MockConversation(new MemoryAuthenticatedSite());
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
    public void testMemoryAuthenticatedRoleIsolation() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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
    public void testMemoryAuthenticatedRoleIsolation2() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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
    public void testMemoryAuthenticatedSessionDuration()
    throws Exception {
        var session_duration = RifeConfig.authentication().getSessionDuration();
        try {
            RifeConfig.authentication().setSessionDuration(1000);

            var conversation = new MockConversation(new MemoryAuthenticatedSite());

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
        } finally {
            RifeConfig.authentication().setSessionDuration(session_duration);
        }
    }

    @Test
    public void testMemoryAuthenticatedNotEnforce() {
        var conversation = new MockConversation(new MemoryAuthenticatedSite());

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

}
