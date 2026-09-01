/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.crud.exceptions.ConfigurationRegisteredException;
import rife.crud.exceptions.HookDivertedRequestException;
import rife.crud.exceptions.HookFailedException;
import rife.crud.exceptions.PlacementChangedException;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.RequestMethod;
import rife.engine.Site;
import rife.test.MockConversation;
import rife.test.MockRequest;
import rifetestmodels.CrudArticle;
import rifetestmodels.CrudSubscriber;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the hooks that an administration runs around its own
 * operations.
 */
public class TestCrudHooks {
    private final Datasource datasource_ = TestDatasources.H2;
    private GenericQueryManager<CrudArticle> articles_;
    private GenericQueryManager<CrudSubscriber> subscribers_;

    @BeforeEach
    void setup() {
        articles_ = GenericQueryManagerFactory.instance(datasource_, CrudArticle.class);
        subscribers_ = GenericQueryManagerFactory.instance(datasource_, CrudSubscriber.class);
        var admin = new CrudAdmin(datasource_)
            .entity(CrudArticle.class)
            .entity(CrudSubscriber.class);
        try {
            admin.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        admin.install();
    }

    @AfterEach
    void tearDown() {
        try {
            new CrudAdmin(datasource_)
                .entity(CrudArticle.class)
                .entity(CrudSubscriber.class)
                .remove();
        } catch (Exception e) {
            // already gone
        }
    }

    private MockConversation articles(CrudEntityBuilder<CrudArticle> options) {
        return new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_).entity(CrudArticle.class, options));
            }
        });
    }

    private MockConversation subscribers(CrudEntityBuilder<CrudSubscriber> options) {
        return new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_).entity(CrudSubscriber.class, options));
            }
        });
    }

    private int article(String title, int ordinal) {
        var instance = new CrudArticle();
        instance.setTitle(title);
        instance.setOrdinal(ordinal);
        return articles_.insert(instance);
    }

    @Test
    void testABeforeAddHookRefusesWithItsMessage() {
        var context_received = new boolean[]{false};
        var conversation = articles(e -> e.beforeAdd((c, article) -> {
            context_received[0] = c != null;
            return "No articles today.";
        }));

        var response = conversation.doRequest("/admin/crudArticle/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Refused"));

        assertTrue(response.getText().contains("No articles today."));
        assertTrue(context_received[0]);
        assertEquals(0, articles_.count());
    }

    @Test
    void testTheFirstRefusalWins() {
        List<String> ran = new ArrayList<>();
        var conversation = articles(e -> e
            .beforeAdd((c, article) -> {
                ran.add("first");
                return null;
            })
            .beforeAdd((c, article) -> {
                ran.add("second");
                return "The second hook says no.";
            })
            .beforeAdd((c, article) -> {
                ran.add("third");
                return "The third hook says no.";
            }));

        var response = conversation.doRequest("/admin/crudArticle/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Refused"));

        assertTrue(response.getText().contains("The second hook says no."));
        assertEquals(List.of("first", "second"), ran);
        assertEquals(0, articles_.count());
    }

    @Test
    void testAnAfterAddHookThrowingTakesTheAdditionBack() {
        var conversation = articles(e -> e.afterAdd((c, article) -> {
            throw new RuntimeException("the audit trail is down");
        }));

        try {
            conversation.doRequest("/admin/crudArticle/add", new MockRequest()
                .method(RequestMethod.POST)
                .parameter("title", "Kept out"));
        } catch (Throwable e) {
            // the operation is reported as failed
        }

        assertEquals(0, articles_.count());
    }

    @Test
    void testAnAfterAddHookThrowingTakesAnUnorderedAdditionBack() {
        var conversation = subscribers(e -> e.afterAdd((c, subscriber) -> {
            throw new RuntimeException("the audit trail is down");
        }));

        try {
            conversation.doRequest("/admin/crudSubscriber/add", new MockRequest()
                .method(RequestMethod.POST)
                .parameter("email", "kept.out@example.com"));
        } catch (Throwable e) {
            // the operation is reported as failed
        }

        assertEquals(0, subscribers_.count());
    }

    @Test
    void testABeforeUpdateHookRefusesAndNothingChanges() {
        var id = article("First", 0);
        var conversation = articles(e -> e.beforeUpdate((c, article) -> "Not while it's published."));

        var response = conversation.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Changed"));

        assertTrue(response.getText().contains("Not while it's published."));
        assertEquals("First", articles_.restore(id).getTitle());
    }

    @Test
    void testAnAfterUpdateHookThrowingTakesTheUpdateBack() {
        var id = article("First", 0);
        var conversation = articles(e -> e.afterUpdate((c, article) -> {
            throw new RuntimeException("the audit trail is down");
        }));

        try {
            conversation.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
                .method(RequestMethod.POST)
                .parameter("title", "Changed"));
        } catch (Throwable e) {
            // the operation is reported as failed
        }

        assertEquals("First", articles_.restore(id).getTitle());
    }

    @Test
    void testABeforeDeleteHookRefusalIsShownOnTheBrowsePage() {
        var id = article("Kept", 0);
        var conversation = articles(e -> e.beforeDelete((c, article) -> "This one stays."));

        conversation.doRequest("/admin/crudArticle/delete/" + id,
            new MockRequest().method(RequestMethod.POST));
        var browse = conversation.doRequest("/admin/crudArticle");

        assertTrue(browse.getText().contains("This one stays."));
        assertNotNull(articles_.restore(id));
    }

    @Test
    void testAnAfterDeleteHookThrowingTakesTheDeletionBack() {
        var id = article("Kept", 0);
        var conversation = articles(e -> e.afterDelete((c, article) -> {
            throw new RuntimeException("the audit trail is down");
        }));

        try {
            conversation.doRequest("/admin/crudArticle/delete/" + id,
                new MockRequest().method(RequestMethod.POST));
        } catch (Throwable e) {
            // the operation is reported as failed
        }

        assertNotNull(articles_.restore(id));
    }

    @Test
    void testAnAfterDeleteHookReceivesTheInstanceThatWasDeleted() {
        var id = article("Doomed", 0);
        var deleted_title = new String[1];
        var context_received = new boolean[]{false};
        var conversation = articles(e -> e.afterDelete((c, article) -> {
            deleted_title[0] = article.getTitle();
            context_received[0] = c != null;
        }));

        conversation.doRequest("/admin/crudArticle/delete/" + id,
            new MockRequest().method(RequestMethod.POST));

        assertNull(articles_.restore(id));
        assertEquals("Doomed", deleted_title[0]);
        assertTrue(context_received[0]);
    }

    @Test
    void testHooksDontRunForDirectManagerWrites() {
        var fired = new int[]{0};
        articles(e -> e
            .beforeAdd((c, article) -> {
                fired[0]++;
                return null;
            })
            .afterAdd((c, article) -> fired[0]++)
            .beforeUpdate((c, article) -> {
                fired[0]++;
                return null;
            })
            .afterUpdate((c, article) -> fired[0]++)
            .beforeDelete((c, article) -> {
                fired[0]++;
                return null;
            })
            .afterDelete((c, article) -> fired[0]++)
            .beforeMove((c, id, upwards) -> {
                fired[0]++;
                return null;
            })
            .afterMove((c, id, upwards) -> fired[0]++));

        var id = article("Direct", 0);
        var instance = articles_.restore(id);
        instance.setTitle("Changed directly");
        articles_.save(instance);
        articles_.delete(id);

        assertEquals(0, fired[0]);
    }

    @Test
    void testAHookThatFailsIsntBlamedOnTheSubmittedValues() {
        // the hook breaks a constraint of its own, which is the same class
        // of failure that the database reports for a submission it refuses
        var conversation = articles(e -> e.afterAdd((c, article) -> {
            var duplicate = new CrudArticle();
            duplicate.setTitle(null);
            articles_.insert(duplicate);
        }));

        var response = conversation.doRequest("/admin/crudArticle/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Kept out")).getText();

        // the failure is reported as the hook's, not as the addition having
        // been refused, which would have shown the form again
        assertTrue(response.contains(HookFailedException.class.getSimpleName()), response);
        assertTrue(response.contains("'afterAdd' hook"), response);
        assertEquals(0, articles_.count());
    }

    @Test
    void testABeforeHookThatFailsIsntBlamedOnTheSubmittedValues() {
        var conversation = articles(e -> e.beforeAdd((c, article) -> {
            var duplicate = new CrudArticle();
            duplicate.setTitle(null);
            articles_.insert(duplicate);
            return null;
        }));

        var response = conversation.doRequest("/admin/crudArticle/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Kept out")).getText();

        assertTrue(response.contains(HookFailedException.class.getSimpleName()), response);
        assertTrue(response.contains("'beforeAdd' hook"), response);
        assertEquals(0, articles_.count());
    }

    @Test
    void testAnAfterHookThatMovesTheInstanceOutOfItsListIsNoticed() {
        article("First", 0);
        // the hook stores the instance again with an ordinal that the list
        // being held never locked
        var conversation = articles(e -> e.afterAdd((c, article) -> {
            article.setOrdinal(500);
            articles_.update(article);
        }));

        var response = conversation.doRequest("/admin/crudArticle/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Moved by the hook")).getText();

        assertTrue(response.contains(PlacementChangedException.class.getSimpleName()), response);
        // only the row that was already there is left
        assertEquals(1, articles_.count());
    }

    @Test
    void testAnAfterUpdateHookThatMovesTheInstanceOutOfItsListIsNoticed() {
        var id = article("First", 0);
        var conversation = articles(e -> e.afterUpdate((c, article) -> {
            article.setOrdinal(500);
            articles_.update(article);
        }));

        var response = conversation.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Changed")).getText();

        assertTrue(response.contains(PlacementChangedException.class.getSimpleName()), response);
        var stored = articles_.restore(id);
        assertEquals("First", stored.getTitle());
        assertEquals(0, stored.getOrdinal());
    }

    @Test
    void testABeforeMoveHookRefusalIsShownAndNothingMoves() {
        var first = article("First", 0);
        var second = article("Second", 1);
        var conversation = articles(e -> e.beforeMove((c, id, upwards) -> "This one is pinned."));

        conversation.doRequest("/admin/crudArticle/move/" + second + "/up",
            new MockRequest().method(RequestMethod.POST));
        var browse = conversation.doRequest("/admin/crudArticle");

        assertTrue(browse.getText().contains("This one is pinned."));
        assertEquals(0, articles_.restore(first).getOrdinal());
        assertEquals(1, articles_.restore(second).getOrdinal());
    }

    @Test
    void testAnAfterMoveHookReceivesTheMoveThatWasPerformed() {
        var first = article("First", 0);
        var second = article("Second", 1);
        var moved = new int[]{-1};
        var direction = new boolean[]{false};
        var context_received = new boolean[]{false};
        var conversation = articles(e -> e.afterMove((c, id, upwards) -> {
            moved[0] = id;
            direction[0] = upwards;
            context_received[0] = c != null;
        }));

        conversation.doRequest("/admin/crudArticle/move/" + second + "/up",
            new MockRequest().method(RequestMethod.POST));

        assertEquals(second, moved[0]);
        assertTrue(direction[0]);
        assertTrue(context_received[0]);
        assertEquals(1, articles_.restore(first).getOrdinal());
        assertEquals(0, articles_.restore(second).getOrdinal());
    }

    @Test
    void testAnAfterMoveHookThrowingTakesTheMoveBack() {
        var first = article("First", 0);
        var second = article("Second", 1);
        var conversation = articles(e -> e.afterMove((c, id, upwards) -> {
            throw new RuntimeException("the audit trail is down");
        }));

        try {
            conversation.doRequest("/admin/crudArticle/move/" + second + "/up",
                new MockRequest().method(RequestMethod.POST));
        } catch (Throwable e) {
            // the operation is reported as failed
        }

        assertEquals(0, articles_.restore(first).getOrdinal());
        assertEquals(1, articles_.restore(second).getOrdinal());
    }

    @Test
    void testAnAfterMoveHookThatMovesTheInstanceElsewhereIsNoticed() {
        var first = article("First", 0);
        var second = article("Second", 1);
        var conversation = articles(e -> e.afterMove((c, id, upwards) -> {
            var instance = articles_.restore(id);
            instance.setOrdinal(500);
            articles_.update(instance);
        }));

        var response = conversation.doRequest("/admin/crudArticle/move/" + second + "/up",
            new MockRequest().method(RequestMethod.POST)).getText();

        assertTrue(response.contains(PlacementChangedException.class.getSimpleName()), response);
        // the move was taken back along with what the hook did
        assertEquals(0, articles_.restore(first).getOrdinal());
        assertEquals(1, articles_.restore(second).getOrdinal());
    }

    @Test
    void testAHookCanRedirectTheRequest() {
        var id = article("First", 0);
        var conversation = articles(e -> e.beforeUpdate((c, article) -> {
            // a control flow exception isn't a hook that failed, it's the
            // hook deciding where the request goes
            c.redirect("/elsewhere");
            return null;
        }));

        var response = conversation.doRequest("/admin/crudArticle/edit/" + id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Changed"));

        assertTrue(response.getHeader("Location").endsWith("/elsewhere"),
            String.valueOf(response.getHeader("Location")));
        assertEquals("First", articles_.restore(id).getTitle());
    }

    @Test
    void testAnAfterHookCantDivertTheRequest() {
        // the operation is inside its transaction when an after hook runs,
        // so a redirect from one would report an addition that the rollback
        // of that redirect took back
        var conversation = articles(e -> e.afterAdd((c, article) -> c.redirect("/elsewhere")));

        var response = conversation.doRequest("/admin/crudArticle/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Kept out"));

        assertNull(response.getHeader("Location"));
        assertTrue(response.getText().contains(HookDivertedRequestException.class.getSimpleName()),
            response.getText());
        assertEquals(0, articles_.count());
    }

    @Test
    void testAnAfterMoveHookCantDivertTheRequest() {
        var first = article("First", 0);
        var second = article("Second", 1);
        var conversation = articles(e -> e.afterMove((c, id, upwards) -> c.redirect("/elsewhere")));

        var response = conversation.doRequest("/admin/crudArticle/move/" + second + "/up",
            new MockRequest().method(RequestMethod.POST));

        assertTrue(response.getText().contains(HookDivertedRequestException.class.getSimpleName()),
            response.getText());
        assertEquals(0, articles_.restore(first).getOrdinal());
        assertEquals(1, articles_.restore(second).getOrdinal());
    }

    @Test
    void testABeforeMoveHookDoesntRunForAnInstanceThatIsGone() {
        var id = article("Gone", 0);
        articles_.delete(id);
        var ran = new int[]{0};
        var conversation = articles(e -> e.beforeMove((c, moved, upwards) -> {
            ran[0]++;
            return "This one is pinned.";
        }));

        // the request is stale, so there's nothing to refuse and nothing to
        // hand a hook that expects to find the row
        conversation.doRequest("/admin/crudArticle/move/" + id + "/up",
            new MockRequest().method(RequestMethod.POST));
        var browse = conversation.doRequest("/admin/crudArticle");

        assertEquals(0, ran[0]);
        assertTrue(browse.getText().contains("no longer exists"), browse.getText());
    }

    @Test
    void testAnAfterMoveHookDoesntRunForAnInstanceThatCantMove() {
        var first = article("First", 0);
        article("Second", 1);
        var fired = new int[]{0};
        var conversation = articles(e -> e.afterMove((c, id, upwards) -> fired[0]++));

        // the first row is already at the start of its list
        conversation.doRequest("/admin/crudArticle/move/" + first + "/up",
            new MockRequest().method(RequestMethod.POST));

        assertEquals(0, fired[0]);
    }

    @Test
    void testHooksCantBeRegisteredAnymoreAfterwards() {
        var captured = new ArrayList<CrudEntityOptions<CrudArticle>>();
        articles(e -> {
            captured.add(e);
            e.beforeAdd((c, article) -> null);
        });

        assertThrows(ConfigurationRegisteredException.class,
            () -> captured.get(0).beforeAdd((c, article) -> null));
        assertThrows(ConfigurationRegisteredException.class,
            () -> captured.get(0).afterDelete((c, article) -> {
            }));
    }
}
