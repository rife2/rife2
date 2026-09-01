/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.TestServerRunner;
import rifetestmodels.CrudArticle;
import rifetestmodels.CrudSubscriber;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the administration through a real browser against a real server,
 * so that the generated forms, the redirects and the flow between the pages
 * are exercised the way a person would.
 */
public class TestCrudAdminEndToEnd {
    private final Datasource datasource_ = TestDatasources.H2;
    private GenericQueryManager<CrudArticle> articles_;
    private GenericQueryManager<CrudSubscriber> subscribers_;

    @BeforeEach
    void setup() {
        articles_ = GenericQueryManagerFactory.instance(datasource_, CrudArticle.class);
        subscribers_ = GenericQueryManagerFactory.instance(datasource_, CrudSubscriber.class);
        try {
            articles_.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        try {
            subscribers_.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        articles_.install();
        subscribers_.install();
    }

    @AfterEach
    void tearDown() {
        try {
            articles_.remove();
            subscribers_.remove();
        } catch (Exception e) {
            // already gone
        }
    }

    @Test
    void testTheWholeLifecycleThroughTheBrowser()
    throws Exception {
        try (var server = new TestServerRunner(new CrudAdminSite(datasource_));
             var client = new WebClient()) {
            client.getOptions().setThrowExceptionOnScriptError(false);

            // the dashboard links to the entities
            HtmlPage page = client.getPage("http://localhost:8181/admin/");
            assertTrue(page.asNormalizedText().contains("Overview"));

            page = page.getAnchorByText("Articles").click();
            assertTrue(page.asNormalizedText().contains("no Articles yet"));

            // add the first article through the generated form
            page = page.getAnchorByText("Add the first one").click();
            var form = page.getForms().get(0);
            form.getInputByName("title").setValue("Hypermedia rediscovered");
            form.getInputByName("author").setValue("Geert");
            form.getSelectByName("status").setSelectedAttribute("draft", true);
            page = form.getButtonByName("").click();

            var text = page.asNormalizedText();
            assertTrue(text.contains("Article has been added."));
            assertTrue(text.contains("Hypermedia rediscovered"));
            assertEquals(1, articles_.count());

            // the mandatory constraint is enforced on the server as well
            page = page.getAnchorByText("Add Article").click();
            var invalid = page.getForms().get(0);
            invalid.getInputByName("title").setValue("");
            invalid.getInputByName("title").removeAttribute("required");
            page = invalid.getButtonByName("").click();
            assertTrue(page.asNormalizedText().contains("Title is required."));
            assertEquals(1, articles_.count());

            // edit the article that was added
            page = client.getPage("http://localhost:8181/admin/crudArticle");
            page = page.getAnchorByText("Edit").click();
            var edit = page.getForms().get(0);
            assertEquals("Hypermedia rediscovered", edit.getInputByName("title").getValue());
            edit.getInputByName("title").setValue("Hypermedia, rediscovered");
            page = edit.getButtonByName("").click();

            text = page.asNormalizedText();
            assertTrue(text.contains("Article has been updated."));
            assertTrue(text.contains("Hypermedia, rediscovered"));

            // publish it through the custom action
            var publish = page.getForms().stream()
                .filter(f -> f.getActionAttribute().contains("/action/publish/"))
                .findFirst().orElseThrow();
            page = publish.getButtonByName("").click();
            assertTrue(page.asNormalizedText().contains("The article has been published."));
            assertEquals("published", articles_.restore().get(0).getStatus());

            // a published article can no longer be deleted from the list
            assertTrue(page.getByXPath("//a[contains(@href,'/delete/')]").isEmpty());
        }
    }

    @Test
    void testOrderingThroughTheBrowser()
    throws Exception {
        try (var server = new TestServerRunner(new CrudAdminSite(datasource_));
             var client = new WebClient()) {
            for (var title : new String[]{"First", "Second", "Third"}) {
                var article = new CrudArticle();
                article.setTitle(title);
                article.setStatus("draft");
                article.setOrdinal(articles_.count());
                articles_.save(article);
            }

            HtmlPage page = client.getPage("http://localhost:8181/admin/crudArticle");
            var rows = page.getByXPath("//tbody/tr");
            assertEquals(3, rows.size());

            // move the last row up and verify the new order on the page
            var moveUp = page.getForms().stream()
                .filter(f -> f.getActionAttribute().endsWith("/up"))
                .reduce((first, second) -> second).orElseThrow();
            page = moveUp.getButtonByName("").click();

            var order = articles_.restore(articles_.getRestoreQuery().orderBy("ordinal"));
            assertEquals("First", order.get(0).getTitle());
            assertEquals("Third", order.get(1).getTitle());
            assertEquals("Second", order.get(2).getTitle());
        }
    }

    @Test
    void testDeletionThroughTheBrowser()
    throws Exception {
        try (var server = new TestServerRunner(new CrudAdminSite(datasource_));
             var client = new WebClient()) {
            var subscriber = new CrudSubscriber();
            subscriber.setEmail("gbevin@uwyn.com");
            subscriber.setName("Geert");
            subscribers_.save(subscriber);

            HtmlPage page = client.getPage("http://localhost:8181/admin/subscribers");
            page = page.getAnchorByText("Delete").click();

            var text = page.asNormalizedText();
            assertTrue(text.contains("Delete Subscriber"));
            assertTrue(text.contains("gbevin@uwyn.com"));
            assertEquals(1, subscribers_.count());

            page = page.getForms().get(0).getButtonByName("").click();
            assertTrue(page.asNormalizedText().contains("Subscriber has been deleted."));
            assertEquals(0, subscribers_.count());
        }
    }
}
