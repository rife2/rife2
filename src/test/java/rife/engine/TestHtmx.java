/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.engine.elements.CsrfProtected;
import rife.test.MockConversation;
import rife.test.MockRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestHtmx {
    // reads back every HX-* request header the accessors expose
    static class RequestSite extends Site {
        public void setup() {
            get("/info", c -> c.print(
                "hx=" + c.isHxRequest() +
                " boosted=" + c.isHxBoosted() +
                " target=" + c.hxTarget() +
                " triggerId=" + c.hxTriggerId() +
                " triggerName=" + c.hxTriggerName() +
                " url=" + c.hxCurrentUrl() +
                " prompt=" + c.hxPrompt()));
        }
    }

    @Test
    void testRequestAccessors() {
        var m = new MockConversation(new RequestSite());
        var response = m.doRequest("/info", new MockRequest()
            .htmx()
            .header("HX-Boosted", "true")
            .header("HX-Target", "content")
            .header("HX-Trigger", "save-btn")
            .header("HX-Trigger-Name", "save")
            .header("HX-Current-URL", "http://localhost/books")
            .header("HX-Prompt", "the answer"));
        assertEquals("hx=true boosted=true target=content triggerId=save-btn " +
                     "triggerName=save url=http://localhost/books prompt=the answer", response.getText());
    }

    @Test
    void testRequestAccessorsPlainRequest() {
        var m = new MockConversation(new RequestSite());
        var response = m.doRequest("/info");
        assertEquals("hx=false boosted=false target=null triggerId=null " +
                     "triggerName=null url=null prompt=null", response.getText());
    }

    @Test
    void testVaryHeaderAdded() {
        var m = new MockConversation(new RequestSite());
        // even a non-htmx request that consults isHxRequest() must vary, so a
        // cached full page is never handed to an htmx request for the same URL
        var response = m.doRequest("/info");
        assertEquals("HX-Request", response.getHeader("Vary"));
    }

    // exercises printBlock directly through the manual full-page-or-fragment
    // branch; real code should use printHtmxFragment (see PrintHtmxFragmentSite
    // below), which also covers htmx history restoration
    static class FragmentSite extends Site {
        public void setup() {
            get("/books", c -> {
                var t = c.template("htmx_fragment");
                t.setValueEncoded("item", "Refactoring");
                if (c.isHxRequest()) {
                    c.printBlock(t, "list");
                } else {
                    t.setBlock("list", "list");
                    c.print(t);
                }
            });
        }
    }

    @Test
    void testFullPageWhenNotHtmx() {
        var m = new MockConversation(new FragmentSite());
        var text = m.doRequest("/books").getText();
        assertTrue(text.contains("<h1>full page</h1>"), "the whole page is sent");
        assertTrue(text.contains("LIST:Refactoring"), "with the block rendered inside it");
    }

    @Test
    void testFragmentWhenHtmx() {
        var m = new MockConversation(new FragmentSite());
        var response = m.doRequest("/books", new MockRequest().htmx());
        // only the block content comes back, not the surrounding document
        assertEquals("LIST:Refactoring", response.getText().trim());
        assertFalse(response.getText().contains("<h1>"), "the surrounding page is not sent");
        assertEquals("HX-Request", response.getHeader("Vary"));
    }

    // the same, but the whole full-page-or-fragment choice is one printHtmxFragment
    // call against a block value, so the element does no branching of its own
    static class PrintHtmxFragmentSite extends Site {
        public void setup() {
            get("/books", c -> {
                var t = c.template("htmx_bv");
                t.setValueEncoded("item", "Refactoring");
                c.printHtmxFragment(t, "list");
            });
        }
    }

    @Test
    void testPrintHtmxFragmentFullPage() {
        var m = new MockConversation(new PrintHtmxFragmentSite());
        var text = m.doRequest("/books").getText();
        assertTrue(text.contains("<h1>full page</h1>"), "a normal request gets the whole page");
        assertTrue(text.contains("LIST:Refactoring"), "with the block value rendered in place");
    }

    @Test
    void testPrintHtmxFragmentHtmx() {
        var m = new MockConversation(new PrintHtmxFragmentSite());
        var response = m.doRequest("/books", new MockRequest().htmx());
        assertEquals("LIST:Refactoring", response.getText().trim(), "an htmx request gets just the block");
        assertFalse(response.getText().contains("<h1>"), "the surrounding page is not sent");
        assertEquals("HX-Request", response.getHeader("Vary"));
    }

    @Test
    void testHtmxMockRequestHelper() {
        var m = new MockConversation(new RequestSite());
        // htmx() sets HX-Request; other HX-* headers still chain via header()
        var response = m.doRequest("/info", new MockRequest().htmx().header("HX-Target", "content"));
        assertTrue(response.getText().startsWith("hx=true"), "htmx() marks the request as htmx");
        assertTrue(response.getText().contains("target=content"), "chained HX-* headers still apply");
    }

    // exercises the response-side HX-* header helpers
    static class ResponseSite extends Site {
        Route target = get("/target", c -> c.print("target"));

        public void setup() {
            get("/redirect", c -> c.hxRedirect("/somewhere"));
            get("/redirect-route", c -> c.hxRedirect(target));
            get("/location", c -> c.hxLocation("/spa"));
            get("/refresh", c -> c.hxRefresh());
            get("/push", c -> c.hxPushUrl(target));
            get("/replace", c -> c.hxReplaceUrl("/history"));
            get("/retarget", c -> { c.hxRetarget("#main"); c.hxReswap("outerHTML"); c.hxReselect("#frag"); });
            get("/trigger", c -> c.hxTrigger("refreshList"));
            get("/trigger-data", c -> c.hxTrigger("showNote", new Note("info", "saved")));
            get("/trigger-list", c -> c.hxTrigger("notes", List.of(new Note("info", "a"), new Note("warn", "b"))));
            get("/trigger-map", c -> c.hxTrigger("notes", Map.of("first", new Note("info", "a"))));
            get("/trigger-array", c -> c.hxTrigger("notes", new Note[]{new Note("info", "a")}));
            get("/trigger-settle", c -> c.hxTriggerAfterSettle("settled"));
            get("/trigger-swap", c -> c.hxTriggerAfterSwap("swapped"));
        }
    }

    record Note(String level, String message) {}

    @Test
    void testResponseHelpers() {
        var m = new MockConversation(new ResponseSite());
        assertEquals("/somewhere", m.doRequest("/redirect").getHeader("HX-Redirect"));
        assertTrue(m.doRequest("/redirect-route").getHeader("HX-Redirect").endsWith("/target"),
            "a route redirect resolves to the route's url");
        assertEquals("/spa", m.doRequest("/location").getHeader("HX-Location"));
        assertEquals("true", m.doRequest("/refresh").getHeader("HX-Refresh"));
        assertTrue(m.doRequest("/push").getHeader("HX-Push-Url").endsWith("/target"),
            "a route push resolves to the route's url");
        assertEquals("/history", m.doRequest("/replace").getHeader("HX-Replace-Url"));

        var retarget = m.doRequest("/retarget");
        assertEquals("#main", retarget.getHeader("HX-Retarget"));
        assertEquals("outerHTML", retarget.getHeader("HX-Reswap"));
        assertEquals("#frag", retarget.getHeader("HX-Reselect"));
    }

    @Test
    void testTriggerHelpers() {
        var m = new MockConversation(new ResponseSite());
        assertEquals("refreshList", m.doRequest("/trigger").getHeader("HX-Trigger"));
        // a payload is serialized to JSON as {"event": data} with RIFE2's own Json
        assertEquals("{\"showNote\":{\"level\":\"info\",\"message\":\"saved\"}}",
            m.doRequest("/trigger-data").getHeader("HX-Trigger"));
        assertEquals("settled", m.doRequest("/trigger-settle").getHeader("HX-Trigger-After-Settle"));
        assertEquals("swapped", m.doRequest("/trigger-swap").getHeader("HX-Trigger-After-Swap"));
    }

    @Test
    void testTriggerNestedBeans() {
        var m = new MockConversation(new ResponseSite());
        // records nested in a collection, a map and an array all serialize
        assertEquals("{\"notes\":[{\"level\":\"info\",\"message\":\"a\"},{\"level\":\"warn\",\"message\":\"b\"}]}",
            m.doRequest("/trigger-list").getHeader("HX-Trigger"));
        assertEquals("{\"notes\":{\"first\":{\"level\":\"info\",\"message\":\"a\"}}}",
            m.doRequest("/trigger-map").getHeader("HX-Trigger"));
        assertEquals("{\"notes\":[{\"level\":\"info\",\"message\":\"a\"}]}",
            m.doRequest("/trigger-array").getHeader("HX-Trigger"));
    }

    @Test
    void testPrintHtmxFragmentServesFullPageOnHistoryRestore() {
        var m = new MockConversation(new PrintHtmxFragmentSite());
        // htmx sends both headers on a history-cache miss and expects the whole
        // page; a fragment would corrupt the restored document
        var response = m.doRequest("/books",
            new MockRequest().htmx().header("HX-History-Restore-Request", "true"));
        assertTrue(response.getText().contains("<h1>full page</h1>"), "the whole page is served on history restore");
        assertTrue(response.getText().contains("LIST:Refactoring"));
        assertTrue(response.getHeaders("Vary").contains("HX-History-Restore-Request"),
            "the response varies on the history-restore header");
    }

    @Test
    void testBoostedVariesOnBoostedHeader() {
        var m = new MockConversation(new RequestSite());
        // /info consults both isHxRequest and isHxBoosted, so both headers must
        // be in Vary, or a cache could serve a boosted response to a plain one
        var vary = m.doRequest("/info", new MockRequest().htmx()).getHeaders("Vary");
        assertTrue(vary.contains("HX-Request"), "varies on HX-Request");
        assertTrue(vary.contains("HX-Boosted"), "varies on HX-Boosted");
    }

    // a CSRF-protected page whose template emits the htmx headers attribute
    static class HtmxHeadersSite extends Site {
        public void setup() {
            before(new CsrfProtected());
            get("/page", c -> c.print(c.template("htmx_headers")));
        }
    }

    @Test
    void testHtmxHeadersTemplateValue() {
        var m = new MockConversation(new HtmxHeadersSite());
        var response = m.doRequest("/page");
        var token = m.getCookieValue(RifeConfig.engine().getCsrfCookieName());
        assertNotNull(token, "the GET establishes a CSRF token");
        // the attribute carries the token in the header CsrfProtected accepts,
        // so every htmx request off this page passes CSRF verification
        assertTrue(response.getText().contains(
            "hx-headers='{\"" + RifeConfig.engine().getCsrfHeaderName() + "\":\"" + token + "\"}'"),
            "the rendered page carries the hx-headers attribute with the token");
    }
}
