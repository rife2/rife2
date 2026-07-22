/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.engine.elements.CsrfProtected;
import rife.engine.exceptions.CsrfTokenException;
import rife.engine.exceptions.CsrfTokenInvalidException;
import rife.engine.exceptions.CsrfTokenMissingException;
import rife.test.MockConversation;
import rife.test.MockRequest;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestCsrf {
    // a CsrfProtected whose refusal hook surfaces which verification
    // failed, so the tests can assert on it
    static class VerboseCsrfProtected extends CsrfProtected {
        VerboseCsrfProtected() {
            super();
        }

        VerboseCsrfProtected(Set<RequestMethod> protectedMethods) {
            super(protectedMethods);
        }

        protected void refused(Context c, CsrfTokenException e) {
            c.print("refused: " + e.getClass().getSimpleName());
        }
    }

    static class ProtectedSite extends Site {
        Route form = get("/form", c -> c.print(c.template("csrf_form")));
        Route save = post("/save", c -> c.print("saved " + c.parameter("name")));

        public void setup() {
            before(new VerboseCsrfProtected());
        }
    }

    // a single route that answers every request method, so each one can be
    // exercised against the same CsrfProtected element
    static class MethodSite extends Site {
        final Set<RequestMethod> protectedMethods_;

        MethodSite() {
            protectedMethods_ = null;
        }

        MethodSite(Set<RequestMethod> protectedMethods) {
            protectedMethods_ = protectedMethods;
        }

        Route act = route("/act", c -> c.print("acted"));

        public void setup() {
            before(protectedMethods_ == null ? new VerboseCsrfProtected() : new VerboseCsrfProtected(protectedMethods_));
        }
    }

    @Test
    void testTokenIsStableWithinRequest() {
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/token", c -> c.print(c.csrfToken() + " " + c.csrfToken()));
            }
        });

        var text = m.doRequest("/token").getText();
        var parts = text.split(" ");
        assertEquals(2, parts.length);
        assertEquals(parts[0], parts[1]);
        assertFalse(parts[0].isEmpty());
    }

    @Test
    void testTokenCookieIsSetOnce() {
        var m = new MockConversation(new ProtectedSite());

        var first = m.doRequest("/form");
        assertTrue(first.getNewCookieNames().contains(RifeConfig.engine().getCsrfCookieName()));
        var cookie = m.getCookie(RifeConfig.engine().getCsrfCookieName());
        assertNotNull(cookie);
        assertTrue(cookie.isHttpOnly());
        assertEquals("Lax", cookie.getAttribute("SameSite"));
        assertFalse(cookie.getValue().isEmpty());

        // the browser keeps its token, a next render doesn't set it again
        var second = m.doRequest("/form");
        assertFalse(second.getNewCookieNames().contains(RifeConfig.engine().getCsrfCookieName()));
        assertTrue(second.getText().contains(cookie.getValue()));
    }

    @Test
    void testFormTagSubmitsSuccessfully() {
        var m = new MockConversation(new ProtectedSite());

        var form = m.doRequest("/form").getParsedHtml().getFormWithName("protected");
        form.setParameter("name", "John");
        assertEquals("saved John", form.submit().getText());
    }

    @Test
    void testHiddenInputCarriesTheToken() {
        var m = new MockConversation(new ProtectedSite());

        var response = m.doRequest("/form");
        var token = m.getCookieValue(RifeConfig.engine().getCsrfCookieName());
        // the route:inputs: tag adds the token as a hidden input, since the
        // form submits to a CSRF-protected route
        assertTrue(response.getText().contains(
            "<input type=\"hidden\" name=\"" + RifeConfig.engine().getCsrfParameterName() +
            "\" value=\"" + token + "\" />"));
        // the raw token is available through context:csrfToken for the
        // requests that scripts perform
        assertTrue(response.getText().contains("data-token=\"" + token + "\""));
    }

    @Test
    void testInputsOmitTokenWhenNoTokenIsActive() {
        var m = new MockConversation(new Site() {
            Route save = post("/save", c -> c.print("saved"));
            // the plain template doesn't reference the token, and no
            // CsrfProtected establishes one, so no token is active
            Route form = get("/form", c -> c.print(c.template("csrf_plain_form")));

            public void setup() {
            }
        });

        var text = m.doRequest("/form").getText();
        assertFalse(text.contains(RifeConfig.engine().getCsrfParameterName() + "\" value="));
    }

    @Test
    void testInputsIncludeTokenWheneverActive() {
        var m = new MockConversation(new Site() {
            Route save = post("/save", c -> c.print("saved"));
            // the token is established, but this route isn't verified by a
            // CsrfProtected element; the form still carries the token, which
            // is harmless, only the verification is what's opt-in
            Route form = get("/form", c -> {
                c.csrfToken();
                c.print(c.template("csrf_plain_form"));
            });

            public void setup() {
            }
        });

        var text = m.doRequest("/form").getText();
        assertTrue(text.contains(RifeConfig.engine().getCsrfParameterName() + "\" value="));
    }

    @Test
    void testHeaderIsAccepted() {
        var m = new MockConversation(new ProtectedSite());

        m.doRequest("/form");
        var token = m.getCookieValue(RifeConfig.engine().getCsrfCookieName());
        var response = m.doRequest("/save", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "Jane")
            .header(RifeConfig.engine().getCsrfHeaderName(), token));
        assertEquals("saved Jane", response.getText());
    }

    @Test
    void testMissingTokenIsRefused() {
        var m = new MockConversation(new ProtectedSite());

        // no token cookie has been established at all
        assertEquals("refused: " + CsrfTokenMissingException.class.getSimpleName(),
            m.doRequest("/save", new MockRequest().method(RequestMethod.POST).parameter("name", "Eve")).getText());

        // the browser has a token, but the request doesn't provide it
        m.doRequest("/form");
        assertEquals("refused: " + CsrfTokenMissingException.class.getSimpleName(),
            m.doRequest("/save", new MockRequest().method(RequestMethod.POST).parameter("name", "Eve")).getText());
    }

    @Test
    void testForeignTokenIsRefused() {
        var m = new MockConversation(new ProtectedSite());
        m.doRequest("/form");

        // a token from another browser doesn't work
        var other = new MockConversation(new ProtectedSite());
        other.doRequest("/form");
        var other_token = other.getCookieValue(RifeConfig.engine().getCsrfCookieName());

        assertEquals("refused: " + CsrfTokenInvalidException.class.getSimpleName(),
            m.doRequest("/save", new MockRequest()
                .method(RequestMethod.POST)
                .parameter("name", "Eve")
                .parameter(RifeConfig.engine().getCsrfParameterName(), other_token)).getText());
    }

    @Test
    void testSafeMethodsArentVerified() {
        var m = new MockConversation(new Site() {
            public void setup() {
                before(new CsrfProtected());
                get("/read", c -> c.print("read"));
            }
        });

        // reading never needs a token, and establishes one for later
        var response = m.doRequest("/read");
        assertEquals("read", response.getText());
        assertTrue(response.getNewCookieNames().contains(RifeConfig.engine().getCsrfCookieName()));
    }

    @Test
    void testUnprotectedRoutesArentVerified() {
        var m = new MockConversation(new Site() {
            public void setup() {
                post("/open", c -> c.print("open"));
            }
        });

        // protection is opt-in, a site without the element is unaffected
        assertEquals("open", m.doRequest("/open",
            new MockRequest().method(RequestMethod.POST)).getText());
    }

    @Test
    void testDefaultRefusalIsForbidden() {
        var m = new MockConversation(new Site() {
            public void setup() {
                // the plain element, without a custom refusal hook
                before(new CsrfProtected());
                post("/save", c -> c.print("saved"));
            }
        });

        // a refused request gets a 403 straight from the element, no
        // exception route is needed
        var response = m.doRequest("/save", new MockRequest().method(RequestMethod.POST));
        assertEquals(Context.SC_FORBIDDEN, response.getStatus());
        assertEquals("The CSRF token verification failed.", response.getText());
    }

    @Test
    void testRefusalHookCanTailorTheResponse() {
        var m = new MockConversation(new Site() {
            public void setup() {
                before(new CsrfProtected() {
                    protected void refused(Context c, CsrfTokenException e) {
                        c.setStatus(418);
                        c.print("nope: " + e.getClass().getSimpleName());
                    }
                });
                post("/save", c -> c.print("saved"));
            }
        });

        var response = m.doRequest("/save", new MockRequest().method(RequestMethod.POST));
        assertEquals(418, response.getStatus());
        assertEquals("nope: " + CsrfTokenMissingException.class.getSimpleName(), response.getText());
    }

    @Test
    void testProtectedMethodsAreRefusedWithoutToken() {
        // every default state-changing method is verified, so it fails when
        // the browser doesn't hold a token at all
        for (var method : CsrfProtected.DEFAULT_PROTECTED_METHODS) {
            var m = new MockConversation(new MethodSite());
            var response = m.doRequest("/act", new MockRequest().method(method));
            assertEquals("refused: " + CsrfTokenMissingException.class.getSimpleName(),
                response.getText(), method + " should be verified");
        }
    }

    @Test
    void testProtectedMethodsAreAcceptedWithToken() {
        // the same methods pass once the request carries the token the
        // browser was handed
        for (var method : CsrfProtected.DEFAULT_PROTECTED_METHODS) {
            var m = new MockConversation(new MethodSite());
            // a safe request establishes the token the browser holds
            m.doRequest("/act");
            var token = m.getCookieValue(RifeConfig.engine().getCsrfCookieName());
            var response = m.doRequest("/act", new MockRequest().method(method)
                .parameter(RifeConfig.engine().getCsrfParameterName(), token));
            assertEquals("acted", response.getText(), method + " should pass with a valid token");
        }
    }

    @Test
    void testProtectedMethodsAcceptTheHeaderToken() {
        // the header carries the token just as well as the parameter
        for (var method : CsrfProtected.DEFAULT_PROTECTED_METHODS) {
            var m = new MockConversation(new MethodSite());
            m.doRequest("/act");
            var token = m.getCookieValue(RifeConfig.engine().getCsrfCookieName());
            var response = m.doRequest("/act", new MockRequest().method(method)
                .header(RifeConfig.engine().getCsrfHeaderName(), token));
            assertEquals("acted", response.getText(), method + " should pass with a valid header token");
        }
    }

    @Test
    void testUnprotectedMethodsArentVerified() {
        // the methods that the default set leaves out are never verified,
        // they establish the token instead of failing
        for (var method : EnumSet.complementOf(EnumSet.copyOf(CsrfProtected.DEFAULT_PROTECTED_METHODS))) {
            var m = new MockConversation(new MethodSite());
            // no token is provided, yet the request isn't refused
            var response = m.doRequest("/act", new MockRequest().method(method));
            assertFalse(response.getText().startsWith("refused"), method + " should not be verified");
            // and the token is established for the requests that follow
            assertTrue(response.getNewCookieNames().contains(RifeConfig.engine().getCsrfCookieName()),
                method + " should establish the token");
        }
    }

    @Test
    void testCustomProtectedMethodIsVerified() {
        // a set that also lists GET turns it into a verified method
        var m = new MockConversation(new MethodSite(EnumSet.of(RequestMethod.GET)));
        assertEquals("refused: " + CsrfTokenMissingException.class.getSimpleName(),
            m.doRequest("/act").getText());

        var m2 = new MockConversation(new MethodSite(EnumSet.of(RequestMethod.GET)));
        m2.doRequest("/act", new MockRequest().method(RequestMethod.POST));
        var token = m2.getCookieValue(RifeConfig.engine().getCsrfCookieName());
        assertEquals("acted", m2.doRequest("/act", new MockRequest()
            .parameter(RifeConfig.engine().getCsrfParameterName(), token)).getText());
    }

    @Test
    void testCustomSetLeavesOtherMethodsUnverified() {
        // a set that only lists POST leaves the other default state-changing
        // methods unverified
        for (var method : EnumSet.of(RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE)) {
            var m = new MockConversation(new MethodSite(EnumSet.of(RequestMethod.POST)));
            var response = m.doRequest("/act", new MockRequest().method(method));
            assertEquals("acted", response.getText(), method + " should not be verified");
            assertTrue(response.getNewCookieNames().contains(RifeConfig.engine().getCsrfCookieName()),
                method + " should establish the token");
        }

        // POST itself stays verified
        var m = new MockConversation(new MethodSite(EnumSet.of(RequestMethod.POST)));
        assertEquals("refused: " + CsrfTokenMissingException.class.getSimpleName(),
            m.doRequest("/act", new MockRequest().method(RequestMethod.POST)).getText());
    }

    @Test
    void testInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new CsrfProtected(null));
        assertThrows(IllegalArgumentException.class, () -> new CookieBuilder("name", "value").sameSite(null));
    }
}
