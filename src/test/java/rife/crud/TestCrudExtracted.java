/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.RequestMethod;
import rife.test.MockConversation;
import rife.test.MockRequest;
import rifetestmodels.CrudArticle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins that everything an administration is configured with can live in a
 * dedicated class, so that a site declaration stays a declaration.
 */
public class TestCrudExtracted {
    private GenericQueryManager<CrudArticle> articles_;
    private MockConversation conversation_;

    @BeforeEach
    void setup() {
        articles_ = GenericQueryManagerFactory.instance(TestDatasources.H2, CrudArticle.class);
        try {
            articles_.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        articles_.install();
        conversation_ = new MockConversation(new CrudExtractedSite(TestDatasources.H2));
    }

    @AfterEach
    void tearDown() {
        try {
            articles_.remove();
        } catch (Exception e) {
            // already gone
        }
    }

    private int addArticle(String title, String status) {
        var article = new CrudArticle();
        article.setTitle(title);
        article.setStatus(status);
        article.setOrdinal(articles_.count());
        return articles_.save(article);
    }

    @Test
    void testTheExtractedOptionsAreApplied() {
        addArticle("A draft", "draft");

        // the slug and the label of the extracted configuration are used
        var text = conversation_.doRequest("/admin/articles").getText();
        assertTrue(text.contains("Extracted Administration"));
        assertTrue(text.contains("Add Article"));
        assertTrue(text.contains("A draft"));
    }

    @Test
    void testTheExtractedActionHandlerRuns() {
        var id = addArticle("A draft", "draft");

        var response = conversation_.doRequest("/admin/articles/action/publish/" + id,
            new MockRequest().method(RequestMethod.POST));
        assertTrue(response.getHeader("Location").endsWith("/admin/articles"));
        assertEquals("published", articles_.restore(id).getStatus());

        var browse = conversation_.doRequest("/admin/articles").getText();
        assertTrue(browse.contains("The article has been published."));
    }

    @Test
    void testTheExtractedConditionsGovernVisibility() {
        var draft = addArticle("A draft", "draft");
        var published = addArticle("Published", "published");

        var text = conversation_.doRequest("/admin/articles").getText();

        // the condition class decides which rows offer which operations
        assertTrue(text.contains("/action/publish/" + draft));
        assertFalse(text.contains("/action/publish/" + published));
        assertTrue(text.contains("/delete/" + draft));
        assertFalse(text.contains("/delete/" + published));

        // and it's enforced again when the operation is submitted
        conversation_.doRequest("/admin/articles/delete/" + published,
            new MockRequest().method(RequestMethod.POST));
        assertNotNull(articles_.restore(published));
    }
}
