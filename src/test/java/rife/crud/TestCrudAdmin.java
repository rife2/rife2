/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.crud.exceptions.*;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.RequestMethod;
import rife.engine.Site;

import java.util.Locale;
import rife.test.MockConversation;
import rife.test.MockRequest;
import rife.test.MockResponse;
import rifetestmodels.CrudArticle;
import rifetestmodels.CrudKeyed;
import rifetestmodels.CrudPriced;
import rifetestmodels.CrudRanged;
import rifetestmodels.CrudRetargeting;
import rifetestmodels.CrudSubscriber;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrudAdmin {
    private final Datasource datasource_ = TestDatasources.H2;
    private CrudAdminSite site_;
    private MockConversation conversation_;
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

        site_ = new CrudAdminSite(datasource_);
        conversation_ = new MockConversation(site_);
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

    private CrudArticle article(String title, String status) {
        var article = new CrudArticle();
        article.setTitle(title);
        article.setAuthor("Geert");
        article.setStatus(status);
        return article;
    }

    private int addArticle(String title, String status, int ordinal) {
        var article = article(title, status);
        article.setOrdinal(ordinal);
        return articles_.save(article);
    }

    private MockResponse post(String url) {
        return conversation_.doRequest(url, new MockRequest().method(RequestMethod.POST));
    }

    @Test
    void testAValueOfATypeThatConvertsFromTextIsStored() {
        var keyed = GenericQueryManagerFactory.instance(datasource_, CrudKeyed.class);
        try {
            keyed.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        keyed.install();
        try {
            var instance = new CrudKeyed();
            instance.setName("Widget");
            instance.setReference(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"));
            var id = keyed.insert(instance);

            var conversation = new MockConversation(new Site() {
                public void setup() {
                    group("/admin", new CrudAdmin(datasource_).entity(CrudKeyed.class));
                }
            });

            // the form shows it, so a submission has to be able to fill it in
            var replacement = "22222222-2222-2222-2222-222222222222";
            var form = conversation.doRequest("/admin/crudKeyed/edit/" + id).getText();
            assertTrue(form.contains("11111111-1111-1111-1111-111111111111"), form);

            conversation.doRequest("/admin/crudKeyed/edit/" + id, new MockRequest()
                .method(RequestMethod.POST)
                .parameter("name", "Widget")
                .parameter("reference", replacement));

            assertEquals(java.util.UUID.fromString(replacement), keyed.restore(id).getReference());

            // and something that isn't one is reported instead of dropped
            var refused = conversation.doRequest("/admin/crudKeyed/edit/" + id, new MockRequest()
                .method(RequestMethod.POST)
                .parameter("name", "Widget")
                .parameter("reference", "not a reference"));
            assertNull(refused.getHeader("Location"), refused.getText());
            assertEquals(java.util.UUID.fromString(replacement), keyed.restore(id).getReference());
        } finally {
            keyed.remove();
        }
    }

    @Test
    void testACustomLinkIsTextLikeAnythingElse() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    // a configured URL carries whatever it carries
                    .link("Search", "/search?q=a&b=c")
                    .link("Broken", "\" onmouseover=\"alert(1)")
                    .entity(CrudArticle.class));
            }
        });

        var text = conversation.doRequest("/admin/").getText();

        // the query string stays valid markup and the quote can't leave the
        // attribute that holds it
        assertTrue(text.contains("/search?q=a&amp;b=c"), text);
        assertFalse(text.contains("onmouseover=\"alert"), text);
    }

    @Test
    void testDashboardShowsTheEntitiesAndTheirCounts() {
        addArticle("First", "draft", 0);
        addArticle("Second", "draft", 1);

        var response = conversation_.doRequest("/admin/");
        var text = response.getText();

        assertTrue(text.contains("Test Administration"));
        assertTrue(text.contains("Article"));
        assertTrue(text.contains("Subscriber"));
        // the dashboard reports how many instances each entity holds
        assertTrue(text.contains(">2<"));
        // the environment marker keeps a development deployment recognizable
        assertTrue(text.contains("development"));
        // the custom link sits in the same navigation
        assertTrue(text.contains("Website"));
    }

    @Test
    void testBrowseShowsTheListedColumnsInOrder() {
        addArticle("The title", "draft", 0);

        var text = conversation_.doRequest("/admin/crudArticle").getText();
        var title_index = text.indexOf("Title");
        var author_index = text.indexOf("Author");
        var status_index = text.indexOf("Status");

        assertTrue(title_index > 0);
        // the positions decide the order of the columns
        assertTrue(title_index < author_index);
        assertTrue(author_index < status_index);
        // the body isn't listed, so it isn't a column
        assertFalse(text.contains(">Body<"));
        assertTrue(text.contains("The title"));
    }

    @Test
    void testBrowseShowsAnEmptyState() {
        var text = conversation_.doRequest("/admin/crudArticle").getText();
        assertTrue(text.contains("no Articles yet"));
        assertTrue(text.contains("Add the first one"));
    }

    @Test
    void testBrowsePagesThroughTheInstances() {
        for (var i = 0; i < 7; i++) {
            addArticle("Article " + i, "draft", i);
        }

        // the page size of this entity is three
        var first = conversation_.doRequest("/admin/crudArticle").getText();
        assertTrue(first.contains("Article 0"));
        assertTrue(first.contains("Article 2"));
        assertFalse(first.contains("Article 3"));

        var second = conversation_.doRequest("/admin/crudArticle?offset=3").getText();
        assertTrue(second.contains("Article 3"));
        assertTrue(second.contains("Article 5"));
        assertFalse(second.contains("Article 6"));

        // an offset beyond the end lands on the last page instead of an
        // empty one
        var beyond = conversation_.doRequest("/admin/crudArticle?offset=99").getText();
        assertTrue(beyond.contains("Article 6"));
    }

    @Test
    void testAFormattedNumberIsShownInAFieldThatAcceptsIt() {
        var priced = GenericQueryManagerFactory.instance(datasource_, CrudPriced.class);
        try {
            priced.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        priced.install();
        try {
            var instance = new CrudPriced();
            instance.setName("Widget");
            instance.setPrice(1234.5);
            instance.setStock(7);
            instance.setTier(1000);
            var id = priced.insert(instance);

            var conversation = new MockConversation(new Site() {
                public void setup() {
                    group("/admin", new CrudAdmin(datasource_).entity(CrudPriced.class));
                }
            });
            var text = conversation.doRequest("/admin/crudPriced/edit/" + id).getText();

            // the field holds what the format writes, which a number field
            // would refuse to show
            assertTrue(text.contains("id=\"crud-price\" name=\"price\" type=\"text\" value=\"$1,234.50\""), text);
            // a number without a format still gets a number field
            assertTrue(text.contains("id=\"crud-stock\" name=\"stock\" type=\"number\""), text);
        } finally {
            priced.remove();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAMoveThatLostItsListReportsAConflict()
    throws Exception {
        var manager = GenericQueryManagerFactory.instance(datasource_, CrudRetargeting.class);
        try {
            manager.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        manager.install();
        try {
            var moving = new CrudRetargeting();
            moving.setTitle("Moving");
            moving.setSection(1);
            moving.setOrdinal(0);
            var id = manager.insert(moving);
            var second = new CrudRetargeting();
            second.setTitle("Second");
            second.setSection(1);
            second.setOrdinal(1);
            manager.insert(second);

            var admin = new CrudAdmin(datasource_).entity(CrudRetargeting.class);
            var entity = (CrudEntity<CrudRetargeting>) admin.getEntities().get(0);
            var move = new CrudMove<>(admin, entity);
            var sequence = entity.createOrdinalSequence(datasource_);

            // the list is held while the row is taken out of it, so the move
            // that is waiting for that list reads where the row sat before
            // it left and finds it gone once it gets in
            var holding = new CountDownLatch(1);
            var proceed = new CountDownLatch(1);
            var holder = new Thread(() -> sequence.inList(1, () -> {
                holding.countDown();
                try {
                    proceed.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                var elsewhere = manager.restore(id);
                elsewhere.setSection(2);
                elsewhere.setOrdinal(0);
                manager.update(elsewhere);
            }));
            holder.start();
            holding.await();

            var outcome = new CrudMove.Outcome[1];
            var mover = new Thread(() -> outcome[0] = move.moveCurrent(null, id, false));
            mover.start();
            // long enough for the move to have read where the row sat and to
            // be waiting for the list that it's about to lose
            Thread.sleep(500);
            proceed.countDown();

            holder.join(30_000);
            mover.join(30_000);
            assertFalse(holder.isAlive());
            assertFalse(mover.isAlive());

            // the move never happened, which isn't the same as the row
            // having been at the end of its list
            assertEquals(CrudMove.Outcome.CONFLICT, outcome[0]);
        } finally {
            manager.remove();
        }
    }

    @Test
    void testANullableBooleanKeepsHavingNoAnswer() {
        var priced = GenericQueryManagerFactory.instance(datasource_, CrudPriced.class);
        try {
            priced.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        priced.install();
        try {
            var instance = new CrudPriced();
            instance.setName("Widget");
            instance.setTier(1000);
            instance.setApproved(null);
            var id = priced.insert(instance);

            var conversation = new MockConversation(new Site() {
                public void setup() {
                    group("/admin", new CrudAdmin(datasource_).entity(CrudPriced.class));
                }
            });

            // a boolean that can hold nothing gets a control that can say so,
            // while one that can't keeps its checkbox
            var form = conversation.doRequest("/admin/crudPriced/edit/" + id).getText();
            assertTrue(form.contains("<select class=\"crud-select\" id=\"crud-approved\""), form);
            assertTrue(form.contains("type=\"checkbox\" class=\"crud-checkbox\" id=\"crud-listedInShop\""), form);

            // a format writes what the browse table reads, which no control
            // can submit, so the state comes from what is stored instead
            var answered = priced.restore(id);
            answered.setApproved(Boolean.TRUE);
            answered.setListedInShop(true);
            priced.update(answered);
            var decided = conversation.doRequest("/admin/crudPriced/edit/" + id).getText();
            assertTrue(decided.contains("<option value=\"true\" selected"), decided);
            assertTrue(decided.contains("type=\"checkbox\" class=\"crud-checkbox\" id=\"crud-listedInShop\" name=\"listedInShop\" value=\"true\" checked"), decided);

            answered.setApproved(null);
            answered.setListedInShop(false);
            priced.update(answered);

            // editing another property doesn't decide what was left open
            conversation.doRequest("/admin/crudPriced/edit/" + id, new MockRequest()
                .method(RequestMethod.POST)
                .parameter("name", "Renamed")
                .parameter("price", "$12.00")
                .parameter("tier", "$1,000")
                .parameter("approved", ""));

            var stored = priced.restore(id);
            assertEquals("Renamed", stored.getName());
            assertNull(stored.getApproved());

            // and it can still be answered
            conversation.doRequest("/admin/crudPriced/edit/" + id, new MockRequest()
                .method(RequestMethod.POST)
                .parameter("name", "Renamed")
                .parameter("price", "$12.00")
                .parameter("tier", "$1,000")
                .parameter("approved", "true"));

            assertEquals(Boolean.TRUE, priced.restore(id).getApproved());
        } finally {
            priced.remove();
        }
    }

    @Test
    void testAnInstanceIsAddedWhenItsIdentifierCarriesConstraints() {
        var ranged = GenericQueryManagerFactory.instance(datasource_, CrudRanged.class);
        try {
            ranged.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        ranged.install();
        try {
            var conversation = new MockConversation(new Site() {
                public void setup() {
                    group("/admin", new CrudAdmin(datasource_).entity(CrudRanged.class));
                }
            });

            // the instance says that it isn't stored yet while it's being
            // validated, which the constraints of the identifier would refuse
            // on a field that no form shows
            var response = conversation.doRequest("/admin/crudRanged/add", new MockRequest()
                .method(RequestMethod.POST)
                .parameter("title", "Added"));

            assertNotNull(response.getHeader("Location"), response.getText());
            var stored = ranged.restore();
            assertEquals(1, stored.size());
            assertEquals("Added", stored.get(0).getTitle());
        } finally {
            ranged.remove();
        }
    }

    @Test
    void testAFormattedBooleanChoiceIsReadableAndStillComesBack() {
        var priced = GenericQueryManagerFactory.instance(datasource_, CrudPriced.class);
        try {
            priced.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        priced.install();
        try {
            var instance = new CrudPriced();
            instance.setName("Widget");
            instance.setTier(1000);
            instance.setShipping(Boolean.TRUE);
            var id = priced.insert(instance);

            var conversation = new MockConversation(new Site() {
                public void setup() {
                    group("/admin", new CrudAdmin(datasource_).entity(CrudPriced.class));
                }
            });
            var text = conversation.doRequest("/admin/crudPriced/edit/" + id).getText();

            // a boolean is read back from its own literal, so the format only
            // decides what the option says, not what it submits
            assertTrue(text.contains("<option value=\"true\" selected>Yes</option>"), text);
            assertTrue(text.contains("<option value=\"false\">No</option>"), text);

            // the choice that can also hold nothing reads the same way, since
            // it's the format of the property that says how it reads
            assertTrue(text.contains("<select class=\"crud-select\" id=\"crud-approved\""), text);
            assertTrue(text.contains("<option value=\"true\">Approved</option>"), text);
            assertTrue(text.contains("<option value=\"false\">Refused</option>"), text);

            // and the literal that the form offers stores what it says
            conversation.doRequest("/admin/crudPriced/edit/" + id, new MockRequest()
                .method(RequestMethod.POST)
                .parameter("name", "Widget")
                .parameter("price", "$0.00")
                .parameter("tier", "$1,000")
                .parameter("shipping", "false"));
            assertEquals(Boolean.FALSE, priced.restore(id).getShipping());
        } finally {
            priced.remove();
        }
    }

    @Test
    void testAFormattedChoiceIsTheOptionThatIsSelected() {
        var priced = GenericQueryManagerFactory.instance(datasource_, CrudPriced.class);
        try {
            priced.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        priced.install();
        try {
            var instance = new CrudPriced();
            instance.setName("Widget");
            instance.setTier(2000);
            instance.setApproved(Boolean.TRUE);
            var id = priced.insert(instance);

            var conversation = new MockConversation(new Site() {
                public void setup() {
                    group("/admin", new CrudAdmin(datasource_).entity(CrudPriced.class));
                }
            });
            var text = conversation.doRequest("/admin/crudPriced/edit/" + id).getText();

            // the options are written the way the format writes them, which
            // is both what marks the stored one as selected and what can be
            // submitted back
            assertTrue(text.contains("<option value=\"$2,000\" selected"), text);
            assertFalse(text.contains("<option value=\"$1,000\" selected"), text);

            // and what the form offers is accepted when it comes back
            var response = conversation.doRequest("/admin/crudPriced/edit/" + id, new MockRequest()
                .method(RequestMethod.POST)
                .parameter("name", "Widget")
                .parameter("price", "$0.00")
                .parameter("tier", "$1,000"));
            assertTrue(response.getHeader("Location") != null, response.getText());
            assertEquals(1000, priced.restore(id).getTier());
        } finally {
            priced.remove();
        }
    }

    @Test
    void testAddShowsAFormThatFollowsTheConstraints() {
        var text = conversation_.doRequest("/admin/crudArticle/add").getText();

        assertTrue(text.contains("Add Article"));
        assertTrue(text.contains("name=\"title\""));
        assertTrue(text.contains("<select class=\"crud-select\" id=\"crud-status\""));
        assertTrue(text.contains("type=\"checkbox\" class=\"crud-checkbox\" id=\"crud-featured\""));
        // the identifier and the ordinal are managed, not submitted
        assertFalse(text.contains("name=\"id\""));
        assertFalse(text.contains("name=\"ordinal\""));
    }

    @Test
    void testTwoPeopleEditingReconcileKnowingly() {
        var id = addArticle("Original title", "draft", 1);

        // both people open the same form
        var first_form = conversation_.doRequest("/admin/crudArticle/edit/" + id).getText();
        var baseline = java.util.regex.Pattern.compile("name=\"crudBaseline\" value=\"([0-9a-f]+)\"").matcher(first_form);
        assertTrue(baseline.find(), first_form);

        // the second one saves
        conversation_.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("crudBaseline", baseline.group(1))
            .parameter("title", "Original title")
            .parameter("author", "Somebody else")
            .parameter("status", "draft"));
        assertEquals("Somebody else", articles_.restore(id).getAuthor());

        // the first one submits a form that was written on top of something
        // that isn't stored anymore
        var refused = conversation_.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("crudBaseline", baseline.group(1))
            .parameter("title", "My new title")
            .parameter("author", "Geert")
            .parameter("status", "draft")).getText();

        // nothing was overwritten and the form says what happened
        assertEquals("Somebody else", articles_.restore(id).getAuthor());
        assertEquals("Original title", articles_.restore(id).getTitle());
        assertTrue(refused.contains("was changed while you were editing it"), refused);
        // each field that differs says what is stored right now
        assertTrue(refused.contains("Now stored: Somebody else"), refused);
        assertTrue(refused.contains("Now stored: Original title"), refused);
        // while what was typed is kept, so nothing has to be entered again
        assertTrue(refused.contains("value=\"My new title\""), refused);

        // the form carries a fresh digest, so saving again overwrites
        // knowingly
        var fresh = java.util.regex.Pattern.compile("name=\"crudBaseline\" value=\"([0-9a-f]+)\"").matcher(refused);
        assertTrue(fresh.find(), refused);
        assertNotEquals(baseline.group(1), fresh.group(1));
        conversation_.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("crudBaseline", fresh.group(1))
            .parameter("title", "My new title")
            .parameter("author", "Geert")
            .parameter("status", "draft"));
        assertEquals("My new title", articles_.restore(id).getTitle());
        assertEquals("Geert", articles_.restore(id).getAuthor());
    }

    @Test
    void testASaveThatLandsDuringTheSubmissionIsStillNoticed() {
        var datasource = datasource_;
        var hook_runs = new int[1];
        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource).entity(CrudArticle.class, e -> e
                    // simulates somebody saving between the form's own check
                    // and the transaction of the update
                    .beforeUpdate((c, article) -> {
                        hook_runs[0]++;
                        var manager = GenericQueryManagerFactory.instance(datasource, CrudArticle.class);
                        var other = manager.restore(article.getId());
                        other.setAuthor("Somebody else");
                        manager.save(other);
                        return null;
                    })));
            }
        });
        var id = addArticle("Original", "draft", 1);
        var form = conversation.doRequest("/admin/crudArticle/edit/" + id).getText();
        var baseline = java.util.regex.Pattern.compile("name=\"crudBaseline\" value=\"([0-9a-f]+)\"").matcher(form);
        assertTrue(baseline.find(), form);

        var refused = conversation.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("crudBaseline", baseline.group(1))
            .parameter("title", "Changed")
            .parameter("author", "Geert")
            .parameter("status", "draft")).getText();

        // the digest is checked again inside the transaction of the update,
        // so the save that slipped in between is still noticed
        assertTrue(refused.contains("was changed while you were editing it"), refused);
        assertEquals("Original", articles_.restore(id).getTitle());
        assertTrue(refused.contains("Now stored: Somebody else"), refused);
        assertEquals(1, hook_runs[0]);

        // while a submission that is already known to conflict is refused
        // before the hooks run at all
        conversation.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("crudBaseline", baseline.group(1))
            .parameter("title", "Changed")
            .parameter("author", "Geert")
            .parameter("status", "draft"));
        assertEquals(1, hook_runs[0]);
    }

    @Test
    void testAnUpdateWritesWhatItDidntAskAboutAsTheRowHoldsItNow() {
        var moderated = GenericQueryManagerFactory.instance(datasource_, rifetestmodels.CrudModerated.class);
        try {
            moderated.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        moderated.install();
        try {
            var instance = new rifetestmodels.CrudModerated();
            instance.setTitle("Fresh");
            instance.setModeration("pending");
            var id = moderated.insert(instance);

            var datasource = datasource_;
            var conversation = new MockConversation(new Site() {
                public void setup() {
                    group("/admin", new CrudAdmin(datasource).entity(rifetestmodels.CrudModerated.class, e -> e
                        // simulates moderation landing between the request's
                        // own restore and the transaction of the update
                        .beforeUpdate((c, edited) -> {
                            var manager = GenericQueryManagerFactory.instance(datasource, rifetestmodels.CrudModerated.class);
                            var other = manager.restore(edited.getId());
                            other.setModeration("approved");
                            manager.save(other);
                            return null;
                        })));
                }
            });

            // the form never enters the moderation, so the digest doesn't
            // change and the edit is allowed to proceed
            var form = conversation.doRequest("/admin/crudModerated/edit/" + id).getText();
            var baseline = java.util.regex.Pattern.compile("name=\"crudBaseline\" value=\"([0-9a-f]+)\"").matcher(form);
            assertTrue(baseline.find(), form);

            var saved = conversation.doRequest("/admin/crudModerated/edit/" + id, new MockRequest()
                .method(RequestMethod.POST)
                .parameter("crudBaseline", baseline.group(1))
                .parameter("title", "Retitled"));
            assertTrue(saved.getHeader("Location").endsWith("/admin/crudModerated"), saved.getText());

            // the edit wrote its own field and carried the moderation over
            // as the row held it at the update, not as the request found it
            var stored = moderated.restore(id);
            assertEquals("Retitled", stored.getTitle());
            assertEquals("approved", stored.getModeration());
        } finally {
            try {
                moderated.remove();
            } catch (Exception e) {
                // already gone
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAnActionHoldsTheListItActsIn() throws Exception {
        var first = addArticle("First", "draft", 0);
        var second = addArticle("Second", "draft", 1);

        var entered = new CountDownLatch(1);
        var proceed = new CountDownLatch(1);
        var admin = new CrudAdmin(datasource_).entity(CrudArticle.class, e -> e
            .action("hold", "Hold", (c, article) -> {
                entered.countDown();
                try {
                    proceed.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                article.setAuthor("acted");
                GenericQueryManagerFactory.instance(datasource_, CrudArticle.class).save(article);
                return null;
            }));
        var entity = (CrudEntity<CrudArticle>) admin.getEntities().get(0);
        var perform = new CrudPerform<>(admin, entity, entity.getOptions().getActions().get(0));
        var move = new CrudMove<>(admin, entity);
        var restriction = entity.createOrdinalSequence(datasource_).storedPlacement(first).restriction();

        var actor = new Thread(() -> perform.performHeld(null, articles_.restore(first), restriction));
        actor.start();
        entered.await();

        var outcome = new CrudMove.Outcome[1];
        var mover = new Thread(() -> outcome[0] = move.moveCurrent(null, first, false));
        mover.start();
        // long enough for the move to have reached the list that the action
        // is holding
        Thread.sleep(500);
        // the move waits for the action instead of interleaving with it
        assertTrue(mover.isAlive());
        assertNull(outcome[0]);

        proceed.countDown();
        actor.join(30_000);
        mover.join(30_000);
        assertFalse(actor.isAlive());
        assertFalse(mover.isAlive());

        // the move that waited still happened, on what the action left behind
        assertEquals(CrudMove.Outcome.MOVED, outcome[0]);
        assertEquals("acted", articles_.restore(first).getAuthor());
        assertTrue(articles_.restore(second).getOrdinal() < articles_.restore(first).getOrdinal());
    }

    @Test
    void testRowOperationsComeBackToThePageTheyCameFrom() {
        addArticle("Item E", "draft", 1);
        var item_d = addArticle("Item D", "draft", 2);
        addArticle("Item C", "draft", 3);
        addArticle("Item B", "draft", 4);
        addArticle("Item A", "draft", 5);

        var page_two = "search=item&sort=title&offset=3";
        var browse = conversation_.doRequest("/admin/crudArticle?" + page_two).getText();

        // the operations of a row carry the state of the page they sit on
        var edit_link = java.util.regex.Pattern.compile("href=\"([^\"]*/edit/[0-9]+[^\"]*)\"").matcher(browse);
        assertTrue(edit_link.find(), browse);
        var carried = edit_link.group(1);
        assertTrue(carried.contains("search=item"), carried);
        assertTrue(carried.contains("sort=title"), carried);
        assertTrue(carried.contains("offset=3"), carried);
        var delete_link = java.util.regex.Pattern.compile("href=\"([^\"]*/delete/[0-9]+[^\"]*)\"").matcher(browse);
        assertTrue(delete_link.find(), browse);
        assertTrue(delete_link.group(1).contains("offset=3"), delete_link.group(1));

        // the form of an operation keeps that state, and so does its way back
        var form = conversation_.doRequest("/admin/crudArticle/edit/" + item_d + "?" + page_two).getText();
        assertTrue(form.contains("/edit/" + item_d + "?search=item&amp;sort=title&amp;offset=3"), form);
        assertTrue(form.contains("crudArticle?search=item&amp;sort=title&amp;offset=3"), form);

        // finishing the operation lands back on the page it came from
        var saved = conversation_.doRequest("/admin/crudArticle/edit/" + item_d + "?" + page_two, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Item D")
            .parameter("author", "Geert")
            .parameter("status", "draft"));
        var location = saved.getHeader("Location");
        assertTrue(location.contains("search=item") && location.contains("sort=title") && location.contains("offset=3"), location);

        // and so does performing an action on it
        var performed = conversation_.doRequest("/admin/crudArticle/action/publish/" + item_d + "?" + page_two,
            new MockRequest().method(RequestMethod.POST));
        var action_location = performed.getHeader("Location");
        assertTrue(action_location.contains("search=item") && action_location.contains("offset=3"), action_location);
    }

    @Test
    void testAMoveKeepsThePageItWasMadeOn() {
        addArticle("First", "draft", 1);
        addArticle("Second", "draft", 2);
        addArticle("Third", "draft", 3);
        var fourth = addArticle("Fourth", "draft", 4);

        var paged = conversation_.doRequest("/admin/crudArticle?offset=3").getText();
        var move_form = java.util.regex.Pattern.compile("action=\"([^\"]*/move/[0-9]+/up[^\"]*)\"").matcher(paged);
        assertTrue(move_form.find(), paged);
        assertTrue(move_form.group(1).contains("offset=3"), move_form.group(1));

        var moved = post("/admin/crudArticle/move/" + fourth + "/up?offset=3");
        assertTrue(moved.getHeader("Location").contains("offset=3"), moved.getHeader("Location"));
    }

    @Test
    void testANativeDateControlRoundTrips() throws Exception {
        var scheduled = GenericQueryManagerFactory.instance(datasource_, rifetestmodels.CrudScheduled.class);
        try {
            scheduled.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        scheduled.install();
        try {
            var conversation = new MockConversation(new Site() {
                public void setup() {
                    group("/admin", new CrudAdmin(datasource_).entity(rifetestmodels.CrudScheduled.class));
                }
            });

            // the format of the property is exactly what the control hands
            // back, so the control is offered
            var form = conversation.doRequest("/admin/crudScheduled/add").getText();
            assertTrue(form.contains("name=\"publishAt\" type=\"datetime-local\""), form);

            conversation.doRequest("/admin/crudScheduled/add", new MockRequest()
                .method(RequestMethod.POST)
                .parameter("title", "Launch")
                .parameter("publishAt", "2026-08-23T14:30"));

            var stored = scheduled.restore().get(0);
            var format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            assertEquals("2026-08-23T14:30", format.format(stored.getPublishAt()));

            // and what was stored comes back the way the control shows it
            var edit = conversation.doRequest("/admin/crudScheduled/edit/" + stored.getId()).getText();
            assertTrue(edit.contains("name=\"publishAt\" type=\"datetime-local\" value=\"2026-08-23T14:30\""), edit);
        } finally {
            try {
                scheduled.remove();
            } catch (Exception e) {
                // already gone
            }
        }
    }

    @Test
    void testSearchFiltersTheBrowseTable() {
        addArticle("Alpha news", "draft", 1);
        addArticle("Beta story", "draft", 2);
        addArticle("Gamma alpha", "published", 3);

        // a search matches without regard for case, in any column that
        // holds text
        var filtered = conversation_.doRequest("/admin/crudArticle?search=ALPHA").getText();
        assertTrue(filtered.contains("Alpha news"), filtered);
        assertTrue(filtered.contains("Gamma alpha"), filtered);
        assertFalse(filtered.contains("Beta story"), filtered);
        // the box still holds what was searched for
        assertTrue(filtered.contains("name=\"search\" value=\"ALPHA\""), filtered);
        // and the rows can't be moved between neighbours the page doesn't show
        assertFalse(filtered.contains("Move up"), filtered);

        var by_status = conversation_.doRequest("/admin/crudArticle?search=published").getText();
        assertTrue(by_status.contains("Gamma alpha"), by_status);
        assertFalse(by_status.contains("Alpha news"), by_status);

        // a search that matches nothing isn't an entity that has nothing
        var nothing = conversation_.doRequest("/admin/crudArticle?search=zzz").getText();
        assertTrue(nothing.contains("No Articles match 'zzz'."), nothing);
        assertTrue(nothing.contains("Show all"), nothing);
        assertTrue(nothing.contains("name=\"search\""), nothing);

        // what was typed comes back as text, wherever it's shown
        var typed = conversation_.doRequest("/admin/crudArticle?search=%3Cb%3E").getText();
        assertTrue(typed.contains("value=\"&lt;b&gt;\""), typed);
        assertFalse(typed.contains("value=\"<b>\""), typed);

        // a quote is data, not the end of the query's own text
        addArticle("O'Reilly guide", "draft", 4);
        var quoted = conversation_.doRequest("/admin/crudArticle?search=o%27reilly").getText();
        assertTrue(quoted.contains("O'Reilly guide"), quoted);
    }

    @Test
    void testSortOrdersTheBrowseTable() {
        addArticle("Banana", "draft", 1);
        addArticle("Apple", "draft", 2);
        addArticle("Cherry", "draft", 3);

        // without a sort the ordinal is the order, and the rows can be moved
        var plain = conversation_.doRequest("/admin/crudArticle").getText();
        assertTrue(plain.indexOf("Banana") < plain.indexOf("Apple"), plain);
        assertTrue(plain.contains("Move up"), plain);
        // the headers say how to sort them
        assertTrue(plain.contains("sort=title"), plain);

        var ascending = conversation_.doRequest("/admin/crudArticle?sort=title").getText();
        assertTrue(ascending.indexOf("Apple") < ascending.indexOf("Banana"), ascending);
        assertTrue(ascending.indexOf("Banana") < ascending.indexOf("Cherry"), ascending);
        // the sorted header says which way it goes and offers the other one
        assertTrue(ascending.contains("aria-sort=\"ascending\""), ascending);
        assertTrue(ascending.contains("dir=desc"), ascending);
        // and the rows can't be moved between neighbours the page doesn't show
        assertFalse(ascending.contains("Move up"), ascending);

        var descending = conversation_.doRequest("/admin/crudArticle?sort=title&dir=desc").getText();
        assertTrue(descending.indexOf("Cherry") < descending.indexOf("Banana"), descending);
        assertTrue(descending.contains("aria-sort=\"descending\""), descending);

        // a sort that was typed into the URL falls back to the order that
        // the page always has
        var forged = conversation_.doRequest("/admin/crudArticle?sort=body").getText();
        assertTrue(forged.indexOf("Banana") < forged.indexOf("Apple"), forged);
        var unknown = conversation_.doRequest("/admin/crudArticle?sort=nonsense").getText();
        assertTrue(unknown.indexOf("Banana") < unknown.indexOf("Apple"), unknown);
    }

    @Test
    void testSearchAndSortRideAlongThePages() {
        addArticle("Item E", "draft", 1);
        addArticle("Item D", "draft", 2);
        addArticle("Item C", "draft", 3);
        addArticle("Item B", "draft", 4);
        addArticle("Item A", "draft", 5);
        addArticle("Other", "draft", 6);

        // the page size is 3, so the five matches span two pages
        var first = conversation_.doRequest("/admin/crudArticle?search=item&sort=title").getText();
        assertTrue(first.contains("Item A"), first);
        assertTrue(first.contains("Item C"), first);
        assertFalse(first.contains("Item D"), first);
        assertFalse(first.contains("Other"), first);
        // the links to the other pages keep the search and the sort
        var next = java.util.regex.Pattern.compile("href=\"([^\"]*offset=3[^\"]*)\"").matcher(first);
        assertTrue(next.find(), first);
        assertTrue(next.group(1).contains("search=item"), next.group(1));
        assertTrue(next.group(1).contains("sort=title"), next.group(1));
        // and a new search keeps the sort, as the form's own fields
        assertTrue(first.contains("name=\"sort\" value=\"title\""), first);

        var second = conversation_.doRequest("/admin/crudArticle?search=item&sort=title&offset=3").getText();
        assertTrue(second.contains("Item D"), second);
        assertTrue(second.contains("Item E"), second);
        assertFalse(second.contains("Item A"), second);
        assertFalse(second.contains("Other"), second);
    }

    @Test
    void testAColumnShowsWhatItsRendererDecides() {
        addArticle("First <one>", "draft", 1);
        addArticle("Second", "published", 2);

        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    .entity(CrudArticle.class, e -> e
                        .column("status", (article, value) -> CrudCell.badge(value, article.getStatus()))
                        .column("author", (article, value) -> CrudCell.badge("<" + value + ">"))
                        .column("title", (article, value) ->
                            "published".equals(article.getStatus()) ? CrudCell.text(value + " (live)") : null)
                        .column("featured", (article, value) -> CrudCell.html("<progress max=\"2\" value=\"1\"></progress>"))));
            }
        });
        var browse = conversation.doRequest("/admin/crudArticle").getText();

        // a badge wraps the text in a marker that its style names
        assertTrue(browse.contains("<td><span class=\"crud-badge crud-badge-draft\">draft</span></td>"), browse);
        assertTrue(browse.contains("<td><span class=\"crud-badge crud-badge-published\">published</span></td>"), browse);
        // a badge without a style isn't set apart, and what it says is text
        assertTrue(browse.contains("<td><span class=\"crud-badge\">&lt;Geert&gt;</span></td>"), browse);
        // a renderer that returns null shows the formatted value after all
        assertTrue(browse.contains("<td>First &lt;one&gt;</td>"), browse);
        assertTrue(browse.contains("<td>Second (live)</td>"), browse);
        // markup is written as it is
        assertTrue(browse.contains("<td><progress max=\"2\" value=\"1\"></progress></td>"), browse);
    }

    @Test
    void testAddStoresTheInstanceAndRedirects() {
        var request = new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "A brand new article")
            .parameter("author", "Geert")
            .parameter("status", "draft");
        var response = conversation_.doRequest("/admin/crudArticle/add", request);

        // the response of a successful submission is a redirect
        assertTrue(response.getHeader("Location").endsWith("/admin/crudArticle"));

        var stored = articles_.restore();
        assertEquals(1, stored.size());
        assertEquals("A brand new article", stored.get(0).getTitle());

        // the confirmation is shown on the page that follows
        var browse = conversation_.doRequest("/admin/crudArticle").getText();
        assertTrue(browse.contains("Article has been added."));
    }

    @Test
    void testAddRefusesAnInvalidSubmission() {
        var request = new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "")
            .parameter("author", "Geert");
        var text = conversation_.doRequest("/admin/crudArticle/add", request).getText();

        // the form comes back with the error marked instead of a redirect
        assertTrue(text.contains("crud-errors"));
        assertTrue(text.contains("Title is required."));
        assertTrue(text.contains("crud-invalid"));
        // what was already filled in isn't lost
        assertTrue(text.contains("value=\"Geert\""));
        assertEquals(0, articles_.count());
    }

    @Test
    void testAddAssignsTheNextOrdinal() {
        conversation_.doRequest("/admin/crudArticle/add", new MockRequest()
            .method(RequestMethod.POST).parameter("title", "First").parameter("status", "draft"));
        conversation_.doRequest("/admin/crudArticle/add", new MockRequest()
            .method(RequestMethod.POST).parameter("title", "Second").parameter("status", "draft"));

        var stored = articles_.restore(articles_.getRestoreQuery().orderBy("ordinal"));
        assertEquals(2, stored.size());
        assertEquals("First", stored.get(0).getTitle());
        assertEquals("Second", stored.get(1).getTitle());
        assertTrue(stored.get(0).getOrdinal() < stored.get(1).getOrdinal());
    }

    @Test
    void testEditRestoresAndUpdates() {
        var id = addArticle("Original", "draft", 0);

        var form = conversation_.doRequest("/admin/crudArticle/edit/" + id).getText();
        assertTrue(form.contains("Edit Article"));
        assertTrue(form.contains("value=\"Original\""));

        var response = conversation_.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Updated")
            .parameter("author", "Geert")
            .parameter("status", "published"));
        assertTrue(response.getHeader("Location").endsWith("/admin/crudArticle"));

        var stored = articles_.restore(id);
        assertEquals("Updated", stored.getTitle());
        assertEquals("published", stored.getStatus());
        // the ordinal isn't part of the form and survives the update
        assertEquals(0, stored.getOrdinal());
    }

    @Test
    void testEditIgnoresAForgedIdentifier() {
        var first = addArticle("First", "draft", 0);
        var second = addArticle("Second", "draft", 1);

        // the identifier comes from the URL, a submitted one can't retarget
        // the update
        conversation_.doRequest("/admin/crudArticle/edit/" + first, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("id", String.valueOf(second))
            .parameter("title", "Overwritten")
            .parameter("status", "draft"));

        assertEquals("Overwritten", articles_.restore(first).getTitle());
        assertEquals("Second", articles_.restore(second).getTitle());
    }

    @Test
    void testEditOfAMissingInstanceLandsOnTheList() {
        var response = conversation_.doRequest("/admin/crudArticle/edit/9999");
        assertTrue(response.getHeader("Location").endsWith("/admin/crudArticle"));

        var browse = conversation_.doRequest("/admin/crudArticle").getText();
        assertTrue(browse.contains("no longer exists"));
    }

    @Test
    void testDeleteConfirmsBeforeItRemoves() {
        var id = addArticle("Doomed", "draft", 0);

        var confirm = conversation_.doRequest("/admin/crudArticle/delete/" + id).getText();
        assertTrue(confirm.contains("Delete Article"));
        assertTrue(confirm.contains("Doomed"));
        assertTrue(confirm.contains("can't be undone"));
        // nothing is removed by looking at the confirmation
        assertEquals(1, articles_.count());

        var response = post("/admin/crudArticle/delete/" + id);
        assertTrue(response.getHeader("Location").endsWith("/admin/crudArticle"));
        assertEquals(0, articles_.count());
    }

    @Test
    void testDeleteIsRefusedForAProtectedInstance() {
        // this administration refuses to delete published articles
        var id = addArticle("Published", "published", 0);

        var response = post("/admin/crudArticle/delete/" + id);
        assertTrue(response.getHeader("Location").endsWith("/admin/crudArticle"));
        assertEquals(1, articles_.count());

        var browse = conversation_.doRequest("/admin/crudArticle").getText();
        assertTrue(browse.contains("can't be deleted"));
        // the button isn't offered either
        assertFalse(browse.contains("/delete/" + id));
    }

    @Test
    void testMoveReordersTheInstances() {
        var first = addArticle("First", "draft", 0);
        var second = addArticle("Second", "draft", 1);

        var response = post("/admin/crudArticle/move/" + second + "/up");
        assertTrue(response.getHeader("Location").endsWith("/admin/crudArticle"));

        var ordered = articles_.restore(articles_.getRestoreQuery().orderBy("ordinal"));
        assertEquals("Second", ordered.get(0).getTitle());
        assertEquals("First", ordered.get(1).getTitle());

        post("/admin/crudArticle/move/" + second + "/down");
        ordered = articles_.restore(articles_.getRestoreQuery().orderBy("ordinal"));
        assertEquals("First", ordered.get(0).getTitle());
        assertEquals("Second", ordered.get(1).getTitle());
    }

    @Test
    void testMoveButtonsAreDisabledAtTheEnds() {
        addArticle("First", "draft", 0);
        addArticle("Second", "draft", 1);

        var text = conversation_.doRequest("/admin/crudArticle").getText();
        // the first row can't move up and the last row can't move down
        assertTrue(text.contains("crud-disabled"));
        assertTrue(text.contains("/move/"));
    }

    @Test
    void testCustomActionRunsAndReportsBack() {
        var id = addArticle("Draft article", "draft", 0);

        var browse = conversation_.doRequest("/admin/crudArticle").getText();
        assertTrue(browse.contains("Publish"));

        var response = post("/admin/crudArticle/action/publish/" + id);
        assertTrue(response.getHeader("Location").endsWith("/admin/crudArticle"));
        assertEquals("published", articles_.restore(id).getStatus());

        var after = conversation_.doRequest("/admin/crudArticle").getText();
        assertTrue(after.contains("The article has been published."));
        // the action is no longer offered for a published article
        assertFalse(after.contains("/action/publish/" + id));
    }

    @Test
    void testCustomActionIsRefusedWhenItDoesntApply() {
        var id = addArticle("Published", "published", 0);

        var response = post("/admin/crudArticle/action/publish/" + id);
        assertTrue(response.getHeader("Location").endsWith("/admin/crudArticle"));
        // the status is untouched since the action doesn't apply
        assertEquals("published", articles_.restore(id).getStatus());
    }

    @Test
    void testTheStylesAreHandedOutByTheAdministrationItself() {
        // everything ships in one jar, so the styles are handed out by a route
        // of the administration rather than by whatever serves files
        var page = conversation_.doRequest("/admin/crudArticle").getText();
        assertFalse(page.contains("<style>"), page);

        var url = page.replaceAll("(?s).*<link rel=\"stylesheet\" href=\"([^\"]+)\".*", "$1");
        assertTrue(url.contains("/admin/style-"), url);
        assertTrue(url.endsWith(".css"), url);

        var styles = conversation_.doRequest(url);
        assertTrue(styles.getText().contains(".crud-admin"), styles.getText());
        assertTrue(styles.getContentType().startsWith("text/css"), styles.getContentType());
        // the name of it holds what it holds, so it never has to be asked for
        // a second time
        assertTrue(styles.getHeader("Cache-Control").contains("immutable"), styles.getHeader("Cache-Control"));

        // and the styles say what a page of an application can change without
        // replacing anything of the administration
        assertTrue(styles.getText().contains("--crud-accent"), "the custom properties are the way to restyle this");
    }

    @Test
    void testASubmissionWithoutTheFormsDigestOverwrites() {
        // the form itself carries the digest that notices somebody else's
        // save, so this is about what posts directly, like a script
        var id = addArticle("Original", "draft", 0);

        conversation_.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Renamed")
            .parameter("author", "Geert")
            .parameter("status", "draft"));
        assertEquals("Renamed", articles_.restore(id).getTitle());

        // a submission that doesn't say what it was written on top of has
        // nothing to check against, so what it submits is what gets
        // stored and the rename is gone
        conversation_.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Original")
            .parameter("author", "Geert")
            .parameter("status", "published"));

        var stored = articles_.restore(id);
        assertEquals("published", stored.getStatus());
        assertEquals("Original", stored.getTitle(), "the last submission wins over the whole instance");
    }

    @Test
    void testTheThingsAnApplicationTypicallyWantsToChange() {
        // an application that brands the administration, tightens the table of
        // one entity, restyles the controls of the forms and adds a font and
        // an icon of its own
        var id = addArticle("Styled", "draft", 0);
        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    .head("<link rel=\"icon\" href=\"/favicon.svg\">")
                    .head("<link rel=\"stylesheet\" href=\"/css/admin.css\">")
                    .entity(CrudArticle.class));
            }
        });

        var pages = new java.util.LinkedHashMap<String, String>();
        pages.put("dashboard", "/admin/");
        pages.put("browse", "/admin/crudArticle");
        pages.put("add", "/admin/crudArticle/add");
        pages.put("edit", "/admin/crudArticle/edit/" + id);
        pages.put("delete", "/admin/crudArticle/delete/" + id);

        for (var page : pages.entrySet()) {
            var html = conversation.doRequest(page.getValue()).getText();
            // the properties that recolour everything are declared on
            // something that every page carries
            assertTrue(html.contains("class=\"crud-admin"), page.getKey() + ": " + html);
            // and what the application adds is on every page as well
            assertTrue(html.contains("/favicon.svg"), page.getKey());
            assertTrue(html.contains("/css/admin.css"), page.getKey());
        }

        // .crud-entity-crudArticle .crud-table td { ... }
        var browse = conversation.doRequest("/admin/crudArticle").getText();
        assertTrue(browse.contains("crud-entity-crudArticle"), browse);
        assertTrue(browse.contains("class=\"crud-table\""), browse);

        // .crud-select { ... } and #crud-title { ... }
        var add = conversation.doRequest("/admin/crudArticle/add").getText();
        assertTrue(add.contains("<select class=\"crud-select\""), add);
        assertTrue(add.contains("id=\"crud-title\""), add);

        // .crud-page-edit .crud-form { ... }, since an addition and a change
        // are otherwise the same form to anything that styles them
        var edit = conversation.doRequest("/admin/crudArticle/edit/" + id).getText();
        var form = "class=\"crud-form\"";
        assertTrue(add.contains(form) && edit.contains(form));
        assertTrue(add.contains("crud-page-add"), add);
        assertTrue(edit.contains("crud-page-edit"), edit);
        assertFalse(add.contains("crud-page-edit"), add);

        // and every other kind of page says which one it is as well
        assertTrue(conversation.doRequest("/admin/").getText().contains("crud-page-dashboard"));
        assertTrue(browse.contains("crud-page-browse"), browse);
        assertTrue(conversation.doRequest("/admin/crudArticle/delete/" + id).getText().contains("crud-page-delete"));
    }

    @Test
    void testTheUrlOfTheStylesCantBreakOutOfThePage() {
        // the url of them holds the host that the request carried, which is
        // nothing that the page decided and everything that a request is able
        // to make up
        var conversation = new MockConversation(site_);
        conversation.setServerName("evil\"><script>alert(1)</script><x y=\"");

        var page = conversation.doRequest("/admin/crudArticle").getText();
        assertFalse(page.contains("<script>alert(1)</script>"), page);
        assertTrue(page.contains("&lt;script&gt;"), page);
    }

    @Test
    void testAPageCarriesWhatAnApplicationAddsToIt() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    .head("<link rel=\"stylesheet\" href=\"/css/mine.css\">")
                    .head("<meta name=\"robots\" content=\"noindex\">")
                    .entity(CrudArticle.class));
            }
        });

        var page = conversation.doRequest("/admin/crudArticle").getText();
        assertTrue(page.contains("<link rel=\"stylesheet\" href=\"/css/mine.css\">"), page);
        assertTrue(page.contains("<meta name=\"robots\" content=\"noindex\">"), page);

        // the entity that a page belongs to is on the page, so the styles of
        // an application can reach one of them without reaching the others
        assertTrue(page.contains("class=\"crud-admin crud-entity-crudArticle crud-page-browse\""), page);
        // while the dashboard belongs to none of them
        assertTrue(conversation.doRequest("/admin/").getText().contains("class=\"crud-admin crud-page-dashboard\""));
    }

    @Test
    void testARefusalReadsTheSameWhateverTheLocaleIs() {
        var id = addArticle("Published", "published", 0);

        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    .entity(CrudArticle.class, e -> e
                        .label("Item")
                        .action("index", "Index", (c, article) -> "Indexed.",
                            a -> a.visibleWhen(article -> false))));
            }
        });

        var previous = Locale.getDefault();
        try {
            // a Turkish locale lowercases an I to one without a dot, which
            // isn't a character that belongs in an English sentence
            Locale.setDefault(new Locale("tr", "TR"));
            conversation.doRequest("/admin/crudArticle/action/index/" + id,
                new MockRequest().method(RequestMethod.POST));
        } finally {
            Locale.setDefault(previous);
        }

        var browse = conversation.doRequest("/admin/crudArticle").getText();
        assertTrue(browse.contains("This Item can't be index."), browse);
    }

    @Test
    void testConfirmedActionAsksFirst() {
        var id = addArticle("Draft article", "draft", 0);

        var confirm = conversation_.doRequest("/admin/crudArticle/action/archive/" + id).getText();
        assertTrue(confirm.contains("Archive Article"));
        assertEquals("draft", articles_.restore(id).getStatus());

        post("/admin/crudArticle/action/archive/" + id);
        assertEquals("archived", articles_.restore(id).getStatus());
    }

    @Test
    void testDetailViewsUseTheHumanizedLabels() {
        var id = addArticle("Doomed", "draft", 0);

        // the details of a confirmation are labeled the way the columns and
        // the form fields are, not with the raw property names
        var text = conversation_.doRequest("/admin/crudArticle/delete/" + id).getText();
        assertTrue(text.contains("<dt>Title</dt>"));
        assertTrue(text.contains("<dt>Author</dt>"));
        assertFalse(text.contains("<dt>title</dt>"));
        assertFalse(text.contains("<dt>author</dt>"));
    }

    @Test
    void testConfirmedActionsAreRequestedWithoutSubmittingThem() {
        addArticle("Draft article", "draft", 0);

        var text = conversation_.doRequest("/admin/crudArticle").getText();

        // an action that confirms has to reach its confirmation page first,
        // so its button can't post the action right away
        var archive = text.indexOf("/action/archive/");
        var archive_form = text.lastIndexOf("<form", archive);
        assertTrue(text.substring(archive_form, archive).contains("method=\"get\""));

        // an action without a confirmation is posted directly
        var publish = text.indexOf("/action/publish/");
        var publish_form = text.lastIndexOf("<form", publish);
        assertTrue(text.substring(publish_form, publish).contains("method=\"post\""));
    }

    @Test
    void testAConfirmedActionKeepsThePageStateThroughItsGetForm() {
        // the archive action confirms, so its row button is a GET form; a
        // browser replaces the query string of a GET form's action with the
        // form's own fields, so the state has to ride along as those fields
        addArticle("Item E", "draft", 1);
        addArticle("Item D", "draft", 2);
        addArticle("Item C", "draft", 3);
        addArticle("Item B", "draft", 4);
        addArticle("Item A", "draft", 5);

        var browse = conversation_.doRequest("/admin/crudArticle?search=item&sort=title&offset=3").getText();

        // isolate the confirmed action's form
        var archive = browse.indexOf("/action/archive/");
        assertTrue(archive >= 0, browse);
        var form = browse.substring(browse.lastIndexOf("<form", archive), browse.indexOf("</form>", archive));
        assertTrue(form.contains("method=\"get\""), form);
        // the state a GET form would otherwise drop rides along as fields
        assertTrue(form.contains("<input type=\"hidden\" name=\"search\" value=\"item\">"), form);
        assertTrue(form.contains("<input type=\"hidden\" name=\"sort\" value=\"title\">"), form);
        assertTrue(form.contains("<input type=\"hidden\" name=\"offset\" value=\"3\">"), form);
    }

    @Test
    void testSearchMatchesWildcardCharactersLiterally() {
        addArticle("50% discount", "draft", 1);
        addArticle("500 discount", "draft", 2);
        addArticle("a_b marker", "draft", 3);
        addArticle("axb marker", "draft", 4);

        // a percent in the term matches a literal percent, not everything
        var percent = conversation_.doRequest("/admin/crudArticle?search=50%25").getText();
        assertTrue(percent.contains("50% discount"), percent);
        assertFalse(percent.contains("500 discount"), percent);

        // and an underscore matches a literal underscore, not any character
        var underscore = conversation_.doRequest("/admin/crudArticle?search=a_b").getText();
        assertTrue(underscore.contains("a_b marker"), underscore);
        assertFalse(underscore.contains("axb marker"), underscore);

        // the escape character itself is just text, not a way to escape out
        addArticle("bang! here", "draft", 5);
        var bang = conversation_.doRequest("/admin/crudArticle?search=bang%21").getText();
        assertTrue(bang.contains("bang! here"), bang);
    }

    @Test
    void testBrowseKeepsTheOperationsInSeparateCells() {
        addArticle("First", "draft", 0);

        var text = conversation_.doRequest("/admin/crudArticle").getText();

        // the ordering and the operations are their own columns, so that
        // they line up with the headers of the table
        assertTrue(text.contains("<td class=\"crud-move\">"));
        assertTrue(text.contains("<td class=\"crud-actions\">"));
        // and the cells of a row aren't laid out as flex containers, which
        // would take them out of the table layout
        assertFalse(text.contains(".crud-actions, .crud-move { display: flex"));
    }

    @Test
    void testSecondEntityIsAdministeredOnItsOwnSlug() {
        var subscriber = new CrudSubscriber();
        subscriber.setEmail("gbevin@uwyn.com");
        subscriber.setName("Geert");
        subscribers_.save(subscriber);

        var text = conversation_.doRequest("/admin/subscribers").getText();
        assertTrue(text.contains("gbevin@uwyn.com"));
        // an entity without an ordinal has no move column
        assertFalse(text.contains(">Order<"));
    }

    @Test
    void testUniqueConstraintIsReportedAsAFieldError() {
        var subscriber = new CrudSubscriber();
        subscriber.setEmail("gbevin@uwyn.com");
        subscribers_.save(subscriber);

        var text = conversation_.doRequest("/admin/subscribers/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("email", "gbevin@uwyn.com")
            .parameter("name", "Impostor")).getText();

        assertTrue(text.contains("already taken"));
        assertEquals(1, subscribers_.count());
    }

    @Test
    void testEditIgnoresAForgedOrdinal() {
        var first = addArticle("First", "draft", 0);
        var second = addArticle("Second", "draft", 1);

        // the ordinal is moved through its own operation, and being the
        // ordinal is what keeps it out of the form, not an editable
        // constraint
        conversation_.doRequest("/admin/crudArticle/edit/" + second, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("ordinal", "0")
            .parameter("title", "Second")
            .parameter("status", "draft"));

        assertEquals(0, articles_.restore(first).getOrdinal());
        assertEquals(1, articles_.restore(second).getOrdinal());
    }

    @Test
    void testEditTurnsOffAnUncheckedCheckbox() {
        var subscriber = new CrudSubscriber();
        subscriber.setEmail("gbevin@uwyn.com");
        subscriber.setActive(true);
        var id = subscribers_.save(subscriber);

        // browsers don't send a checkbox that isn't checked, so an absent
        // parameter is the only way to store false
        conversation_.doRequest("/admin/subscribers/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("email", "gbevin@uwyn.com"));

        assertFalse(subscribers_.restore(id).isActive());
    }

    @Test
    void testActionsAreOnlyPerformedThroughPost() {
        var id = addArticle("First", "draft", 0);

        // a crawler or a link preview that merely looks at the URL of an
        // action can't perform it
        var text = conversation_.doRequest("/admin/crudArticle/action/publish/" + id).getText();
        assertEquals("draft", articles_.restore(id).getStatus());
        assertTrue(text.contains("Publish Article"));

        conversation_.doRequest("/admin/crudArticle/action/publish/" + id, new MockRequest()
            .method(RequestMethod.POST));
        assertEquals("published", articles_.restore(id).getStatus());
    }

    @Test
    void testDefaultValueIsPreselectedWhenAdding() {
        var text = conversation_.doRequest("/admin/crudArticle/add").getText();
        assertTrue(text.contains("<option value=\"draft\" selected>"));
    }

    @Test
    void testStoredValueIsntReplacedByTheDefaultWhenEditing() {
        var article = article("First", null);
        article.setOrdinal(0);
        var id = articles_.save(article);

        // an empty value was stored that way on purpose
        var text = conversation_.doRequest("/admin/crudArticle/edit/" + id).getText();
        assertFalse(text.contains("<option value=\"draft\" selected>"));
    }

    @Test
    void testBrowsePagesThroughUnorderedInstancesWithoutRepeats() {
        for (var i = 0; i < 7; i++) {
            var subscriber = new CrudSubscriber();
            subscriber.setEmail("subscriber" + i + "@uwyn.com");
            subscribers_.save(subscriber);
        }

        // an unordered entity is ordered by its identifier, so that paging
        // shows each instance exactly once
        var seen = new java.util.HashSet<String>();
        for (var offset = 0; offset < 7; offset += 3) {
            var text = conversation_.doRequest("/admin/subscribers?offset=" + offset).getText();
            for (var i = 0; i < 7; i++) {
                if (text.contains("subscriber" + i + "@uwyn.com")) {
                    assertTrue(seen.add("subscriber" + i), "subscriber" + i + " was shown twice");
                }
            }
        }
        assertEquals(7, seen.size());
    }

    @Test
    void testNegativeOffsetLandsOnTheFirstPage() {
        addArticle("First", "draft", 0);

        var text = conversation_.doRequest("/admin/crudArticle?offset=-5").getText();
        assertTrue(text.contains("First"));
    }

    @Test
    void testDeleteLeavesTheRestInOrder() {
        var first = addArticle("First", "draft", 0);
        var second = addArticle("Second", "draft", 1);
        var third = addArticle("Third", "draft", 2);

        post("/admin/crudArticle/delete/" + first);

        // the gap that a deletion leaves behind doesn't have to be closed,
        // since the instances are moved through the order that their
        // ordinals put them in rather than through the values themselves
        assertTrue(articles_.restore(second).getOrdinal() < articles_.restore(third).getOrdinal());

        // and moving the first one down still swaps it with the second
        post("/admin/crudArticle/move/" + second + "/down");
        assertTrue(articles_.restore(third).getOrdinal() < articles_.restore(second).getOrdinal());
    }

    @Test
    void testAValueThatCantBeConvertedIsRefusedAndShownBack() {
        var text = conversation_.doRequest("/admin/subscribers/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("email", "gbevin@uwyn.com")
            .parameter("visits", "not a number")).getText();

        // the conversion failure refuses the submission, instead of the
        // property quietly keeping the value that it already had
        assertTrue(text.contains("crud-errors"));
        assertTrue(text.contains("has to be a number"));
        // and what was typed comes back, so it can be corrected
        assertTrue(text.contains("value=\"not a number\""));
        assertEquals(0, subscribers_.count());
    }

    @Test
    void testAValueThatCantBeConvertedDoesntOverwriteTheStoredOne() {
        var subscriber = new CrudSubscriber();
        subscriber.setEmail("gbevin@uwyn.com");
        subscriber.setVisits(42);
        var id = subscribers_.save(subscriber);

        var text = conversation_.doRequest("/admin/subscribers/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("email", "gbevin@uwyn.com")
            .parameter("visits", "not a number")).getText();

        assertTrue(text.contains("has to be a number"));
        // the stored value is untouched, instead of the edit reporting
        // success while having ignored what was submitted
        assertEquals(42, subscribers_.restore(id).getVisits());
    }

    @Test
    void testEntitiesCantBeAddedAfterTheRoutesWereSetUp() {
        // an entity that is added at that point shows up in the navigation
        // while none of its routes exist
        assertThrows(ConfigurationRegisteredException.class, () -> site_.admin.entity(CrudSubscriber.class));
        assertThrows(ConfigurationRegisteredException.class, () -> site_.admin.link("Late", "/late"));
        assertThrows(ConfigurationRegisteredException.class, () -> site_.admin.title("Late"));
        assertThrows(ConfigurationRegisteredException.class, () -> site_.admin.environment("late"));
    }

    @Test
    void testEntityRestrictedToARoleRefusesEveryOperation() {
        var site = new rife.engine.Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    .entity(CrudArticle.class, e -> e.role("editor")));
            }
        };
        var conversation = new MockConversation(site);
        var id = addArticle("First", "draft", 0);

        // hiding an entity that nobody is authenticated for also has to keep
        // its routes out of reach
        for (var url : new String[]{"/admin/crudArticle", "/admin/crudArticle/add",
                                    "/admin/crudArticle/edit/" + id, "/admin/crudArticle/delete/" + id}) {
            var response = conversation.doRequest(url);
            assertEquals(403, response.getStatus(), "reachable without the role: " + url);
        }
        // a move is only reachable the way a browser reaches it
        {
            var response = conversation.doRequest("/admin/crudArticle/move/" + id + "/up",
                new MockRequest().method(RequestMethod.POST));
            assertEquals(403, response.getStatus(), "movable without the role");
            assertTrue(response.getText().contains("Not allowed"));
        }

        // and the dashboard doesn't advertise it either
        var dashboard = conversation.doRequest("/admin/").getText();
        assertFalse(dashboard.contains("crudArticle"));
    }

    @Test
    void testEntityRestrictedToARoleServesTheOnesWhoHoldIt() {
        // the administration reads the identity that authentication leaves
        // on the request, the way the authentication elements leave it
        var identity = new String[]{null};
        var site = new rife.engine.Site() {
            public void setup() {
                before(c -> {
                    if (identity[0] != null) {
                        c.setAttribute(rife.authentication.credentialsmanagers.RoleUserIdentity.class.getName(),
                            new rife.authentication.credentialsmanagers.RoleUserIdentity("gbevin",
                                new rife.authentication.credentialsmanagers.RoleUserAttributes(1L, "pwd", new String[]{identity[0]})));
                    }
                });
                group("/admin", new CrudAdmin(datasource_)
                    .entity(CrudArticle.class, e -> e.role("editor")));
            }
        };
        var conversation = new MockConversation(site);
        var id = addArticle("Guarded", "draft", 0);

        // somebody who holds the role reaches every operation
        identity[0] = "editor";
        for (var url : new String[]{"/admin/crudArticle", "/admin/crudArticle/add",
                                    "/admin/crudArticle/edit/" + id, "/admin/crudArticle/delete/" + id}) {
            var response = conversation.doRequest(url);
            assertNotEquals(403, response.getStatus(), "refused with the role: " + url);
        }
        assertNotEquals(403, conversation.doRequest("/admin/crudArticle/move/" + id + "/up",
            new MockRequest().method(RequestMethod.POST)).getStatus(), "move refused with the role");
        assertTrue(conversation.doRequest("/admin/crudArticle").getText().contains("Guarded"));
        // and the dashboard offers the entity
        assertTrue(conversation.doRequest("/admin/").getText().contains("crudArticle"));

        // while somebody who is authenticated with another role is refused
        // like somebody who isn't authenticated at all
        identity[0] = "accounting";
        for (var url : new String[]{"/admin/crudArticle", "/admin/crudArticle/add",
                                    "/admin/crudArticle/edit/" + id, "/admin/crudArticle/delete/" + id}) {
            assertEquals(403, conversation.doRequest(url).getStatus(), "reachable with the wrong role: " + url);
        }
        assertEquals(403, conversation.doRequest("/admin/crudArticle/move/" + id + "/up",
            new MockRequest().method(RequestMethod.POST)).getStatus(), "movable with the wrong role");
    }

    @Test
    void testValuesAreEncodedInEveryView() {
        var article = article("<script>alert('x')</script>", "draft");
        article.setOrdinal(0);
        var id = articles_.save(article);

        for (var url : new String[]{"/admin/crudArticle", "/admin/crudArticle/edit/" + id, "/admin/crudArticle/delete/" + id}) {
            var text = conversation_.doRequest(url).getText();
            assertFalse(text.contains("<script>alert("), "unescaped payload in " + url);
            assertTrue(text.contains("&lt;script&gt;"), "no encoded payload in " + url);
        }
    }
}
