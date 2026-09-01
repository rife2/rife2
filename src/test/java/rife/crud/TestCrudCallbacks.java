/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.RequestMethod;
import rife.engine.Site;
import rife.test.MockConversation;
import rife.test.MockRequest;
import rifetestmodels.CrudRetargeting;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises what the administration does when the callbacks of a bean point
 * an instance at another row than the one that was asked for.
 */
public class TestCrudCallbacks {
    private final Datasource datasource_ = TestDatasources.H2;
    private MockConversation conversation_;
    private GenericQueryManager<CrudRetargeting> manager_;

    @BeforeEach
    void setup() {
        CrudRetargeting.retargetTo = -1;
        CrudRetargeting.scopeTo = -1;
        CrudRetargeting.retargetAfterStoring = -1;
        CrudRetargeting.scopeAfterStoring = -1;

        manager_ = GenericQueryManagerFactory.instance(datasource_, CrudRetargeting.class);
        var admin = new CrudAdmin(datasource_).entity(CrudRetargeting.class);
        try {
            admin.remove();
        } catch (Exception e) {
            // nothing to remove
        }
        admin.install();

        conversation_ = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_).entity(CrudRetargeting.class));
            }
        });
    }

    @AfterEach
    void tearDown() {
        CrudRetargeting.retargetTo = -1;
        CrudRetargeting.scopeTo = -1;
        CrudRetargeting.retargetAfterStoring = -1;
        CrudRetargeting.scopeAfterStoring = -1;
        try {
            new CrudAdmin(datasource_).entity(CrudRetargeting.class).remove();
        } catch (Exception e) {
            // already gone
        }
    }

    private int add(String title, int section, int ordinal) {
        var instance = new CrudRetargeting();
        instance.setTitle(title);
        instance.setSection(section);
        instance.setOrdinal(ordinal);
        return manager_.insert(instance);
    }

    @Test
    void testAnEditStaysOnTheRowThatTheUrlAsksFor() {
        var first = add("First", 1, 0);
        var second = add("Second", 1, 1);

        // the restore returns the other row's identifier
        CrudRetargeting.retargetTo = second;
        conversation_.doRequest("/admin/crudRetargeting/edit/" + first, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Edited")
            .parameter("section", "1"));

        CrudRetargeting.retargetTo = -1;
        assertEquals("Edited", manager_.restore(first).getTitle());
        assertEquals("Second", manager_.restore(second).getTitle());
    }

    @Test
    void testADeleteStaysOnTheRowThatTheUrlAsksFor() {
        var first = add("First", 1, 0);
        var second = add("Second", 1, 1);

        CrudRetargeting.retargetTo = second;
        conversation_.doRequest("/admin/crudRetargeting/delete/" + first,
            new MockRequest().method(RequestMethod.POST));

        CrudRetargeting.retargetTo = -1;
        assertNull(manager_.restore(first));
        assertNotNull(manager_.restore(second));
    }

    @Test
    void testAnAddIsHeldToTheListThatWasSubmitted() {
        add("Existing", 1, 0);

        // the addition goes where the form said, whatever the callbacks of
        // the restore claim about the instances that are already there
        CrudRetargeting.scopeTo = 2;
        conversation_.doRequest("/admin/crudRetargeting/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Added")
            .parameter("section", "1"));

        CrudRetargeting.scopeTo = -1;
        var stored = manager_.restore(manager_.getRestoreQuery().orderBy("id"));
        assertEquals(2, stored.size());
        assertEquals(1, stored.get(1).getSection());
    }

    @Test
    void testAnAfterHookIsGivenTheRowThatWasStored() {
        // the callback of the store points the instance at another row after
        // it was written, and that isn't something a hook should be auditing
        CrudRetargeting.retargetAfterStoring = 4242;
        var seen = new java.util.ArrayList<Integer>();
        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    .entity(CrudRetargeting.class, e -> e.afterAdd((c, instance) -> seen.add(instance.getId()))));
            }
        });

        conversation.doRequest("/admin/crudRetargeting/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Added")
            .parameter("section", "1"));

        CrudRetargeting.retargetAfterStoring = -1;
        CrudRetargeting.scopeAfterStoring = -1;
        var stored = manager_.restore(manager_.getRestoreQuery().orderBy("id"));
        assertEquals(1, stored.size());
        assertEquals(1, seen.size());
        // the hook was handed the row that the addition actually wrote
        assertEquals(stored.get(0).getId(), seen.get(0));
        assertNotEquals(4242, seen.get(0));
    }

    @Test
    void testAnAfterHookIsGivenThePlacementThatWasStored() {
        // the callback moves the instance after it was written, which the
        // check on the database doesn't see since the row itself stayed put
        CrudRetargeting.scopeAfterStoring = 7;
        var seen = new java.util.ArrayList<String>();
        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    .entity(CrudRetargeting.class, e -> e.afterAdd((c, instance) ->
                        seen.add(instance.getSection() + "/" + instance.getOrdinal()))));
            }
        });

        conversation.doRequest("/admin/crudRetargeting/add", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Added")
            .parameter("section", "1"));

        CrudRetargeting.scopeAfterStoring = -1;
        var stored = manager_.restore(manager_.getRestoreQuery().orderBy("id"));
        assertEquals(1, stored.size());
        assertEquals(1, seen.size());
        // the hook was handed where the row actually sits
        assertEquals(stored.get(0).getSection() + "/" + stored.get(0).getOrdinal(), seen.get(0));
        assertEquals("1/0", seen.get(0));
    }

    @Test
    void testAnAfterUpdateHookIsGivenTheRowThatWasStored() {
        var first = add("First", 1, 0);
        CrudRetargeting.retargetAfterStoring = 4242;
        var seen = new java.util.ArrayList<Integer>();
        var conversation = new MockConversation(new Site() {
            public void setup() {
                group("/admin", new CrudAdmin(datasource_)
                    .entity(CrudRetargeting.class, e -> e.afterUpdate((c, instance) -> seen.add(instance.getId()))));
            }
        });

        conversation.doRequest("/admin/crudRetargeting/edit/" + first, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Edited")
            .parameter("section", "1"));

        CrudRetargeting.retargetAfterStoring = -1;
        CrudRetargeting.scopeAfterStoring = -1;
        assertEquals(1, seen.size());
        assertEquals(first, seen.get(0));
    }

    @Test
    void testAnEditIsHeldToTheListThatIsStored() {
        var first = add("First", 1, 0);
        add("Elsewhere", 2, 0);

        // the restore claims the instance belongs to another list
        CrudRetargeting.scopeTo = 2;
        conversation_.doRequest("/admin/crudRetargeting/edit/" + first, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("title", "Edited"));

        CrudRetargeting.scopeTo = -1;
        // the row keeps the list that the database holds
        assertEquals(1, manager_.restore(first).getSection());
        assertEquals(0, manager_.restore(first).getOrdinal());
    }
}
